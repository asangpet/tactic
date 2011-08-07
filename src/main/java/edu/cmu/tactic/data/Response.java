package edu.cmu.tactic.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.document.mongodb.mapping.Document;


@Document(collection="responseTime")
public class Response {
	@Id
	private String id;
	private long timestamp;
	private Socket server, client;
	private double requestTime, responseTime;
	private String request,response;
	private String protocol;
	
	public String getProtocol() {
		return protocol;
	}		
	
	public Socket getServer() {
		return server;
	}
	
	public Socket getClient() {
		return client;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id:").append(id);
		builder.append(" timestamp:").append(timestamp);
		builder.append(" server:").append(server);
		builder.append(" client:").append(client);
		builder.append(" proto:").append(protocol);
		builder.append(" time - req:").append(requestTime);
		builder.append(" res:").append(responseTime);
		return builder.toString();
	}
}
