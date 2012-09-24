package org.kwince.contribs.osem.app;

import org.kwince.contribs.osem.annotations.hooks.PostOsemRead;
import org.kwince.contribs.osem.annotations.hooks.PostOsemSave;
import org.kwince.contribs.osem.annotations.hooks.PreOsemRead;
import org.kwince.contribs.osem.annotations.hooks.PreOsemSave;
import org.kwince.contribs.osem.model.Company;

public class CompanyMessage {
	
	@PreOsemSave
	public void preCreate(Company obj) {
		System.out.println("pre create: " + obj);
	}
	
	@PostOsemSave
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