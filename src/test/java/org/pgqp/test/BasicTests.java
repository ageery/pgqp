package org.pgqp.test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Before;
import org.junit.Test;
import org.pgqp.CriteriaField;
import org.pgqp.QueryDefinition;
import org.pgqp.QueryHandler;
import org.pgqp.SortInfo;
import org.pgqp.SortInfo.Direction;
import org.pgqp.StandardOperation;
import org.pgqp.test.entity.Business;
import org.pgqp.test.entity.Person;
import org.pgqp.test.query.PersonCriteria;
import org.pgqp.test.query.PersonQueryHandlerConfig;
import org.pgqp.test.query.PersonSort;

public class BasicTests {

	private EntityManager entityManager;
	private QueryHandler<CriteriaQuery<Person>, CriteriaQuery<Long>, Person, PersonCriteria, PersonSort> queryHandler;
	
	private Business acme;
	private Business hal;
	private Business tree;
	
	private Person bob;
	private Person molly;
	private Person brian;
	private Person suzy;
	private Person bill;
	private Person bert;
	private Person wendy;
	private Person ernie;
	
	@Before
	public void config() {
		HibernatePersistenceProvider provider = new HibernatePersistenceProvider();
		EntityManagerFactory emf = provider.createEntityManagerFactory("test", null);
		entityManager = emf.createEntityManager();
		queryHandler = new PersonQueryHandlerConfig().getPersonQueryHandler(entityManager);
		createData();
	}

	private void createData() {
		entityManager.getTransaction().begin();
		
		acme = new Business(1, "Acme", "NY");
		entityManager.persist(acme);
		hal = new Business(2, "HAL", "CA");
		entityManager.persist(hal);
		tree = new Business(3, "Tree Corp", null);
		
		bob = new Person(1, "Bob", "Smith", LocalDate.now().minusYears(40));
		bob.setEmployer(acme);
		entityManager.persist(bob);
		
		molly = new Person(2, "Molly", "Smith", LocalDate.now().minusYears(15));
		molly.setParent(bob);
		entityManager.persist(molly);
		
		brian = new Person(3, "Brian", "Smith", LocalDate.now().minusYears(17));
		brian.setParent(bob);
		entityManager.persist(brian);
		
		suzy = new Person(4, "Suzy", "Johnson", LocalDate.now().minusYears(62));
		suzy.setEmployer(acme);
		entityManager.persist(suzy);
		acme.setOwner(suzy);
		
		bill = new Person(5, "Bill", "Johnson", LocalDate.now().minusYears(5));
		bill.setParent(suzy);
		entityManager.persist(bill);
		
		bert = new Person(6, "Bert", "Apple", LocalDate.now().minusYears(65));
		bert.setEmployer(tree);
		entityManager.persist(bert);
		wendy = new Person(7, "Wendy", "Apple", LocalDate.now().minusYears(42));
		wendy.setEmployer(tree);
		wendy.setParent(bert);
		entityManager.persist(wendy);
		tree.setOwner(wendy);
		
		ernie = new Person(8, "Ernie", "Banana", LocalDate.now().minusYears(58));
		ernie.setEmployer(hal);
		entityManager.persist(ernie);
		
		entityManager.persist(tree);
		entityManager.getTransaction().commit();
	}
	
	@Test
	public void testSortUsesOuterJoin() {
		/*
		 * Note: we want to do an entity search, not just a count query, to show that it's using an outer join.
		 */
		assertEquals(8, entityManager.createQuery(
				queryHandler.toEntityQuery(new QueryDefinition<>(
						new PersonCriteria(), 
						PersonSort.BUSINESS_NAME)))
				.getResultList()
				.size());
	}
	
	@Test
	public void testContains() {
		assertEquals(1, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria().setFirstName("ill"))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testEquals() {
		assertEquals(3, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria().setLastName(new CriteriaField<String>(StandardOperation.EQ, "Smith")))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testDescSort() {
		List<Person> list = entityManager.createQuery(
				queryHandler.toEntityQuery(new QueryDefinition<>(
						new PersonCriteria().setLastName(new CriteriaField<String>(StandardOperation.EQ, "Smith")),
						Arrays.asList(new SortInfo<>(PersonSort.FIRST_NAME, Direction.DESC)))))
				.getResultList();
		assertEquals(2, list.get(0).getId().intValue());
		assertEquals(3, list.get(1).getId().intValue());
		assertEquals(1, list.get(2).getId().intValue());
	}
	
	@Test
	public void testInnerJoin() {
		assertEquals(1, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria().setCompanyName("HAL"))))
				.getSingleResult()
				.intValue());
	}

	@Test
	public void testNotNullCriteria() {
		assertEquals(5, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria().setEmployed(true))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testMultiFieldMultiTableSort() {
		List<Person> list = entityManager.createQuery(
				queryHandler.toEntityQuery(new QueryDefinition<>(
						new PersonCriteria().setEmployed(true), 
						PersonSort.BUSINESS_NAME, PersonSort.FIRST_NAME, PersonSort.LAST_NAME)))
				.getResultList();
		assertEquals(1, list.get(0).getId().intValue());
		assertEquals(4, list.get(1).getId().intValue());
	}
	
	// FIXME: it looks like we're running into a situation where we're doing a join with the children table too...
	@Test
	public void testOneToManyCount() {
		assertEquals(2, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria().setUnderageChildren(true))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testManualInClause() {
		assertEquals(3, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria()
							.setHasChildren(true))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testManualNotInClause() {
		assertEquals(5, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria()
							.setHasChildren(false))))
				.getSingleResult()
				.intValue());
	}

	@Test
	public void testMultipleRestrictionsOnSameTable() {
		assertEquals(1, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria()
							.setFirstName("Bob")
							.setLastName(new CriteriaField<String>(StandardOperation.EQ, "Smith")))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testRestrictionsOnDifferentTables() {
		assertEquals(1, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria()
							.setFirstName("Bob")
							.setCompanyName("Acme"))))
				.getSingleResult()
				.intValue());
	}
	
	@Test
	public void testNonNullRestrictionParameter() {
		/*
		 * Note: the purpose of this test is to ensure that the query engine sees the last name criteria as not existing.
		 */
		assertEquals(8, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria()
							.setLastName(new CriteriaField<String>(StandardOperation.EQ, null)))))
				.getSingleResult()
				.intValue());
	}
	
	// FIXME: do we want to make it configurable as to what should happen in this situation?
	@Test
	public void testMissingSortMapping() {
		/*
		 * Note: we just want to check that this does not throw an exception.
		 */
		entityManager.createQuery(
				queryHandler.toEntityQuery(new QueryDefinition<>(
						new PersonCriteria(), 
						PersonSort.NO_MAPPING)))
				.getResultList();
	}
	
	@Test
	public void testMissingCriteriaMapping() {
		/*
		 * Note: since there is no restriction on the query, all of the results are returned.
		 */
		assertEquals(8, entityManager.createQuery(
				queryHandler.toCountQuery(new QueryDefinition<>(
						new PersonCriteria().setNotMapped("test"))))
				.getSingleResult()
				.intValue());
	}

}
