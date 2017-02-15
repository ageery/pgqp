package org.pgqp;

import static org.pgqp.SortInfo.Direction.ASC;

/**
 * Combines a sort definition (how to construct an order by clause on a column
 * in a table) with a sort direction (asc/desc).
 * 
 * @param <S>
 *            type of the sort identifier
 */
public class SortInfo<S> {

	/**
	 * Direction of a sort.
	 */
	public enum Direction {
		ASC, DESC;

		/**
		 * Returns whether the sort direction is ascending.
		 * 
		 * @return whether the sort direction is ascending.
		 */
		public boolean isAscending() {
			return this.equals(ASC);
		}

	}

	private final S sortIdentifier;
	private final Direction direction;

	/**
	 * Constructs an ascending {@link SortInfo} object on
	 * {@code sortIdentifier}.
	 * 
	 * @param sortIdentifier
	 *            identifier of the sort to use
	 */
	public SortInfo(S sortIdentifier) {
		this(sortIdentifier, ASC);
	}

	/**
	 * Constructs a {@link SprtInfo} object on {@code sortIdentifier} in
	 * direction {@code direction}.
	 * 
	 * @param sortIdentifier
	 *            identifier of the sort to use
	 * @param direction
	 *            sort direction (asc/desc)
	 */
	public SortInfo(S sortIdentifier, Direction direction) {
		this.sortIdentifier = sortIdentifier;
		this.direction = direction;
	}

	/**
	 * Returns the identifier of the sort.
	 * 
	 * @return the identifier of the sort
	 */
	public S getSortIdentifier() {
		return sortIdentifier;
	}

	/**
	 * Returns the direction of the sort.
	 * 
	 * @return direction of the sort
	 */
	public Direction getDirection() {
		return direction;
	}

}
