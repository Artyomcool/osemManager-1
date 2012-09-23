package org.kwince.contribs.osem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.annotations.Lazy;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.dao.SearchResult;
import org.kwince.contribs.osem.event.EventDispatcher;

public class LazyTest {
	
	public static class A{
		
		@Id
		private String id;
		
		@Lazy
		private B lazyField;
		
		public B getLazyField(){
			return lazyField;
		}
		
		public void setLazyField(B field){
			this.lazyField = field;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
		
	}
	
	public static class B{
		
		@Id
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
		
	}
	
	private static int c = 0;
	
	public static class C{
		
		@Id
		private String id;
		
		public C(){
			c++;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
	
	public static class D{
				
		@Id
		private String id;

		@Lazy
		private List<C> lazyList = new ArrayList<C>();
		
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
	
	static OsemManager osem;
	
	private List<C> lst;
	
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
		A a = new A();
		a.lazyField = new B();
        osem.save(a,true);
        
        lst = new ArrayList<C>();
        D d = new D();
        for(int i = 0;i<10;i++){
        	C c = new C();
        	lst.add(c);
        	d.lazyList.add(c);
        }
        osem.save(d,true);
        
		osem.dropCache();
    }
	
	@After
    public void cleanUp() {
		for(A e:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class).result())
			osem.delete(e,true);
		for(D e:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, D.class).result())
			osem.delete(e,true);
    }
	
	@AfterClass
	public static void cleanUpGlobal(){
    	osem.close();
	}
	
	@Test
	public void lazy_loading() {
		SearchResult<A> result = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class);
				
		A a = result.result().get(0);
		assertNull(a.lazyField);
		
		B b = a.getLazyField();
		assertNotNull(b);
		assertSame(b, a.lazyField);
    }
	
	
	@Test
	public void lazy_save() {
		SearchResult<A> result = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class);
				
		A a = result.result().get(0);
		B b = a.getLazyField();
		
		a = osem.save(a);
		osem.dropCache();
		
		a = osem.read(a.getId(), A.class);
		B b2 = a.getLazyField();
	
		assertEquals(b.getId(),b2.getId());
		
		osem.dropCache();
		a = osem.read(a.getId(), A.class);
		osem.save(a);
		osem.dropCache();
		
		a = osem.read(a.getId(), A.class);
		b2 = a.getLazyField();
		
		assertEquals(b.getId(),b2.getId());
		
    }
	
	
	@Test
	public void lazy_collection(){
		c = 0;
		SearchResult<D> result = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, D.class);
		
		D d = result.result().get(0);
		assertEquals(c, 0);
		
		C c1 = d.lazyList.get(5);
		assertEquals(c1.getId(),lst.get(5).getId());
		assertEquals(c, 1);
		
		for(int i=0;i<d.lazyList.size();i++)
			assertEquals(d.lazyList.get(i).getId(),lst.get(i).getId());
		assertEquals(c, 10);
	}
	
}