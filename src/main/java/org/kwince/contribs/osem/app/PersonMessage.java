package org.kwince.contribs.osem.app;

import org.kwince.contribs.osem.annotations.PostOsemCreate;
import org.kwince.contribs.osem.annotations.PostOsemRead;
import org.kwince.contribs.osem.annotations.PostOsemUpdate;
import org.kwince.contribs.osem.annotations.PreOsemCreate;
import org.kwince.contribs.osem.annotations.PreOsemRead;
import org.kwince.contribs.osem.annotations.PreOsemUpdate;
import org.kwince.contribs.osem.model.Address;
import org.kwince.contribs.osem.model.Person;

public class PersonMessage {	
	@PreOsemCreate
	public void preCreate(Person obj) {}
	
	@PostOsemCreate
	public void postCreate(Person obj) {}
	
	@PreOsemRead
	public void preRead(String id) {}
	
	@PostOsemRead
	public void postRead(Person obj) {}
	
	@PreOsemUpdate
	public void preUpdate(Person obj) {}
	
	@PostOsemUpdate
	public void postUpdate(Person obj) {}
}