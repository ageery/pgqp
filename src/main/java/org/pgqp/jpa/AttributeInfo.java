package org.pgqp.jpa;

import java.util.Objects;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
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
	
	public JoinInfo<P, C> join(From<?, P> from, JoinTypeInfo joinTypeInfo) {
		JoinInfo<P, C> joinInfo;
		if (singluarAttribute != null) {
			joinInfo = toSingularAttributeJoin(from, singluarAttribute, joinTypeInfo);
		} else if (collectionAttribute != null) {
			joinInfo = toCollectionAttributeJoin(from, collectionAttribute, joinTypeInfo);
		} else if (listAttribute != null) {
			joinInfo = toListAttributeJoin(from, listAttribute, joinTypeInfo);
		} else if (setAttribute != null) {
			joinInfo = toSetAttributeJoin(from, setAttribute, joinTypeInfo);
		} else {
			throw new AssertionError("[Internal error] None of the attributes are set");
		}
		return joinInfo;
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
	
	private static JoinType toJoinType(boolean innerJoin) {
		return innerJoin ? JoinType.INNER : JoinType.LEFT;
	} 
	
	private static <P,C> JoinInfo<P, C> toSingularAttributeJoin(From<?, P> from, SingularAttribute<P, C> singularAttribute, JoinTypeInfo joinTypeInfo) {
		return joinTypeInfo.isFetchJoin() 
				? new JoinInfo<>(from.fetch(singularAttribute, toJoinType(joinTypeInfo.isInnerJoin())))
				: new JoinInfo<>(from.join(singularAttribute, toJoinType(joinTypeInfo.isInnerJoin())));
	}

	private static <P,C> JoinInfo<P, C> toCollectionAttributeJoin(From<?, P> from, CollectionAttribute<P,C> collectionAttribute, JoinTypeInfo joinTypeInfo) {
		return joinTypeInfo.isFetchJoin() 
				? new JoinInfo<>(from.fetch(collectionAttribute, toJoinType(joinTypeInfo.isInnerJoin())))
				: new JoinInfo<>(from.join(collectionAttribute, toJoinType(joinTypeInfo.isInnerJoin())));
	}
	
	private static <P,C> JoinInfo<P, C> toListAttributeJoin(From<?, P> from, ListAttribute<P,C> listAttribute, JoinTypeInfo joinTypeInfo) {
		return joinTypeInfo.isFetchJoin() 
				? new JoinInfo<>(from.fetch(listAttribute, toJoinType(joinTypeInfo.isInnerJoin())))
				: new JoinInfo<>(from.join(listAttribute, toJoinType(joinTypeInfo.isInnerJoin())));
	}

	private static <P,C> JoinInfo<P, C> toSetAttributeJoin(From<?, P> from, SetAttribute<P,C> setAttribute, JoinTypeInfo joinTypeInfo) {
		return joinTypeInfo.isFetchJoin() 
				? new JoinInfo<>(from.fetch(setAttribute, toJoinType(joinTypeInfo.isInnerJoin())))
				: new JoinInfo<>(from.join(setAttribute, toJoinType(joinTypeInfo.isInnerJoin())));
	}

}
