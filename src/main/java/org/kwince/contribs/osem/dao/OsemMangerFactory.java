package org.kwince.contribs.osem.dao;

import org.kwince.contribs.osem.common.ElasticClientFactory;
import org.kwince.contribs.osem.event.EventDispatcher;

public class OsemMangerFactory {
	
	private ElasticClientFactory elastic;
	private EventDispatcher dispatcher;
	
	public void setElastic(ElasticClientFactory elastic){
		this.elastic = elastic;
	}
	
	public void setDispatcher(EventDispatcher dispatcher){
		this.dispatcher = dispatcher;
	}
		
	public OsemManager createOsemManager() {
		return new OsemManagerImpl(elastic.createNode(), dispatcher);
	}
	
}
