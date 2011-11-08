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
	
	public double getResponseTime() {
		return responseTime;
	}
	public double getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(double requestTime) {
		this.requestTime = requestTime;
	}
	public void setResponseTime(double responseTime) {
		this.responseTime = responseTime;
	}
	
	boolean contains(double time) {
		return requestTime <= time && requestTime+responseTime >= time;
	}
	public boolean isOverlap(Response r) {
		return r.contains(requestTime) || r.contains(requestTime+responseTime) || contains(r.requestTime);
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
		builder.append(" req:").append(request);
		builder.append(" req:").append(response);
		return builder.toString();
	}
}
