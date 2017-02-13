package org.pgqp;

public interface QueryHandler<QE, QC, T, C, S> {

	QC toCountQuery(QueryDefinition<C, S> queryDefinition);

	QE toEntityQuery(QueryDefinition<C, S> queryDefinition);

}
