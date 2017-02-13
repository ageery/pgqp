package org.pgqp.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;

/**
 * Provides the context of a criteria query.
 * 
 * @param <Q>
 *            type of the query
 * @param <F>
 *            type of the column
 */
public class QueryContext<Q, F> {

	private CriteriaBuilder criteriaBuilder;
	private CriteriaQuery<Q> query;
	private Path<F> path;

	public QueryContext(CriteriaBuilder criteriaBuilder, CriteriaQuery<Q> query, Path<F> path) {
		this.criteriaBuilder = criteriaBuilder;
		this.query = query;
		this.path = path;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return criteriaBuilder;
	}

	public CriteriaQuery<Q> getQuery() {
		return query;
	}

	public Path<F> getPath() {
		return path;
	}
}
