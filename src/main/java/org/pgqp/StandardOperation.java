package org.pgqp;

/**
 * Standard criteria operations.
 */
public enum StandardOperation {

	/** Equal */
	EQ,
	/** Not equal */
	NE,
	/** Greater than */
	GT,
	/** Less than */
	LT,
	/** Greater than or equal to */
	GTE,
	/** Less than or equal to */
	LTE,
	/** Is null */
	NULL,
	/** Is not null */
	NOT_NULL,
	/** Like (string) */
	LIKE,
	/** Starts-with (string) */
	STARTS_WITH,
	/** Contains (string) */
	CONTAINS,
	/** Starts-with (case-insensitive) (string) */
	STARTS_WITH_CI,
	/** Contains (case-insensitive) (string) */
	CONTAINS_CI;
	
}
