package com.osem.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.osem.common.OsemStores;
import com.osem.exceptions.OsemException;

public class OsemStoreInvalidDriverTest {

	@Before
	public void setUp() {
		TestingUtil.rename("META-INF/osem.json", "META-INF/osem1.json");
		TestingUtil.rename("META-INF/osem-invalidDriver.json", "META-INF/osem.json");
	}
	
	@After()
	public void retsoreJsonStores() {
		TestingUtil.rename("META-INF/osem.json", "META-INF/osem-invalidDriver.json");
		TestingUtil.rename("META-INF/osem1.json", "META-INF/osem.json");
	}
	
	@Test
	public void INVALID_STORE_DRIVER(){
		final String expectedError = "Invalid 'osemStore' found in JSON configuration - bad 'driverClass' parameter";
		String actualError = null;
		
		try {
			OsemStores.getDefaultOsemStore();
		} catch (OsemException actualException) {
    		actualError = actualException.getMessage();
		}
		
		assertEquals(expectedError, actualError);
		System.out.println(">>>>>>>>> Success - test 'INVALID_STORE_DRIVER' <<<<<<<<<");
    }
	
}
