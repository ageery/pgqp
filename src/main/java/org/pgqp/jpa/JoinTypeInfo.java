package org.pgqp.jpa;

import java.util.function.BinaryOperator;

class JoinTypeInfo {

	public static final BinaryOperator<JoinTypeInfo> MERGE_FUNCTION = (a,b) -> a.merge(b);
	
	private boolean innerJoin;
	private boolean fetchJoin;
	
	public JoinTypeInfo(boolean innerJoin, boolean fetchJoin) {
		this.innerJoin = innerJoin;
		this.fetchJoin = fetchJoin;
	}

	public boolean isInnerJoin() {
		return innerJoin;
	}
	
	public boolean isFetchJoin() {
		return fetchJoin;
	}
	
	public JoinTypeInfo merge(JoinTypeInfo other) { 
		return new JoinTypeInfo(this.innerJoin && other.innerJoin, this.fetchJoin || other.fetchJoin);
	}
	
}
