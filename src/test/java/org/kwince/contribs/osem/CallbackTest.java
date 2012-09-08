package org.kwince.contribs.osem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.event.EventDispatcher;

public class CallbackTest {
	
	private static OsemManager osem;
    Employee emp;
    
    @BeforeClass
    public static void setUpGlobal(){
    	OsemMangerFactory factory = new OsemMangerFactory();

    	factory.setElastic(new ElasticClientFactory()
		.setProperties("classpath:osem.properties"));
    	
    	EventDispatcher dispatcher = new EventDispatcher();
    	factory.setDispatcher(dispatcher);
    	
    	dispatcher.addHandler(Employee.class, new Callback());
    	
    	osem = factory.createOsemManager();
    }
    
    @Before
    public void setUp() {
    	emp = new Employee();
    	String name = "Mary";
    	// prepare to read, update and delete
    	emp.setName(name);
        emp = osem.save(emp,true);
        resetStatus();
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
    public void PRE_READ_TEST() throws Exception
    {
    	assertFalse(Callback.preRead);
    	String id = emp.getId();
    	String name = emp.getName();
        Employee result = osem.read(emp.getId(), Employee.class);
        assertTrue(Callback.preRead);
        
        Assert.assertEquals(id, result.getId());
        Assert.assertEquals(name, result.getName());
        
    }
    
    @Test
    public void POST_READ_TEST() throws Exception
    {
    	assertFalse(Callback.postRead);
    	Employee result = osem.read(emp.getId()+"__123",Employee.class);
        assertTrue(Callback.postRead);
        
        Assert.assertNull(result);
        
    }
    
    @Test
    public void PRE_DELETE_TEST() throws Exception
    {
    	assertFalse(Callback.preDelete);
        osem.delete(emp,true);
        assertTrue(Callback.preDelete);
                
    }
    
    @Test
    public void POST_DELETE_TEST() throws Exception
    {
    	assertFalse(Callback.postDelete);
    	emp.setId(emp.getId() + "123");
    	osem.delete(emp);
    	//TODO need to think about
        //assertTrue(Callback.postDelete);
    }
    
    void resetStatus() {
    	Callback.preRead = false;
    	Callback.postRead = false;
                    	
    	Callback.preDelete = false;
    	Callback.postDelete = false;
    }
}
