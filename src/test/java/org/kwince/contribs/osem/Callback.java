package org.kwince.contribs.osem;

import org.kwince.contribs.osem.annotations.hooks.PostOsemDelete;
import org.kwince.contribs.osem.annotations.hooks.PostOsemRead;
import org.kwince.contribs.osem.annotations.hooks.PostOsemSave;
import org.kwince.contribs.osem.annotations.hooks.PreOsemDelete;
import org.kwince.contribs.osem.annotations.hooks.PreOsemRead;
import org.kwince.contribs.osem.annotations.hooks.PreOsemSave;

public class Callback {
	
	public static boolean preCreate = false;
    public static boolean postCreate = false;
    
    public static boolean preRead = false;
	public static boolean postRead = false;
        
	public static boolean preDelete = false;
    public static boolean postDelete = false;
        
    @PreOsemSave
    public void preCreate(Object object) {
    	preCreate = true;
    }
    	
    @PostOsemSave
    public void postCreate(Object object) {
    	postCreate = true;
    }
    
    @PreOsemRead
    public void preRead(String id) {
       	preRead = true;
    }
    	
    @PostOsemRead
    public void postRead(Object object) {
    	postRead = true;
    }
    
    @PreOsemDelete
    public void preDelete(Object object) {
    	preDelete = true;
    }
    	
    @PostOsemDelete
    public void postDelete(Object object) {
    	postDelete = true;
    }
}