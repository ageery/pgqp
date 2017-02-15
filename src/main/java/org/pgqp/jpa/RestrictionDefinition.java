package org.pgqp.jpa;

import java.util.function.BiFunction;

import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Defines how to create a restriction for a given column on a given table.
 * 
 * @param <P>
 *            type of the parent table
 * @param <C>
 *            type of the child table
 * @param <F>
 *            type of the column in the child table
 * @param <V>
 *            type of the search criteria
 */
public class RestrictionDefinition<P, C, F, V> {

	private JoinDefinition<P, C> joinDefinition;
	private SingularAttribute<C, F> attribute;
	private BiFunction<QueryContext<?, F>, V, Predicate> toPredicate;

	public RestrictionDefinition(JoinDefinition<P, C> joinDefinition, SingularAttribute<C, F> attribute,
			BiFunction<QueryContext<?, F>, V, Predicate> toPredicate) {
		this.joinDefinition = joinDefinition;
		this.attribute = attribute;
		this.toPredicate = toPredicate;
	}

	public JoinDefinition<P, C> getJoinDefinition() {
		return joinDefinition;
	}

	public SingularAttribute<C, F> getAttribute() {
		return attribute;
	}

	public BiFunction<QueryContext<?, F>, V, Predicate> getToPredicate() {
		return toPredicate;
	}

	/**
	 * Converts a restriction definition to a JPA predicate.
	 * 
	 * @param queryContext
	 * @param value
	 * @return
	 */
	public Predicate toPredicate(QueryContext<?, F> queryContext, V value) {
		return toPredicate.apply(queryContext, value);
	}

}
