package dev.codesoup.mc.mcws.messages;

public class ConsoleMessage extends OutMessage {

	public String threadName;
	public String level;
	public String message;
	
	public ConsoleMessage(String threadName, String level, String message) {
		super("console");
		this.threadName = threadName;
		this.level = level;
		this.message = message;
	}
	
}