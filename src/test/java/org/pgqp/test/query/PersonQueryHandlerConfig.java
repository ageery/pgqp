package org.pgqp.test.query;

import static java.util.Arrays.asList;
import static org.pgqp.jpa.JpaCriteriaHandlers.CONTAINS_FIELD_HANDLER;
import static org.pgqp.jpa.JpaCriteriaHandlers.LIKE_FIELD_HANDLER;
import static org.pgqp.jpa.JpaCriteriaHandlers.STRING_FIELD_HANDLER;
import static org.pgqp.jpa.JpaCriteriaHandlers.notNullFieldHandler;
import static org.pgqp.test.entity.Business_.address;
import static org.pgqp.test.entity.Business_.name;
import static org.pgqp.test.entity.Person_.birthdate;
import static org.pgqp.test.entity.Person_.employer;
import static org.pgqp.test.entity.Person_.firstName;
import static org.pgqp.test.entity.Person_.lastName;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.pgqp.QueryHandler;
import org.pgqp.jpa.AttributeInfo;
import org.pgqp.jpa.JoinDefinition;
import org.pgqp.jpa.JpaQueryHandler;
import org.pgqp.jpa.RestrictionDefinition;
import org.pgqp.jpa.RestrictionMapping;
import org.pgqp.jpa.SortDefinition;
import org.pgqp.test.entity.Business;
import org.pgqp.test.entity.Business_;
import org.pgqp.test.entity.Person;
import org.pgqp.test.entity.Person_;

public class PersonQueryHandlerConfig {

	private JoinDefinition<?, Person> PERSON_TABLE = new JoinDefinition<>("person", Person.class);
	private JoinDefinition<Person, Business> BUSINESS_TABLE = new JoinDefinition<>("business", Business.class,
			PERSON_TABLE, new AttributeInfo<>(Person_.employer));
	private JoinDefinition<Business, Person> BUSINESS_OWNER_TABLE = new JoinDefinition<>("owner", Person.class,
			BUSINESS_TABLE, new AttributeInfo<>(Business_.owner));
	private JoinDefinition<Person, Person> CHILD_TABLE = new JoinDefinition<>("child", Person.class, PERSON_TABLE,
			new AttributeInfo<>(Person_.children));

	private List<RestrictionMapping<PersonCriteria, ?, ?, ?, ?>> getRestrictions() {
		return asList(
				new RestrictionMapping<>(PersonCriteria::getFirstName, 
						new RestrictionDefinition<>(PERSON_TABLE, firstName, CONTAINS_FIELD_HANDLER)),
				new RestrictionMapping<>(pc -> pc.getLastName() != null && pc.getLastName().hasValue(), 
						PersonCriteria::getLastName, 
						new RestrictionDefinition<>(PERSON_TABLE, lastName, STRING_FIELD_HANDLER)),
				new RestrictionMapping<>(PersonCriteria::getCompanyName, 
						new RestrictionDefinition<>(BUSINESS_TABLE, name, LIKE_FIELD_HANDLER)),
				new RestrictionMapping<>(PersonCriteria::getCompanyAddress, 
						new RestrictionDefinition<>(BUSINESS_TABLE, address, LIKE_FIELD_HANDLER)),
				new RestrictionMapping<>(PersonCriteria::getOwnerName, 
						new RestrictionDefinition<>(BUSINESS_OWNER_TABLE, lastName, LIKE_FIELD_HANDLER)),
				new RestrictionMapping<>(PersonCriteria::getChildName, 
						new RestrictionDefinition<>(CHILD_TABLE, firstName, LIKE_FIELD_HANDLER)),
				new RestrictionMapping<>(PersonCriteria::getEmployed, 
						new RestrictionDefinition<>(PERSON_TABLE, employer, notNullFieldHandler(Business.class))),
				new RestrictionMapping<>(PersonCriteria::getHasChildren, 
						new RestrictionDefinition<>(PERSON_TABLE, Person_.id, (context, value) -> {
							Subquery<Integer> sq = context.getQuery().subquery(Integer.class);
							Root<Person> root = sq.from(Person.class);
							sq.select(root.get(Person_.parent).get(Person_.id))
								.where(root.get(Person_.parent).isNotNull());
							In<Integer> in = context.getCriteriaBuilder().in(context.getPath()).value(sq);
							return value ? in : in.not();
						})),
				new RestrictionMapping<>(PersonCriteria::getUnderageChildren,
						new RestrictionDefinition<>(CHILD_TABLE, birthdate,
								(context, value) -> value
										? context.getCriteriaBuilder().greaterThan(context.getPath(),
												LocalDate.now().minusYears(18))
										: context.getCriteriaBuilder().lessThan(context.getPath(),
												LocalDate.now().minusYears(18)))
						));
	}

	private Collection<SortDefinition<PersonSort, ?, ?>> getSorts() {
		return asList(
				new SortDefinition<>(PersonSort.FIRST_NAME, PERSON_TABLE, firstName),
				new SortDefinition<>(PersonSort.LAST_NAME, PERSON_TABLE, lastName),
				new SortDefinition<>(PersonSort.BUSINESS_NAME, BUSINESS_TABLE, name),
				new SortDefinition<>(PersonSort.OWNER_LAST_NAME, BUSINESS_OWNER_TABLE, lastName));
	}
	
	public QueryHandler<CriteriaQuery<Person>, CriteriaQuery<Long>, Person, PersonCriteria, PersonSort> getPersonQueryHandler(EntityManager entityManager) {
		return new JpaQueryHandler<>(entityManager, Person.class, Integer.class, r -> r.get(Person_.id),
				getRestrictions(), getSorts());
	}
	
}
