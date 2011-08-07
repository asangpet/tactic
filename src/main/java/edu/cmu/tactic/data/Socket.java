package edu.cmu.tactic.data;

public class Socket {
	String address;
	int port;
	
	@Override
	public String toString() {
		return address + ":" + port;
	}
}
