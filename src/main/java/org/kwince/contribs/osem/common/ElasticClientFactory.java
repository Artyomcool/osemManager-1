package org.kwince.contribs.osem.common;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class ElasticClientFactory {
	
	private Boolean nodeClient;
	private Boolean nodeLocal = false;
	
	private String clusterName;
	
	private String host;
	private String port;
	private String clientTransportSniff;
	
	public Node createNode() {
						
		Builder settings = ImmutableSettings.settingsBuilder();
		
		if(nodeClient != null){
			settings.put("node.client", nodeClient);
			settings.put("cluster.name", clusterName);
			settings.put("node.local", nodeLocal);
		}else{
			settings.put("host", host);
			settings.put("port", port);
			settings.put("client.transport.sniff", clientTransportSniff);
			settings.put("cluster.name", clusterName);
		}
		
		settings.put("index.number_of_shards", 16);
		
		settings.build();

		NodeBuilder nb = NodeBuilder.nodeBuilder().settings(settings);
		return nb.node();
	}
	
	public ElasticClientFactory setNodeClient(Boolean nodeClient) {
		this.nodeClient = nodeClient;
		return this;
	}

	public ElasticClientFactory setNodeLocal(Boolean nodeLocal) {
		this.nodeLocal = nodeLocal;
		return this;
	}

	public ElasticClientFactory setClusterName(String clusterName) {
		this.clusterName = clusterName;
		return this;
	}

	public ElasticClientFactory setHost(String host) {
		this.host = host;
		return this;
	}

	public ElasticClientFactory setPort(String port) {
		this.port = port;
		return this;
	}

	public ElasticClientFactory setClientTransportSniff(String clientTransportSniff) {
		this.clientTransportSniff = clientTransportSniff;
		return this;
	}

}
