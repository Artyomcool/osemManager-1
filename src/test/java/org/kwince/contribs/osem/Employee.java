package org.kwince.contribs.osem;

import org.kwince.contribs.osem.annotations.Id;

public class Employee {
    @Id
	private String id;
	private String name;
		
	public Employee(String name) { this.name = name; }

	public Employee() { }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
		
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
}