package org.pgqp;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the restriction and sort portion of a query.
 * 
 * @param <C>
 *            criteria type
 * @param <S>
 *            sort type
 */
public class QueryDefinition<C, S> {

	private C criteria;
	private List<SortInfo<S>> sorts;

	public QueryDefinition(C criteria) {
		this(criteria, (List<SortInfo<S>>) null);
	}

	@SafeVarargs
	public QueryDefinition(C criteria, S... sorts) {
		this.criteria = criteria;
		this.sorts = sorts == null ? null : Stream.of(sorts).map(SortInfo<S>::new)
				.collect(Collectors.toList());
	}
	
	public QueryDefinition(C criteria, List<SortInfo<S>> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	public C getCriteria() {
		return criteria;
	}

	public Stream<SortInfo<S>> getSortStream() {
		return sorts == null ? Stream.empty() : sorts.stream();
	}

}
