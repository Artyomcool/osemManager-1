package org.kwince.contribs.osem.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			.setClusterName("elasticsearch")
			.setNodeClient(false)
			.setNodeLocal(false);
	}
	
    public static void main( String[] args )
    {
        try {
// Testing permissions
//        	OsemMangerFactory factory = new OsemMangerFactory("my_osem", "elasticsearch");
        	OsemMangerFactory factory = new OsemMangerFactory();
        	factory.setElastic(getElastic());
        	
        	EventDispatcher d = new EventDispatcher();
        	factory.setDispatcher(d);
        	
        	d.addHandler(Person.class, new PersonMessage());
        	d.addHandler(Company.class, new CompanyMessage());
        	
        	OsemManager osem = factory.createOsemManager();
        	
        	Person p1 = new Person();
        	p1.setFirstName("p1 first name");
    	    p1 = osem.create(p1);
    	    System.out.println("p1 "+p1);
    	    p1 = osem.read(p1.getId(), Person.class);
    	    System.out.println("p1 "+p1);
    	        	    
    		Company c1 = new Company();
            c1.setName("The Company");
            c1.setAddress(new Address("D Rd.", "Paris", "TX", "77382"));
            c1 = osem.create(c1);
            
            List<Person> people = new ArrayList<Person>();
            people.add(p1);
            people.add(p1);
            c1.setEmployees(people);
            c1 = osem.create(c1);
            
            p1.setFirstName(p1.getFirstName() + " update" + new Date().getTime());
            p1 = osem.update(p1);

    	    String query = "{\"match_all\": {}}";
    	    
    	    System.out.println(query);
    	    List<Person> list = osem.find(query, Person.class);
    	    for(Person p : list) {
    	    	osem.delete(p);
    	    }
    	    List<Company> lst = osem.find(query, Company.class);
    	    for(Company p : lst) {
        	    System.out.println("c "+p);
    	    	osem.delete(p);
    	    }
            osem.close();
    		System.in.read();
		
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    static void test1() throws Exception {
    	OsemMangerFactory factory = new OsemMangerFactory();
    	factory.setElastic(getElastic());
    	
    	OsemManager osem = factory.createOsemManager();
    	Document doc = new Document();
        doc.setName("myName");
        doc.setVendor("elasticsearch");
        
        doc = (Document) osem.create(doc);
        osem.close();
    }
  
    
}
