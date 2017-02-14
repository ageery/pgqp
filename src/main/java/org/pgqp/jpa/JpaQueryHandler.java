package org.pgqp.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.pgqp.QueryDefinition;
import org.pgqp.QueryHandler;
import org.pgqp.SortInfo;

public class JpaQueryHandler<T, ID, C, S> implements QueryHandler<CriteriaQuery<T>, CriteriaQuery<Long>, T, C, S> {
	
	private Class<ID> idClass;
	private Class<T> entityClass;
	private Function<Root<T>, Expression<ID>> idExpression;
	private Map<S, SortDefinition<S, ?, ?>> sortDefinitions;
	private Collection<RestrictionMapping<C, ?, ?, ?, ?>> restrictions;
	private EntityManager entityManager;

	public JpaQueryHandler(EntityManager entityManager, Class<T> entityClass, Class<ID> idClass,
			Function<Root<T>, Expression<ID>> idExpression, Collection<RestrictionMapping<C, ?, ?, ?, ?>> restrictions,
			Collection<SortDefinition<S, ?, ?>> sorts) {
		this.entityManager = entityManager;
		this.idClass = idClass;
		this.idExpression = idExpression;
		this.entityClass = entityClass;
		this.restrictions = restrictions;
		this.sortDefinitions = sorts.stream()
				.collect(Collectors.toMap(SortDefinition::getIdentifier, Function.identity()));
	}

	@Override
	public CriteriaQuery<Long> toCountQuery(QueryDefinition<C, S> queryDefinition) {
		return toCriteriaQuery(Long.class, queryDefinition,
				(context) -> context.getQuery().select(context.getCriteriaBuilder().count(context.getPath())), false);
	}

	@Override
	public CriteriaQuery<T> toEntityQuery(QueryDefinition<C, S> queryDefinition) {
		return toCriteriaQuery(entityClass, queryDefinition, null, true);
	}

	private Map<JoinDefinition<?, ?>, JoinTypeInfo> toJoinTypeInfoMap(Stream<JoinDefinition<?, ?>> restrictionJoins, Stream<JoinDefinition<?, ?>> sortJoins) {
		return Stream.concat(
				restrictionJoins
					.map(JoinDefinition::fromRoot)
					.flatMap(Function.identity())
					.map(jd -> Tuple.tuple(jd, new JoinTypeInfo(true, false))), 
				sortJoins
					.map(JoinDefinition::fromRoot)
					.flatMap(Function.identity())
					.map(jd -> Tuple.tuple(jd, new JoinTypeInfo(false, true))))
			.collect(Collectors.toMap(
				Tuple2::v1, 
				Tuple2::v2, 
				JoinTypeInfo.MERGE_FUNCTION));
	}
	
	private Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> toJoinInfo(Root<?> root, Stream<JoinDefinition<?, ?>> joinDefinitions, Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap) {
		Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> existingJoins = new HashMap<>();
		JoinInfo<?, ?> lastFrom = new JoinInfo<>(root);
		for (JoinDefinition<?,?> join : joinDefinitions.collect(Collectors.toList())) {
			
			if (join.getParentJoinDefinition() == null) {
				if (!existingJoins.containsKey(join)) {
					existingJoins.put(join, new JoinInfo<>(root));
				}
			} else {
				if (!existingJoins.containsKey(join)) {
					JoinTypeInfo jtInfo = joinTypeInfoMap.get(join);
					@SuppressWarnings({ "unchecked", "rawtypes" })
					JoinInfo<?,?> f = join.getAttributeInfo().join((From) lastFrom.toFrom(), jtInfo);
					existingJoins.put(join, f);
					lastFrom = f;
				}
			}
		}
		return existingJoins;
	}
	
	private Stream<JoinDefinition<?,?>> toSortInfoStream(Stream<SortInfo<S>> sortInfoStream) {
		return sortInfoStream
				.map(SortInfo::getSortIdentifier)
				.map(sortDefinitions::get)
				.filter(Objects::nonNull)
				.map(SortDefinition::getJoinDefinition);
	}
	
	private <Q> CriteriaQuery<Q> toCriteriaQuery(Class<Q> queryClass, QueryDefinition<C, S> queryDefinition,
			Consumer<QueryContext<Q, ?>> queryCustomizer, boolean handleSorts) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Q> query = criteriaBuilder.createQuery(queryClass);
		Root<T> root = query.from(entityClass);
		if (queryCustomizer != null) {
			queryCustomizer.accept(new QueryContext<>(criteriaBuilder, query, root));
		}
		Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> restrictionsByTable = groupByTableInfo(
				toRestrictionValues(queryDefinition.getCriteria()).filter(RestrictionValue::hasValue));
		Map<Boolean, List<JoinDefinition<?, ?>>> tableTypes = splitByType(restrictionsByTable.keySet().stream());
		Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> mainQueryInfo = toFilteredMap(restrictionsByTable,
				tableTypes.get(false));
		
