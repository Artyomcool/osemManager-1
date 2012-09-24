package org.kwince.contribs.osem.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.kwince.contribs.osem.exceptions.OsemException;

public class ElasticClientFactory {
	
	private Boolean nodeClient;
	private Boolean nodeLocal = false;
	
	private String clusterName;
	private String path;
	
	private String host;
	private String port;
	private Boolean clientTransportSniff;
	
	public ClientWrapper createNode() {
						
		Builder settings = ImmutableSettings.settingsBuilder();

		if(path!=null)
			settings.put("path.home", path);
		
		if(host == null){
			settings.put("node.client", nodeClient)
				.put("cluster.name", clusterName)
				.put("node.local", nodeLocal);
		}else{
			settings
				.put("client.transport.sniff", clientTransportSniff)
				.put("cluster.name", clusterName);
			
			final Client client = new TransportClient(settings)
							.addTransportAddress(new InetSocketTransportAddress(host, Integer.valueOf(port)));
			return new ClientWrapper() {
				
				@Override
				public Client getClient() {
					return client;
				}
				
				@Override
				public void close() {
					client.close();
				}
			};
		}
		
		NodeBuilder nb = NodeBuilder.nodeBuilder().settings(settings);
		final Node n=nb.node();
		
		return new ClientWrapper() {
			
			private Client client;
			
			@Override
			public Client getClient() {
				if(client == null)
					client = n.client();
				return client;
			}
			
			@Override
			public void close() {
				if(client!=null)client.close();
				n.close();
			}
		};
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

	public ElasticClientFactory setClientTransportSniff(Boolean clientTransportSniff) {
		this.clientTransportSniff = clientTransportSniff;
		return this;
	}

	public ElasticClientFactory setPath(String path) {
		this.path = path;
		return this;
	}
	
	public ElasticClientFactory setProperties(String path){
		Properties p = new Properties();
		try {
			
			if(path.startsWith("classpath:")){
				p.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(path.substring("classpath:".length())));
			}else{
				p.load(new FileInputStream(path));
			}
			
		} catch (IOException e) {
			throw new OsemException("Can't load file", e);
		}

		nodeClient = Boolean.valueOf(p.getProperty("osem.nodeClient", String.valueOf(nodeClient)));
		nodeLocal = Boolean.valueOf(p.getProperty("osem.nodeLocal", String.valueOf(nodeLocal)));
		clientTransportSniff = Boolean.valueOf(p.getProperty("osem.clientTransportSniff", String.valueOf(clientTransportSniff)));

		clusterName = p.getProperty("osem.clusterName", clusterName);
		host = p.getProperty("osem.host", host);
		port = p.getProperty("osem.port", port);
		this.path = p.getProperty("osem.path", this.path);
		
		return this;
	}

}
