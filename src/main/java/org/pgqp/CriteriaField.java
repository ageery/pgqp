package org.pgqp;

/**
 * Combines an operation with a value.
 *
 * @param <V>
 *            type of the operation
 */
public class CriteriaField<V> {

	private final StandardOperation op;
	private final V value;

	/**
	 * Creates a new instance of {@link CriteriaField} with the given operation
	 * and value.
	 * 
	 * @param op
	 *            operation to perform with the data
	 * @param value
	 *            the value of the data
	 */
	public CriteriaField(StandardOperation op, V value) {
		super();
		this.op = op;
		this.value = value;
	}

	/**
	 * Returns the operation.
	 * 
	 * @return the operation
	 */
	public StandardOperation getOp() {
		return op;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * Returns whether the value is set.
	 * 
	 * @return whether the value is set
	 */
	public boolean hasValue() {
		return value != null;
	}

}
