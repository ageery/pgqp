package org.pgqp.jpa;

import static org.jooq.lambda.tuple.Tuple.tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.pgqp.QueryDefinition;
import org.pgqp.QueryHandler;
import org.pgqp.SortInfo;

public class JpaQueryHandler<T, ID, C, S> implements QueryHandler<CriteriaQuery<T>, CriteriaQuery<Long>, T, C, S> {
	
	private static final Consumer<QueryContext<Long, ?>> COUNT_CUSTOMIZER = (context) -> context.getQuery().select(context.getCriteriaBuilder().count(context.getPath()));  
	
	private Class<ID> idClass;
	private Class<T> entityClass;
	private Function<Root<T>, Expression<ID>> idExpression;
	private Map<S, SortDefinition<S, ?, ?>> sortDefinitions;
	private Collection<RestrictionMapping<C, ?, ?, ?, ?>> restrictions;
	private EntityManager entityManager;
	private JoinDefinition<?, T> rootJoinDefinition;

	public JpaQueryHandler(EntityManager entityManager, Class<T> entityClass, Class<ID> idClass, JoinDefinition<?, T> rootJoinDefinition,
			Function<Root<T>, Expression<ID>> idExpression, Collection<RestrictionMapping<C, ?, ?, ?, ?>> restrictions,
			Collection<SortDefinition<S, ?, ?>> sorts) {
		this.entityManager = entityManager;
		this.idClass = idClass;
		this.rootJoinDefinition = rootJoinDefinition;
		this.idExpression = idExpression;
		this.entityClass = entityClass;
		this.restrictions = restrictions;
		this.sortDefinitions = sorts.stream()
				.collect(Collectors.toMap(SortDefinition::getIdentifier, Function.identity()));
	}
	
	@Override
	public CriteriaQuery<Long> toCountQuery(QueryDefinition<C, S> queryDefinition) {
		return toCriteriaQuery(Long.class, queryDefinition, COUNT_CUSTOMIZER, false);
	}

	@Override
	public CriteriaQuery<T> toEntityQuery(QueryDefinition<C, S> queryDefinition) {
		return toCriteriaQuery(entityClass, queryDefinition, null, true);
	}
	
