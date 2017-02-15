package org.pgqp.jpa.entity;

import java.time.LocalDate;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

@Data
@Entity
public class Person {

	@Id
	private Integer id;

	private String firstName;

	private String lastName;

	private LocalDate birthdate;

	@ManyToOne
	private Person parent;
	
	@ManyToOne
	private Business employer;

	@OneToMany(mappedBy = "parent")
	private Collection<Person> children;

	public Person() {
		super();
	}

	public Person(Integer id, String firstName, String lastName, LocalDate birthdate) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthdate = birthdate;
	}

}
