package org.kwince.contribs.osem.exceptions;

public class OsemException extends RuntimeException {
	
	private static final long serialVersionUID = 2181854548409384551L;

	public OsemException(String message) {
		super(message);
	}

	public OsemException(String message, Throwable cause) {
		super(message,cause);
	}
}
