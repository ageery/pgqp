package org.pgqp.jpa;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class JoinInfo<P, C> {

	private Fetch<P, C> fetch;
	private From<P, C> from;
	
	public JoinInfo(Fetch<P, C> fetch) {
		this.fetch = fetch;
	}
	
	public JoinInfo(From<P, C> from) {
		this.from = from;
	}
	
	public Fetch<P, C> getFetch() {
		return fetch;
	}

	public From<P, C> getFrom() {
		return from;
	}

	public boolean isFetch() {
		return fetch != null;
	}
	
	@SuppressWarnings("unchecked")
	public From<P,C> toFrom() {
		return from != null ? from : (From<P,C>) fetch;
	}
	
	public <X> Path<X> get(SingularAttribute<C, X> singularAttribute) {
		return toFrom().get(singularAttribute);
	}
	
	@SuppressWarnings("unchecked")
	public Path<C> toPath() {
		return from != null ? from : (Path<C>) fetch;
	}
	
}
