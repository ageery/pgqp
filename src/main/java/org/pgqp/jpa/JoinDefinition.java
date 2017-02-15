package org.pgqp.jpa;

import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.criteria.From;

/**
 * Defines how to join a parent to a child table.
 * 
 * @param <P>
 *            type of the parent table
 * @param <C>
 *            type of the child table
 */
public class JoinDefinition<P, C> {

	private String identifier;
	private Class<C> tableClass;
	private JoinDefinition<?, P> parentJoinDefinition;
	private AttributeInfo<P, C> attributeInfo;

	public JoinDefinition(String identifier, Class<C> tableClass) {
		this(identifier, tableClass, null, null);
	}

	public JoinDefinition(String identifier, Class<C> tableClass, JoinDefinition<?, P> parentJoinDefinition,
			AttributeInfo<P, C> attributeInfo) {
		this.identifier = identifier;
		this.tableClass = tableClass;
		this.parentJoinDefinition = parentJoinDefinition;
		this.attributeInfo = attributeInfo;
	}

	public String getJoinIdentifier() {
		return identifier;
	}

	public Class<C> getTableClass() {
		return tableClass;
	}

	public JoinDefinition<?, P> getParentJoinDefinition() {
		return parentJoinDefinition;
	}

	public AttributeInfo<P, C> getAttributeInfo() {
		return attributeInfo;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JoinDefinition)) {
			return false;
		}
		JoinDefinition<?, ?> joinDefinition = (JoinDefinition<?, ?>) other;
		return Objects.equals(identifier, joinDefinition.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(identifier);
	}

	/**
	 * Returns a Stream of joins required to use this join.
	 * 
	 * @return Stream of joins required in order to use this join
	 */
	public Stream<JoinDefinition<?, ?>> fromRoot() {
		List<JoinDefinition<?, ?>> list = StreamSupport
				.stream(new JoinDefinitionIterator(this).spliterator(), false)
				.collect(Collectors.toList());
		Collections.reverse(list);
		return list.stream();
	}

	/**
	 * Returns whether, in the joins required to use this join, there are any
	 * joins that potentially produce more than one row.
	 * 
	 * @return whether any of the joins leading up to this joins potentially
	 *         produces more than one row
	 */
	public boolean hasOneToManyRelationship() {
		return StreamSupport
				.stream(spliteratorUnknownSize(new JoinDefinitionIterator(this), Spliterator.ORDERED), false)
				.map(JoinDefinition::getAttributeInfo)
				.filter(Objects::nonNull)
				.anyMatch(AttributeInfo::isOneToMany);
	}
	
	public JoinInfo<P, C> join(From<?, P> from, JoinTypeInfo joinTypeInfo) {
		return attributeInfo.join(from, joinTypeInfo);
	}
	
}
