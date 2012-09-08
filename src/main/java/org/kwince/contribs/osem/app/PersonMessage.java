package org.kwince.contribs.osem.app;

import org.kwince.contribs.osem.annotations.PostOsemSave;
import org.kwince.contribs.osem.annotations.PostOsemRead;
import org.kwince.contribs.osem.annotations.PreOsemSave;
import org.kwince.contribs.osem.annotations.PreOsemRead;
import org.kwince.contribs.osem.model.Person;

public class PersonMessage {	
	@PreOsemSave
	public void preCreate(Person obj) {}
	
	@PostOsemSave
	public void postCreate(Person obj) {}
	
	@PreOsemRead
	public void preRead(String id) {}
	
	@PostOsemRead
	public void postRead(Person obj) {}
	
}