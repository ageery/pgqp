package org.pgqp.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

import org.pgqp.SortInfo;

/**
 * Defines how to create an order by clause on a column in a table.
 * 
 * @param <N>
 *            type of the sort identifier
 * @param <P>
 *            type of the parent table
 * @param <C>
 *            type of the child table
 */
public class SortDefinition<N, P, C> {

	private N identifier;
	private JoinDefinition<P, C> joinDefinition;
	private SingularAttribute<C, ?> attribute;

	public SortDefinition(N identifier, JoinDefinition<P, C> joinDefinition, SingularAttribute<C, ?> attribute) {
		this.identifier = identifier;
		this.joinDefinition = joinDefinition;
		this.attribute = attribute;
	}

	public N getIdentifier() {
		return identifier;
	}

	public JoinDefinition<P, C> getJoinDefinition() {
		return joinDefinition;
	}

	/**
	 * Using the sort definition, creates and returns an order by clause in the
	 * given direciton.
	 * 
	 * @param cb
	 * @param path
	 * @param direction
	 * @return
	 */
	public Order toOrder(QueryContext<?, C> context, SortInfo.Direction direction) {
		CriteriaBuilder cb = context.getCriteriaBuilder();
		Expression<?> exp = ((Path<C>) context.getPath()).get(attribute);
		return direction.isAscending() ? cb.asc(exp) : cb.desc(exp);
	}

}
