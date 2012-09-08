package org.kwince.contribs.osem;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
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

public class CyclicTest {
	
	static OsemManager osem;
	
	static class A{
		@Id
		String id;
		int num;
		List<B> b = new ArrayList<B>();
		C c;
		
		public A(){}
		
		A(int num){
			this.num = num;
		}
	}
	
	static class B{
		@Id
		String id;
		List<C> c = new ArrayList<C>();
		A a;
	}
	
	static class C{
		@Id
		String id;
		List<A> a = new ArrayList<A>();
		B b;
	}
	
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
    }
	
	@After
    public void cleanUp() {
		for(A a:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class).result())
			osem.delete(a,true);
		
		for(B a:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, B.class).result())
			osem.delete(a,true);
		
		for(C a:osem.find(QueryBuilders.matchAllQuery(), 0, 1000, C.class).result())
			osem.delete(a,true);
    }
	
	@AfterClass
	public static void cleanUpGlobal(){
    	osem.close();
	}
	
	@Test
	public void ONE_DIRECTION() {
		//PREPARE
		int a_count = 0;
		A a = new A(a_count++);
		for(int i=0;i<3;i++){
			B b = new B();
			for(int j=0;j<3;j++){
				C c = new C();
				for(int k=0;k<3;k++)
					c.a.add(new A(a_count++));
				b.c.add(c);
			}
			a.b.add(b);
		}
		
		a = osem.save(a, true);
		
		//CHECK LOAD
		List<A> list = new ArrayList<A>(
				osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class)
					.result());
				
		assertEquals(list.size(),a_count);
		
		Collections.sort(list, new Comparator<A>() {
			@Override
			public int compare(A o1, A o2) {
				return o1.num-o2.num;
			}
		});

		for(int i=0;i<a_count;i++)
			assertEquals(list.get(i).num, i);
		
		A root = list.get(0);
		assertEquals(a.id,root.id);
		
		assertEquals(a.b.size(),3);
		
		for(B b:root.b){
			assertEquals(b.c.size(),3);
			for(C c:b.c)
				assertEquals(c.a.size(),3);
		}
		
		//CHECK DELETE
		osem.delete(root, true);

		long count = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class).total();
		assertEquals(count,0);
		count = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, B.class).total();
		assertEquals(count,0);
		count = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, C.class).total();
		assertEquals(count,0);
	}
	
	@Test
	public void TWO_DIRECTIONS() {
		//PREPARE
		int a_count = 0;
		A a = new A(a_count++);
		for(int i=0;i<3;i++){
			B b = new B();
			b.a = a;
			for(int j=0;j<3;j++){
				C c = new C();
				c.b = b;
				for(int k=0;k<3;k++) {
					A ta = new A(a_count++);
					ta.c = c;
					c.a.add(ta);
				}
				b.c.add(c);
			}
			a.b.add(b);
		}
		
		a = osem.save(a, true);
		
		//CHECK LOAD
		List<A> list = new ArrayList<A>(
				osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class)
					.result());
				
		assertEquals(list.size(),a_count);
		
		Collections.sort(list, new Comparator<A>() {
			@Override
			public int compare(A o1, A o2) {
				return o1.num-o2.num;
			}
		});

		for(int i=0;i<a_count;i++)
			assertEquals(list.get(i).num, i);
		
		A root = list.get(0);
		assertEquals(a.id,root.id);
		
		assertEquals(a.b.size(),3);
		
		for(B b:root.b){
			assertEquals(b.a,root);
			assertEquals(b.c.size(),3);
			for(C c:b.c){
				assertEquals(c.b,b);
				assertEquals(c.a.size(),3);
				for(A ta:c.a)
					assertEquals(ta.c,c);
			}
		}
		
		//CHECK DELETE
		osem.delete(root, true);

		long count = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, A.class).total();
		assertEquals(count,0);
		count = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, B.class).total();
		assertEquals(count,0);
		count = osem.find(QueryBuilders.matchAllQuery(), 0, 1000, C.class).total();
		assertEquals(count,0);
	}

}
