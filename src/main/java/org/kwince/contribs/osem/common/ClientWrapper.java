package org.kwince.contribs.osem.common;

import org.elasticsearch.client.Client;

/**
 * Wrapper for {@link Client}
 * @author Artyomcool
 *
 */
public interface ClientWrapper {

	/**
	 * Returns a {@link Client}
	 * @return ES-client
	 */
	Client getClient();
	
	/**
	 * Releases a client connection
	 */
	void close();
	
}
