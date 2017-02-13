package org.pgqp;

public class CriteriaField<V> {

	private StandardOperation op;
	private V value;
	
	public CriteriaField(StandardOperation op, V value) {
		super();
		this.op = op;
		this.value = value;
	}

	public StandardOperation getOp() {
		return op;
	}

	public V getValue() {
		return value;
	}
	
	public boolean hasValue() {
		return value != null;
	}
	
}
