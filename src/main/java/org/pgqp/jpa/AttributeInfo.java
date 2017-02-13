package org.pgqp.jpa;

import java.util.Objects;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Provides a unified join "attribute type" allowing the caller to not care
 * about the exact type of the attribute involved in the join.
 *
 * @param <P>
 *            parent table type
 * @param <C>
 *            child table type
 */
public class AttributeInfo<P, C> {

	private SingularAttribute<P, C> singluarAttribute;
	private CollectionAttribute<P, C> collectionAttribute;
	private ListAttribute<P, C> listAttribute;
	private SetAttribute<P, C> setAttribute;

	public AttributeInfo(SingularAttribute<P, C> singluarAttribute) {
		Objects.requireNonNull(singluarAttribute);
		this.singluarAttribute = singluarAttribute;
	}

	public AttributeInfo(CollectionAttribute<P, C> collectionAttribute) {
		Objects.requireNonNull(collectionAttribute);
		this.collectionAttribute = collectionAttribute;
	}

	public AttributeInfo(ListAttribute<P, C> listAttribute) {
		Objects.requireNonNull(listAttribute);
		this.listAttribute = listAttribute;
	}

	public AttributeInfo(SetAttribute<P, C> setAttribute) {
		Objects.requireNonNull(setAttribute);
		this.setAttribute = setAttribute;
	}

	/**
	 * Returns the join performed from the parent to the child.
	 * 
	 * @param from
	 *            parent table
	 * @param type
	 *            type of the join to perform
	 * @return the join between the parent and child tables, with the given join
	 *         type
	 */
	public Join<P, C> join(From<?, P> from, JoinType type) {
		Join<P, C> join;
		if (singluarAttribute != null) {
			join = from.join(singluarAttribute, type);
		} else if (collectionAttribute != null) {
			join = from.join(collectionAttribute, type);
		} else if (listAttribute != null) {
			join = from.join(listAttribute, type);
		} else if (setAttribute != null) {
			join = from.join(setAttribute, type);
		} else {
			throw new AssertionError("[Internal error] None of the attributes are set");
		}
		return join;
	}
	
	public Expression<?> get(From<?, P> from) {
		Expression<?> exp;
		if (singluarAttribute != null) {
			exp = from.get(singluarAttribute);
		} else if (collectionAttribute != null) {
			exp = from.get(collectionAttribute);
		} else if (listAttribute != null) {
			exp = from.get(listAttribute);
		} else if (setAttribute != null) {
			exp = from.get(setAttribute);
		} else {
			throw new AssertionError("[Internal error] None of the attributes are set");
		}
		return exp;
	}

	/**
	 * Returns whether the join from the parent to the child potentially
	 * consists of more than one row.
	 * 
	 * @return
	 */
	public boolean isOneToMany() {
		return singluarAttribute == null;
	}

}
