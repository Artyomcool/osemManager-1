package org.kwince.contribs.osem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.event.EventDispatcher;
import org.kwince.contribs.osem.exceptions.OsemException;

public class IndexTest {
	
	static OsemManager osem;
	Employee emp;
	
	@BeforeClass
	public static void setUpGlobal(){
		OsemMangerFactory factory = new OsemMangerFactory();
    	

    	factory.setElastic(new ElasticClientFactory()
    			.setClusterName("elasticsearch")
    			.setClientTransportSniff(true)
    			.setHost("localhost")
    			.setPort("9300"));
    	
    	factory.setDispatcher(new EventDispatcher());
    	
		osem = factory.createOsemManager();
	}
	
	@Before
    public void setUp() {
        emp = osem.create(new Employee("John"));
    }
	
	@After
    public void cleanUp() {
		
		if (emp!=null) {
            osem.delete(emp);
        }
    }
	
	@AfterClass
	public static void cleanUpGlobal(){
    	osem.close();
	}
	
	@Test
	public void INDEX_EXIST() {
		final String expectedError = null;
		String actualError = null;
    	
		String query = "{\"match_all\": {}}";
	    List<Employee> list = null;
	    
		try {
			list = osem.find(query, Employee.class);
		} catch (OsemException actualException) {
			actualError = actualException.getMessage();
		}
		
		assertEquals(expectedError, actualError);
		assertNotNull(list);
		System.out.println(">>>>>>>>> Success - test 'INDEX_EXIST_2' <<<<<<<<<");
    }
	
	@Test
	public void INDEX_EXIST_2() {
		final String expectedError = null;
		String actualError = null;
		
		Employee emp2 = new Employee();
		try {
			emp2 = (Employee) osem.read(emp.getId(), Employee.class);
		} catch (OsemException actualException) {
			actualError = actualException.getMessage();
		}
		
		assertEquals(expectedError, actualError);
		assertNotNull(emp2);
		System.out.println(">>>>>>>>> Success - test 'INDEX_EXIST_2' <<<<<<<<<");
    }
		
	@Test
	public void INDEX_NOT_EXIST() {
    	
		String query = "{\"match_all\": {}}";
	    List<Car> list = null;
	    
		list = osem.find(query, Car.class);
			
		assertEquals(list.size(),0);
		System.out.println(">>>>>>>>> Success - test 'INDEX_NOT_EXIST' <<<<<<<<<");
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
