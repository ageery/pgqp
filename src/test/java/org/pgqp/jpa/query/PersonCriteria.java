package org.pgqp.jpa.query;

import org.pgqp.CriteriaField;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PersonCriteria {

	private String firstName;
	private CriteriaField<String> lastName;
	private String companyName;
	private String companyAddress;
	private String ownerName;
	private String childName;
	private Boolean underageChildren;
	private Boolean employed;
	private Boolean hasChildren;
	private String notMapped;
	
}
