package org.kwince.contribs.osem.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.kwince.contribs.osem.annotations.Id;
import org.kwince.contribs.osem.annotations.Syntetic;
import org.kwince.contribs.osem.annotations.mapping.Mapping;
import org.kwince.contribs.osem.annotations.mapping.StringParam;
import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.dao.OsemManager;
import org.kwince.contribs.osem.dao.OsemMangerFactory;
import org.kwince.contribs.osem.event.EventDispatcher;
import org.kwince.contribs.osem.model.Address;
import org.kwince.contribs.osem.model.Company;
import org.kwince.contribs.osem.model.Document;
import org.kwince.contribs.osem.model.Person;

public class App 
{
	
	private static ElasticClientFactory getElastic(){
		
		return new ElasticClientFactory()
			.setProperties("classpath:osem.properties");
	}
	
	public static class TestA{
		
		@Id
		private String id;
		@Mapping(
				stringParams={
						@StringParam(name="store",value="yes")
				}
		)
		private double data = Math.random();
		private List<TestB> children = new ArrayList<App.TestB>();
		
		@Syntetic("myField")
		@Mapping(
				stringParams={
						@StringParam(name="store",value="yes")
				}
		)
		@SuppressWarnings("unused")
		private int getSynteticField(){
			return (int) (data*100);
		}
		
		public void add(TestB b){
			children.add(b);
			b.parent = this;
		}

		@Override
		public String toString() {
			return "TestA [id=" + id + ", data=" + data + "]";
		}
		
	}
	
	public static class TestB{
		
		@Id
		private String id;
		private double test = Math.random();
		private TestA parent;
		@Override
		public String toString() {
			return "TestB [id=" + id + ", test=" + test + ", parent=" + parent
					+ "]";
		}
	}
		
    public static void main( String[] args )
    {
    	OsemMangerFactory factory = new OsemMangerFactory();
    	factory.setElastic(getElastic());
    	
    	EventDispatcher d = new EventDispatcher();
    	factory.setDispatcher(d);
    	
    	d.addHandler(Person.class, new PersonMessage());
    	d.addHandler(Company.class, new CompanyMessage());
    	
    	OsemManager osem = factory.createOsemManager();
    	
    	Person p1 = new Person();
    	p1.setFirstName("p1 first name");
	    p1 = osem.save(p1);
	    p1 = osem.read(p1.getId(), Person.class);
	        	    
		Company c1 = new Company();
        c1.setName("The Company");
        c1.setAddress(new Address("D Rd.", "Paris", "TX", "77382"));
        c1 = osem.save(c1);
        
        List<Person> people = new ArrayList<Person>();
        people.add(p1);
        people.add(p1);
        c1.setEmployees(people);
        c1 = osem.save(c1,true); //force to refresh
        
        p1.setFirstName(p1.getFirstName() + " update" + new Date().getTime());
        p1 = osem.save(p1,true); //force to refresh

	    QueryBuilder query = QueryBuilders.matchAllQuery();

	    System.out.println("first find for persons: "+osem.find(query, 0, 1000, Person.class).result());
	    System.out.println("first find for companies: "+osem.find(query, 0, 1000, Company.class).result());
	    
	    osem.delete(c1,true); //force to refresh
	    
	    System.out.println("second find for persons: "+osem.find(query, 0, 1000, Person.class).result());
	    System.out.println("second find for companies: "+osem.find(query, 0, 1000, Company.class).result());
	    
	    
	    //Same with TestA and TestB
	    TestA a = new TestA();
	    for(int i=0;i<10;i++)
	    	a.add(new TestB());
	    
	    a = osem.save(a);

	    osem.delete(a);
	    
        osem.close();
		
    }
    
    
    static void test1() throws Exception {
    	OsemMangerFactory factory = new OsemMangerFactory();
    	factory.setElastic(getElastic());
    	
    	OsemManager osem = factory.createOsemManager();
    	Document doc = new Document();
        doc.setName("myName");
        doc.setVendor("elasticsearch");
        
        doc = (Document) osem.save(doc);
        osem.close();
    }
  
    
}