	private <Q> CriteriaQuery<Q> toCriteriaQuery(Class<Q> queryClass, QueryDefinition<C, S> queryDefinition,
			Consumer<QueryContext<Q, ?>> queryCustomizer, boolean handleSorts) {
		
		/*
		 * Query set up.
		 */
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Q> query = criteriaBuilder.createQuery(queryClass);
		Root<T> root = query.from(entityClass);
		if (queryCustomizer != null) {
			queryCustomizer.accept(new QueryContext<>(criteriaBuilder, query, root));
		}
		
		/*
		 * Map restrictions to joins.
		 */
		Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> restrictionsByTable = groupByTableInfo(
				toRestrictionValues(queryDefinition.getCriteria())
				.filter(RestrictionValue::hasValue));
		
		/*
		 * Map joins by whether they are in the main query or in a sub-query.
		 */
		Map<Boolean, List<JoinDefinition<?, ?>>> tableTypes = splitByType(restrictionsByTable.keySet().stream());
		
		/*
		 * Joins dictated by restrictions in the main query.
		 */
		Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> mainQueryInfo = toFilteredMap(restrictionsByTable,
				tableTypes.get(false));
		
		/*
		 * Joins dictated by sorts. 
		 */
		Stream<SortInfo<S>> sortStream = queryDefinition.getSortStream();
		Stream<JoinDefinition<?, ?>> sortJoins = handleSorts && sortStream != null ? toSortInfoStream(sortStream) : Stream.empty();
		List<JoinDefinition<?, ?>> sortList = sortJoins.collect(Collectors.toList());
		
		/*
		 * All joins with information about the join type.
		 */
		Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap = toJoinTypeInfoMap(mainQueryInfo.keySet().stream(), sortList.stream());
		
		/*
		 * Create the joins.
		 */
		Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> joinInfoMap = toJoinInfo(root, Stream.concat(mainQueryInfo.keySet().stream(), sortList.stream()), joinTypeInfoMap);
		
		/*
		 * Create the predicates.
		 */
		List<Predicate> mainQueryPredicates = toPredicates(criteriaBuilder, query, mainQueryInfo, joinInfoMap)
				.collect(Collectors.toList());

		/*
		 * Sub-query handling.
		 */
		List<JoinDefinition<?, ?>> subqueryTables = tableTypes.get(true);
		if (!subqueryTables.isEmpty()) {
			Subquery<ID> subquery = handleSubquery(criteriaBuilder, query, root, restrictionsByTable, subqueryTables);
			mainQueryPredicates.add(criteriaBuilder.in(idExpression.apply(root)).value(subquery));
		}
		
		/*
		 * Create the where clause.
		 */
		if (!mainQueryPredicates.isEmpty()) {
			query.where(mainQueryPredicates.toArray(new Predicate[mainQueryPredicates.size()]));
		}

		/*
		 * Create the order by clause.
		 */
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
		Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> subQueryInfo = toFilteredMap(restrictionsByTable, tables);
		Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap = toJoinTypeInfoMap(subQueryInfo.keySet().stream(), Stream.empty());
		Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> joinInfoMap = toJoinInfo(subqueryRoot, subQueryInfo.keySet().stream(), joinTypeInfoMap);
		List<Predicate> subQueryPredicates = toPredicates(criteriaBuilder, query, subQueryInfo, joinInfoMap)
				.collect(Collectors.toList());
		return subquery.where(subQueryPredicates.toArray(new Predicate[subQueryPredicates.size()]));
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
	
	private <P> Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> toJoinInfo(Root<?> root, Stream<JoinDefinition<?, ?>> joinDefinitions, Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap) {
		Map<JoinDefinition<?,?>, JoinInfo<?,?>> joinInfoMap = new HashMap<>();
		joinInfoMap.put(rootJoinDefinition, new JoinInfo<>(root));
		joinDefinitions.forEach(x -> {
			Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> map = toJoinInfo2(root, x, joinInfoMap, joinTypeInfoMap);
			joinInfoMap.putAll(map);
		});
		return joinInfoMap;
	}
	
	private <P> Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> toJoinInfo2(Root<?> root, JoinDefinition<?, ?> joinDefinition, Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> joinInfoMap, Map<JoinDefinition<?, ?>, JoinTypeInfo> joinTypeInfoMap) {
		return Seq.seq(joinDefinition.fromRoot())	
				.sliding(2)
				.map(x -> x.collect(Collectors.toList()))
				.map(x -> Tuple.tuple(x.get(0), x.get(1)))
				.filter(t -> !joinInfoMap.containsKey(t.v2))
				.map(t -> {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						JoinInfo<?,?> ji = joinInfoMap.getOrDefault(t.v2, toJoinInfo(t.v2, (JoinInfo) joinInfoMap.get(t.v1), joinTypeInfoMap.get(t.v2)));
						return Tuple.tuple(t.v2, ji);
				})
				.collect(Collectors.toMap(Tuple2::v1, Tuple2::v2));
	}
	
	private <P,R> JoinInfo<P, R> toJoinInfo(JoinDefinition<P,R> joinDefinition, JoinInfo<?,P> parentJoin, JoinTypeInfo joinTypeInfo) {
		return joinDefinition.join(parentJoin.toFrom(), joinTypeInfo);
	}
	
	private Stream<JoinDefinition<?,?>> toSortInfoStream(Stream<SortInfo<S>> sortInfoStream) {
		return sortInfoStream
				.map(SortInfo::getSortIdentifier)
				.map(sortDefinitions::get)
				.filter(Objects::nonNull)
				.map(SortDefinition::getJoinDefinition);
	}

	@SuppressWarnings("unchecked")
	private <Q,F> Stream<Order> toOrders(CriteriaBuilder criteriaBuilder, CriteriaQuery<Q> query,
			Map<JoinDefinition<?, ?>, JoinInfo<?, ?>> tables, Stream<SortInfo<S>> sorts) {
		return sorts
				.map(si -> Tuple.tuple(si, sortDefinitions.get(si.getSortIdentifier())))
				.filter(t -> t.v2() != null)
				.map(t -> Tuple.tuple((SortDefinition<S, ?, F>) t.v2(), (JoinInfo<?,F>) tables.get(t.v2().getJoinDefinition()), t.v1().getDirection()))
				.map(t -> t.v1().toOrder(new QueryContext<Q,F>(criteriaBuilder, query, t.v2().toPath()), t.v3()));
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

	private <X, F> Stream<Predicate> toPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, JoinInfo<?,X> joinInfo, Stream<RestrictionValue<?, X, F, ?>> restrictions) {
		return restrictions.map(r -> tuple(r, joinInfo.get(r.getRestrictionDefinition().getAttribute())))
				.map(t -> t.v1.toPredicate(criteriaBuilder, query, t.v2));
	}

	@SuppressWarnings("unchecked")
	private <X,F> Stream<Predicate> toPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query,
			Map<JoinDefinition<?, ?>, List<RestrictionValue<?, ?, ?, ?>>> restrictions,
			Map<JoinDefinition<?, ?>, JoinInfo<?,?>> joins) {
		return restrictions.entrySet().stream()
				.map(e -> Tuple.tuple(joins.get(e.getKey()), e.getValue().stream()))
				.map(t -> toPredicates(criteriaBuilder, query, (JoinInfo<?,X>) t.v1(), (Stream<RestrictionValue<?, X, F, ?>>) (Stream<?>) t.v2()))
				.flatMap(Function.identity());
	}

}
