package org.pgqp.jpa;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 * Maps a restriction definition to an optional value related to the definition
 * 
 * @param <P>
 *            type of the parent table
 * @param <C>
 *            type of the child table
 * @param <F>
 *            type of the column in the child table
 * @param <V>
 *            type of the restriction field in the criteria
 */
class RestrictionValue<P, C, F, V> {

	private RestrictionDefinition<P, C, F, V> restrictionDefinition;
	private Optional<V> value;

	public RestrictionValue(RestrictionDefinition<P, C, F, V> restrictionDefinition, Optional<V> value) {
		this.restrictionDefinition = restrictionDefinition;
		this.value = value;
	}

	public RestrictionDefinition<P, C, F, V> getRestrictionDefinition() {
		return restrictionDefinition;
	}

	// FIXME: hide the fact that optional is being used internally

	public Optional<V> getValue() {
		return value;
	}

	public boolean hasValue() {
		return value.isPresent();
	}
	
	public Predicate toPredicate(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query, Path<F> attribute) {
		return restrictionDefinition.toPredicate(new QueryContext<>(criteriaBuilder, query, attribute), value.get());
	}
	
}
