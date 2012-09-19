package org.kwince.contribs.osem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.dao.SearchResult;
import org.kwince.contribs.osem.event.EventDispatcher;

public class ConistencyTest {
	
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
	public void READ() {
		Employee emp2 = osem.read(emp.getId(), Employee.class);
		assertSame(emp,emp2);
	}
	
	@Test
	public void FIND() {
		SearchResult<Employee> result = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, Employee.class);
						
		assertEquals(result.total(), 1);
		assertSame(result.result().get(0),emp);
	}
	
	@Test
	public void DELETE_CLEAR_CACHE() {
		osem.delete(emp,true);

		Employee emp2 = osem.read(emp.getId(), Employee.class);
		assertNull(emp2);
		
		osem.save(emp);
	}
	

}
