package org.kwince.contribs.osem.app;

import org.kwince.contribs.osem.annotations.PostOsemCreate;
import org.kwince.contribs.osem.annotations.PostOsemRead;
import org.kwince.contribs.osem.annotations.PreOsemCreate;
import org.kwince.contribs.osem.annotations.PreOsemRead;
import org.kwince.contribs.osem.model.Company;

public class CompanyMessage {
	
	@PreOsemCreate
	public void preCreate(Company obj) {
		System.out.println("pre create: " + obj);
	}
	
	@PostOsemCreate
	public void postCreate(Company obj) {
		System.out.println("post create: " + obj);
	}
	
	@PreOsemRead
	public void preRead(String id) {
		System.out.println("**************** @PreOsemRead **************");
	}
	
	@PostOsemRead
	public void postRead(Company obj) {
		System.out.println("post read: " + obj);
	}
}