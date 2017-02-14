package org.pgqp.jpa;

import java.util.Iterator;

class JoinDefinitionIterator implements Iterator<JoinDefinition<?, ?>>, Iterable<JoinDefinition<?, ?>> {

	private JoinDefinition<?, ?> next;

	public JoinDefinitionIterator(JoinDefinition<?, ?> seed) {
		this.next = seed;
	}

	@Override
	public Iterator<JoinDefinition<?, ?>> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public JoinDefinition<?, ?> next() {
		if (next == null) {
			throw new RuntimeException("Iterator has expired");
		}
		JoinDefinition<?, ?> t = next;
		next = t.getParentJoinDefinition();
		return t;
	}
}