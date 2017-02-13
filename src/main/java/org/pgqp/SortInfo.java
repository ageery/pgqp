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

	public enum Direction {
		ASC, DESC;

		public boolean isAscending() {
			return this.equals(ASC);
		}

	}

	private S sortIdentifier;
	private Direction direction;

	public SortInfo(S sortIdentifier) {
		this(sortIdentifier, ASC);
	}

	public SortInfo(S sortIdentifier, Direction direction) {
		this.sortIdentifier = sortIdentifier;
		this.direction = direction;
	}

	public S getSortIdentifier() {
		return sortIdentifier;
	}

	public Direction getDirection() {
		return direction;
	}

}
