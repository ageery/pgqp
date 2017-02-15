package org.pgqp;

/**
 * Entry point into the PGQP library. Defines how to convert
 * {@link QueryDefinition}s into count and data/entity queries.
 *
 * @param <QE>
 *            type of data/entity query returned
 * @param <QC>
 *            type of count query returned
 * @param <T>
 *            type of root element
 * @param <C>
 *            type of criteria
 * @param <S>
 *            type of sort
 */
public interface QueryHandler<QE, QC, T, C, S> {

	/**
	 * Converts a {@link QueryDefinition} to a count query.
	 * 
	 * @param queryDefinition
	 *            query definition to use for constructing the query
	 * @return count query
	 */
	QC toCountQuery(QueryDefinition<C, S> queryDefinition);

	/**
	 * Converts a {@link QueryDefinition} to a data/entity query.
	 * 
	 * @param queryDefinition
	 *            query definition to use for constructing the query
	 * @return data/entity query
	 */
	QE toEntityQuery(QueryDefinition<C, S> queryDefinition);

}
