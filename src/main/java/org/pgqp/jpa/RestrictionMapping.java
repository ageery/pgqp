package org.pgqp.jpa;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Maps a field in a bean to a restriction definition and also specifies how to
 * determine whether the field is considered to contain a restriction (e.g., not
 * null).
 * 
 * @param <C>
 *            type of the criteria
 * @param <P>
 *            type of the parent table
 * @param <T>
 *            type of the child table
 * @param <F>
 *            type of the column
 * @param <V>
 *            type of the restriction value
 */
public class RestrictionMapping<C, P, T, F, V> {

	private Predicate<C> hasValuePredicate;
	private Function<C, V> getter;
	private RestrictionDefinition<P, T, F, V> restrictionDefinition;

	public RestrictionMapping(Function<C, V> getter, RestrictionDefinition<P, T, F, V> restrictionDefinition) {
		this(c -> Objects.nonNull(getter.apply(c)), getter, restrictionDefinition);
	}

	public RestrictionMapping(Predicate<C> hasValuePredicate, Function<C, V> getter,
			RestrictionDefinition<P, T, F, V> restrictionDefinition) {
		this.hasValuePredicate = hasValuePredicate;
		this.getter = getter;
		this.restrictionDefinition = restrictionDefinition;
	}

	public Predicate<C> getHasValuePredicate() {
		return hasValuePredicate;
	}

	public Function<C, V> getGetter() {
		return getter;
	}

	public RestrictionDefinition<P, T, F, V> getRestrictionDefinition() {
		return restrictionDefinition;
	}

	public RestrictionValue<P, T, F, V> toRestrictionValue(C c) {
		return new RestrictionValue<>(restrictionDefinition,
				Optional.ofNullable(hasValuePredicate.test(c) ? getter.apply(c) : null));
	}
}
