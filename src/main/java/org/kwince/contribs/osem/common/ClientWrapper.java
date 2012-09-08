package org.kwince.contribs.osem.common;

import org.elasticsearch.client.Client;

public interface ClientWrapper {

	Client getClient();
	void close();
	
}
