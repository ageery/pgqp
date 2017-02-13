package org.pgqp.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.pgqp.StandardOperation;

public final class StandardOperationHandler {

	public static <V> Predicate toPredicate(CriteriaBuilder criteriaBuilder, Expression<V> path, StandardOperation op, V value) {
		Predicate p = null;
		switch (op) {
		case EQ:
			p = criteriaBuilder.equal(path, value);
			break;
		case NE:
			p = criteriaBuilder.notEqual(path, value);
			break;
		default:
			throw new RuntimeException("Operation is not applicable: " + op);
		}
		
		return p;
	}

	public static <Y extends Comparable<? super Y>> Predicate toPredicate(CriteriaBuilder criteriaBuilder, Path<Y> path, StandardOperation op, Y value) {
		Predicate p = null;
		switch (op) {
		case GT:
			p = criteriaBuilder.greaterThan(path, value);
			break;
		case LT:
			p = criteriaBuilder.lessThan(path, value);
			break;
		case GTE:
			p = criteriaBuilder.greaterThanOrEqualTo(path, value);
			break;
		case LTE:
			p = criteriaBuilder.lessThanOrEqualTo(path, value);
			break;
		default:
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Predicate x = StandardOperationHandler.toPredicate(criteriaBuilder, (Path) path, op, (Object) value);
			p = x;
			break;
		}
		
		return p;
	}
	
	public static Predicate toPredicate(CriteriaBuilder criteriaBuilder, Path<String> path, StandardOperation op, String value) {
		Predicate p = null;
		switch (op) {
		case LIKE:
			p = criteriaBuilder.like(path, value);
			break;
		case STARTS_WITH:
			p = criteriaBuilder.like(path, value + "%");
			break;
		case CONTAINS:
			p = criteriaBuilder.like(path, "%" + value + "%");
			break;
		default:
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Predicate x = StandardOperationHandler.toPredicate(criteriaBuilder, (Path) path, op, (Comparable) value);
			p = x;
			break;
		}
		
		return p;
	}
	
	public static Predicate toPredicate(CriteriaBuilder criteriaBuilder, Path<?> path, StandardOperation op, Boolean value) {
		Predicate p = null;
		switch (op) {
		case NULL:
			p = value ? criteriaBuilder.isNull(path) : criteriaBuilder.isNotNull(path);
			break;
		case NOT_NULL:
			p = value ? criteriaBuilder.isNotNull(path) : criteriaBuilder.isNull(path);
			break;
		default:
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Predicate x = StandardOperationHandler.toPredicate(criteriaBuilder, (Path) path, op, (Object) value);
			p = x;
			break;
		}
		
		return p;
	}
	
	private StandardOperationHandler() {
		assert false;
	}
	
}
