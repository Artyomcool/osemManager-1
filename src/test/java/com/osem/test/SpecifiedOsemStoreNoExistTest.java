package com.osem.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.osem.common.OsemStores;
import com.osem.exceptions.OsemException;

public class SpecifiedOsemStoreNoExistTest {

	@Test
	public void STORE_NOT_FOUND() {
		String storeName = "my_osemXYZ";
		final String expectedError = "Specified 'osemStore' not found in 'osemStores' array, " + storeName;
		String actualError = null;
		
		try {
			OsemStores.findOsemStore(storeName);
		} catch (OsemException actualException) {
    		actualError = actualException.getMessage();
		}
		
		assertEquals(expectedError, actualError);
		System.out.println(">>>>>>>>> Success - test 'STORE_NOT_FOUND' <<<<<<<<<");
    }
}
