package org.kwince.contribs.osem.model;

import org.kwince.contribs.osem.annotations.Id;

public class Person {
    
	@Id
	private String id;
	
    private String firstName;
    private String middleInitial;
    private String lastName;
    
    private Address address;
 
    public Person() {
    }
 
    public Person(final String fn, final String mi, final String ln, final Address address) {
        setFirstName(fn);
        setMiddleInitial(mi);
        setLastName(ln);
        setAddress(address);
    }
 
    public String getFirstName() {
        return firstName;
    }
 
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
 
    public String getLastName() {
        return lastName;
    }
 
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
 
    public String getMiddleInitial() {
    	return middleInitial;
    }
 
    public void setMiddleInitial(final String middleInitial) {
        this.middleInitial = middleInitial;
    }
 
    public final Address getAddress() {
        return address;
    }
 
    public final void setAddress(final Address address) {
        this.address = address;
    }

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", firstName=" + firstName
				+ ", middleInitial=" + middleInitial + ", lastName=" + lastName
				+ ", address=" + address + "]";
	}
}