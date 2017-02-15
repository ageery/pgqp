package org.pgqp.jpa.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

@Data
@Entity
public class Business {

	@Id
	private Integer id;

	private String name;

	private String address;

	@ManyToOne
	private Person owner;

	@OneToMany(mappedBy = "employer")
	private Set<Person> employees;

	public Business() {
		super();
	}
	
	public Business(Integer id, String name, String address) {
		this.id = id;
		this.name = name;
		this.address = address;
	}
	
}
