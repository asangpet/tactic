package edu.cmu.tactic.data;

import org.codehaus.jackson.annotate.JsonProperty;

public class Socket {
	@JsonProperty String address;
	@JsonProperty int port;
	
	public String toString() {
		return "address:"+address+",port:"+port;
	}
}
