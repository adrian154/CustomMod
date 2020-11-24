package dev.codesoup.mc.mcws.messages;

public class OutMessage {
	
	public String type;
	public long timestamp;
	
	public OutMessage(String type) {
		timestamp = System.currentTimeMillis();
		this.type = type;
	}
	
}