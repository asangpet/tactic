package edu.cmu.tactic.data;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="responseTime")
public class Response {
	@Id @JsonProperty private String id;
	@JsonProperty private long timestamp;
	@JsonProperty private Socket server, client;
	@JsonProperty private double requestTime, responseTime;
	@JsonProperty private String request,response;
	@JsonProperty private String protocol;
	
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