		Stream<SortInfo<S>> sortStream = queryDefinition.getSortStream();
		Stream<JoinDefinition<?, ?>> sortJoins = handleSorts && sortStream != null ? toSortInfoStream(sortStream) : Stream.empty();
		List<JoinDefinition<?, ?>> sortList = sortJoins.collect(Collectors.toList());
		Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap = toJoinTypeInfoMap(mainQueryInfo.keySet().stream(), sortList.stream());
		Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> joinInfoMap = toJoinInfo(root, Stream.concat(mainQueryInfo.keySet().stream(), sortList.stream()), joinTypeInfoMap);
		List<Predicate> mainQueryPredicates = toPredicates(criteriaBuilder, query, mainQueryInfo, joinInfoMap).collect(Collectors.toList());

		List<JoinDefinition<?, ?>> subqueryTables = tableTypes.get(true);
		if (!subqueryTables.isEmpty()) {
			Subquery<ID> subquery = handleSubquery(criteriaBuilder, query, root, restrictionsByTable, subqueryTables);
			mainQueryPredicates = new ArrayList<>(mainQueryPredicates);
			mainQueryPredicates.add(criteriaBuilder.in(idExpression.apply(root)).value(subquery));
		}
		
		if (!mainQueryPredicates.isEmpty()) {
			query.where(mainQueryPredicates.toArray(new Predicate[mainQueryPredicates.size()]));
		}

		if (handleSorts) {
			List<Order> orders = toOrders(criteriaBuilder, query, joinInfoMap, queryDefinition.getSortStream())
					.collect(Collectors.toList());
			if (!orders.isEmpty()) {
				query.orderBy(orders);
			}
		}

		return query;
	}

	private Subquery<ID> handleSubquery(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, Root<T> root,
			Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> restrictionsByTable,
			List<JoinDefinition<?, ?>> tables) {
		Subquery<ID> subquery = query.subquery(idClass);
		Root<T> subqueryRoot = subquery.from(entityClass);
		subquery.select(idExpression.apply(subqueryRoot));
		Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> subQueryInfo = toFilteredMap(restrictionsByTable,
				tables);
		Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap = toJoinTypeInfoMap(subQueryInfo.keySet().stream(), Stream.empty());
		Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> joinInfoMap = toJoinInfo(subqueryRoot, subQueryInfo.keySet().stream(), joinTypeInfoMap);
		List<Predicate> subQueryPredicates = toPredicates(criteriaBuilder, query, subQueryInfo, joinInfoMap)
				.collect(Collectors.toList());
		return subquery.where(subQueryPredicates.toArray(new Predicate[subQueryPredicates.size()]));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <Q> Stream<Order> toOrders(CriteriaBuilder criteriaBuilder, CriteriaQuery<Q> query,
			Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> tables, Stream<SortInfo<S>> sorts) {
		return sorts
				.map(si -> Tuple.tuple(si, sortDefinitions.get(si.getSortIdentifier())))
				.filter(t -> t.v2() != null)
				.map(t -> Tuple.tuple(t.v2(), tables.get(t.v2().getJoinDefinition()), t.v1().getDirection()))
				.map(t -> t.v1().toOrder(new QueryContext(criteriaBuilder, query, t.v2().toPath()), t.v3()));
	}

	private Stream<RestrictionValue<?, ?, ?, ?>> toRestrictionValues(C criteria) {
		return restrictions.stream().map(x -> x.toRestrictionValue(criteria));
	}

	private Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> groupByTableInfo(
			Stream<RestrictionValue<?, ?, ?, ?>> restrictionValues) {
		return restrictionValues.filter(RestrictionValue::hasValue)
				.collect(Collectors.groupingBy(x -> x.getRestrictionDefinition().getJoinDefinition()));
	}

	private Map<Boolean, List<JoinDefinition<?, ?>>> splitByType(Stream<JoinDefinition<?, ?>> tables) {
		return tables.collect(Collectors.partitioningBy(JoinDefinition::hasOneToManyRelationship));
	}

	private static <K, V> Map<K, V> toFilteredMap(Map<K, V> map, Collection<K> keys) {
		return map.entrySet().stream().filter(e -> keys.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Stream<Predicate> toPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, JoinInfo<?,?> joinInfo,
			Stream<RestrictionValue<?, ?, ?, ?>> restrictions) {
		return restrictions.map(r -> {
			RestrictionDefinition def = r.getRestrictionDefinition();
			Optional<?> op = r.getValue();
			return def.toPredicate(new QueryContext(criteriaBuilder, query, joinInfo.get(def.getAttribute())), op.get());
		});
	}

	private Stream<Predicate> toPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query,
			Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> restrictions,
			Map<JoinDefinition<?, ?>, JoinInfo<?,?>> joins) {
		return restrictions.entrySet().stream().map(e -> Tuple.tuple(joins.get(e.getKey()), e.getValue().stream()))
				.map(t -> {
					JoinInfo<?,?> x = t.v1();
					Stream<RestrictionValue<?, ?, ?, ?>> restrictionValues = t.v2();
					return toPredicates(criteriaBuilder, query, x, restrictionValues);
				}).flatMap(Function.identity());
	}

}
