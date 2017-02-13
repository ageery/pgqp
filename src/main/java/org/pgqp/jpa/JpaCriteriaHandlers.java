package org.pgqp.jpa;

import java.util.function.BiFunction;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.pgqp.CriteriaField;

public final class JpaCriteriaHandlers {

	public static final BiFunction<QueryContext<?, ?>, ?, Predicate> EQ_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().equal(context.getPath(), value);

	public static final BiFunction<QueryContext<?, ?>, ?, Predicate> NE_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().notEqual(context.getPath(), value);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final BiFunction<QueryContext<?, Comparable>, Comparable, Predicate> GT_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().greaterThan(context.getPath(), value);
			
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final BiFunction<QueryContext<?, Comparable>, Comparable, Predicate> GTE_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().greaterThanOrEqualTo(context.getPath(), value);
					
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final BiFunction<QueryContext<?, Comparable>, Comparable, Predicate> LT_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().lessThan(context.getPath(), value);				

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final BiFunction<QueryContext<?, Comparable>, Comparable, Predicate> LTE_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().lessThanOrEqualTo(context.getPath(), value);				

	public static final BiFunction<QueryContext<?, ?>, Boolean, Predicate> NULL_FIELD_HANDLER = 
			(context, value) -> value ? context.getCriteriaBuilder().isNull(context.getPath()) : context.getCriteriaBuilder().isNotNull(context.getPath());			

	public static final BiFunction<QueryContext<?, ?>, Boolean, Predicate> NOT_NULL_FIELD_HANDLER = 
			(context, value) -> value ? context.getCriteriaBuilder().isNotNull(context.getPath()) : context.getCriteriaBuilder().isNull(context.getPath());			
			
	public static final BiFunction<QueryContext<?, String>, String, Predicate> LIKE_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().like(context.getPath(), value);

	public static final BiFunction<QueryContext<?, String>, String, Predicate> CONTAINS_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().like(context.getPath(), "%" + value + "%");			

	public static final BiFunction<QueryContext<?, String>, String, Predicate> STARTS_WITH_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().like(context.getPath(), value + "%");				

	public static final BiFunction<QueryContext<?, String>, String, Predicate> STARTS_WITH_CI_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().like(context.getCriteriaBuilder().lower(context.getPath()), value.toLowerCase() + "%");				

	public static final BiFunction<QueryContext<?, String>, String, Predicate> CONTAINS_CI_FIELD_HANDLER = 
			(context, value) -> context.getCriteriaBuilder().like(context.getCriteriaBuilder().lower(context.getPath()), "%" + value.toLowerCase() + "%");				
			
	public static final BiFunction<QueryContext<?, String>, CriteriaField<String>, Predicate> STRING_FIELD_HANDLER = 
			(context, value) -> StandardOperationHandler.toPredicate(context.getCriteriaBuilder(), context.getPath(),
					value.getOp(), value.getValue());

	public static final BiFunction<QueryContext<?, ?>, CriteriaField<Boolean>, Predicate> BOOLEAN_FIELD_HANDLER = 
			(context, value) -> StandardOperationHandler.toPredicate(context.getCriteriaBuilder(), context.getPath(),
					value.getOp(), value.getValue());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final BiFunction<QueryContext<?, Comparable<?>>, CriteriaField<Comparable<?>>, Predicate> COMPARABLE_FIELD_HANDLER = 
			(context, value) -> StandardOperationHandler.toPredicate(context.getCriteriaBuilder(),
					(Path) context.getPath(), value.getOp(), (Comparable) value.getValue());

	public static final BiFunction<QueryContext<?, Object>, CriteriaField<?>, Predicate> SIMPLE_FIELD_HANDLER = 
			(context, value) -> StandardOperationHandler.toPredicate(context.getCriteriaBuilder(), context.getPath(),
					value.getOp(), value.getValue());

	public static <T> BiFunction<QueryContext<?, T>, Boolean, Predicate> nullFieldHandler(Class<T> fieldClass) {
		return (context, value) -> value 
				? context.getCriteriaBuilder().isNull(context.getPath()) 
				: context.getCriteriaBuilder().isNotNull(context.getPath());			
	}

	public static <T> BiFunction<QueryContext<?, T>, Boolean, Predicate> notNullFieldHandler(Class<T> fieldClass) {
		return (context, value) -> value 
				? context.getCriteriaBuilder().isNotNull(context.getPath()) 
				: context.getCriteriaBuilder().isNull(context.getPath());			
	}
	
	private JpaCriteriaHandlers() {
		assert false;
	}

}
