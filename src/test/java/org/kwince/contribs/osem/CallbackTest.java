package org.kwince.contribs.osem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        emp = osem.create(emp);
    	System.out.println("-->"+emp.getId());
        resetStatus();
    }
    
    @After
    public void cleanUp() {
    	System.out.println("======================================= clean up");
    	if (emp!=null) {
            osem.delete(emp);
        }
    	System.out.println("======================================= cleaned up");
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
        
        System.out.println(">>>>>>>>> Success - test 'PRE_READ_TEST' <<<<<<<<<");
    }
    
    @Test
    public void POST_READ_TEST() throws Exception
    {
    	assertFalse(Callback.postRead);
    	Employee result = osem.read(emp.getId()+"__123",Employee.class);
        assertTrue(Callback.postRead);
        
        Assert.assertNull(result);
        
        System.out.println(">>>>>>>>> Success - test 'POST_READ_TEST' <<<<<<<<<");
    }
    
    @Test
    public void PRE_UPDATE_TEST() throws Exception
    {
    	assertFalse(Callback.preUpdate);
        emp.setName("Thatcher");
        Employee result = (Employee) osem.update(emp);
        assertTrue(Callback.preUpdate);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(emp.getId(), result.getId());
        Assert.assertEquals("Thatcher", result.getName());
        
        System.out.println(">>>>>>>>> Success - test 'PRE_UPDATE_TEST' <<<<<<<<<");
    }
    
    @Test
    public void POST_UPDATE_TEST() throws Exception
    {
    	assertFalse(Callback.postUpdate);
    	emp.setId(emp.getId() + "123");
        emp.setName("Thatcher II");
        osem.update(emp);
        assertTrue(Callback.postUpdate);
                
        System.out.println(">>>>>>>>> Success - test 'POST_UPDATE_TEST' <<<<<<<<<");
    }
    
    @Test
    public void PRE_DELETE_TEST() throws Exception
    {
    	System.out.println("-->"+emp.getId());
    	assertFalse(Callback.preDelete);
        osem.delete(emp);
        assertTrue(Callback.preDelete);
                
        System.out.println(">>>>>>>>> Success - test 'PRE_DELETE_TEST' <<<<<<<<<");
    }
    
    @Test
    public void POST_DELETE_TEST() throws Exception
    {
    	assertFalse(Callback.postDelete);
    	emp.setId(emp.getId() + "123");
    	osem.delete(emp);
    	//TODO need to think about
        //assertTrue(Callback.postDelete);
        
        System.out.println(">>>>>>>>> Success - test 'POST_DELETE_TEST' <<<<<<<<<");
    }
    
    void resetStatus() {
    	Callback.preRead = false;
    	Callback.postRead = false;
        
    	Callback.preUpdate = false;
    	Callback.postUpdate = false;
            	
    	Callback.preDelete = false;
    	Callback.postDelete = false;
    }
}
