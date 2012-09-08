package org.kwince.contribs.osem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.dao.SearchResult;
import org.kwince.contribs.osem.event.EventDispatcher;

public class IndexTest {
	
	static OsemManager osem;
	Employee emp;
	
	@BeforeClass
	public static void setUpGlobal(){
		OsemMangerFactory factory = new OsemMangerFactory();
    	

    	factory.setElastic(new ElasticClientFactory()
		.setProperties("classpath:osem.properties"));
    	
    	factory.setDispatcher(new EventDispatcher());
    	
		osem = factory.createOsemManager();
	}
	
	@Before
    public void setUp() {
        emp = osem.save(new Employee("John"),true);
    }
	
	@After
    public void cleanUp() {
		for(Employee e:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, Employee.class).result())
			osem.delete(e,true);
    }
	
	@AfterClass
	public static void cleanUpGlobal(){
    	osem.close();
	}
	
	@Test
	public void INDEX_EXIST() {
		SearchResult<Employee> result = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, Employee.class);
				
		assertNotNull(result);
		assertEquals(result.total(), 1);
		assertEquals(result.result().get(0).getId(),emp.getId());
    }
	
	@Test
	public void INDEX_EXIST_2() {
		Employee emp2 = osem.read(emp.getId(), Employee.class);
		assertEquals(emp2.getId(),emp.getId());
    }
		
	@Test(expected=IndexMissingException.class)
	public void INDEX_NOT_EXIST() {

	    QueryBuilder query = QueryBuilders.matchAllQuery();

    	osem.find(query, 0, 1000, Car.class);
    }
	
	static class Car {
		@Id
		private String id;
		private String name;
		
		public Car() {
		}
		
		public Car(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

}
