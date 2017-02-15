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

	private final C criteria;
	private final List<SortInfo<S>> sorts;

	/**
	 * Creates a new {@link QueryDefinition} object with the given restriction
	 * {@code criteria} object but no sort.
	 * 
	 * @param criteria
	 *            query restriction information
	 */
	public QueryDefinition(C criteria) {
		this(criteria, (List<SortInfo<S>>) null);
	}

	/**
	 * Creates a new {@link QueryDefinition} object with the given restriction
	 * {@code criteria} and sorted ascending by {@code sorts}.
	 * 
	 * @param criteria
	 *            query restriction information
	 * @param sorts
	 *            ascending sorts to use
	 */
	@SafeVarargs
	public QueryDefinition(C criteria, S... sorts) {
		this(criteria, sorts == null ? null : Stream.of(sorts).map(SortInfo<S>::new).collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@link QueryDefinition} object with the given restriction
	 * {@code criteria} and a description of the sort information encoded in the
	 * list of {@link SortInfo} objects.
	 * 
	 * @param criteria
	 *            query restriction information
	 * @param sorts
	 *            information about how to sort the query
	 */
	public QueryDefinition(C criteria, List<SortInfo<S>> sorts) {
		this.criteria = criteria;
		this.sorts = sorts;
	}

	/**
	 * Returns the criteria portion of the query definition.
	 * 
	 * @return the criteria portion of the query definition
	 */
	public C getCriteria() {
		return criteria;
	}

	/**
	 * Returns the sort portion of the query definition as a non-null
	 * {@link Stream} of {@link SortInfo} objects.
	 * 
	 * @return the sort portion of the query definition
	 */
	public Stream<SortInfo<S>> getSortStream() {
		return sorts == null ? Stream.empty() : sorts.stream();
	}

}
