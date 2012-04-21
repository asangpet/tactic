package edu.cmu.tactic.data;

import java.util.Comparator;

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

	public static Comparator<Response> getDeadlineComparator() {
		return new Comparator<Response>() {
			@Override
			public int compare(Response o1, Response o2) {
				double diff = (o1.requestTime+o1.responseTime - (o2.requestTime+o2.responseTime));
				if (diff < 0) return -1;
				else if (diff > 0) return 1;
				else return 0;
			}
		};
	}
	
	public static Comparator<Response> getRequestTimeComparator() {
		return new Comparator<Response>() {
			@Override
			public int compare(Response o1, Response o2) {
				double diff = (o1.requestTime - o2.requestTime);
				if (diff < 0) return -1;
				else if (diff > 0) return 1;
				else return 0;
			}
		};
	}
}
