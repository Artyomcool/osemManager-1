package org.kwince.contribs.osem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.event.EventDispatcher;

public class CallbackCreateTest {
	
	OsemMangerFactory factory;
	OsemManager osem;
    EmployeeCreate emp;
    String id;
    
    @Before
    public void setUp() {
    	factory = new OsemMangerFactory();
    	
    	factory.setElastic(new ElasticClientFactory()
    			.setClusterName("elasticsearch")
    			.setClientTransportSniff(true)
    			.setHost("localhost")
    			.setPort("9300"));
    	
    	EventDispatcher dispatcher = new EventDispatcher();
    	factory.setDispatcher(dispatcher);
    	
    	dispatcher.addHandler(EmployeeCreate.class, new Callback());
    	
    	osem = factory.createOsemManager();
        emp = new EmployeeCreate();
    	id = String.valueOf(new Date().getTime());
    	emp.setId(id);
    	resetStatus();
    }
    
    @After
    public void cleanUp() {
    	System.out.println("======================================= clean up");
        osem.delete(emp);
    	osem.close();
        System.out.println("======================================= cleaned up");
    }
    
    @Test
    public void PRE_CREATE_TEST()
    {
    	assertFalse(Callback.preCreate);
        emp.setName("Thatcher");
        EmployeeCreate result = (EmployeeCreate) osem.create(emp);
        assertTrue(Callback.preCreate);
    
        Assert.assertNotNull(result);
        
        System.out.println(">>>>>>>>> Success - test 'PRE_CREATE_TEST' <<<<<<<<<");
    }
    
    @Test
    public void POST_CREATE_TEST()
    {
    	assertFalse(Callback.postCreate);
        emp.setName("Thatcher I");
        EmployeeCreate result = (EmployeeCreate) osem.create(emp);
        assertTrue(Callback.postCreate);
    
        Assert.assertNotNull(result);
        
        System.out.println(">>>>>>>>> Success - test 'POST_CREATE_TEST' <<<<<<<<<");
    }
    
    void resetStatus() {
    	Callback.preCreate = false;
    	Callback.postCreate = false;
    }
}
