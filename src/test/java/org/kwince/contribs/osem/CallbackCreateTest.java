package org.kwince.contribs.osem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import junit.framework.Assert;

import org.elasticsearch.index.query.QueryBuilders;
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
    			.setProperties("classpath:osem.properties"));
    	
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
		for(EmployeeCreate e:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, EmployeeCreate.class).result())
			osem.delete(e,true);
    	osem.close();
    }
    
    @Test
    public void PRE_CREATE_TEST()
    {
    	assertFalse(Callback.preCreate);
        emp.setName("Thatcher");
        EmployeeCreate result = osem.save(emp,true);
        assertTrue(Callback.preCreate);
    
        Assert.assertNotNull(result);
    }
    
    @Test
    public void POST_CREATE_TEST()
    {
    	assertFalse(Callback.postCreate);
        emp.setName("Thatcher I");
        EmployeeCreate result = (EmployeeCreate) osem.save(emp,true);
        assertTrue(Callback.postCreate);
    
        Assert.assertNotNull(result);
    }
    
    void resetStatus() {
    	Callback.preCreate = false;
    	Callback.postCreate = false;
    }
}
