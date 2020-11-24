package dev.codesoup.mc.mcws;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.mcws.messages.ConsoleMessage;

public class CustomAppender extends AbstractAppender {

	private CustomMod mod;
	
	public CustomAppender(CustomMod mod) {
		
		super(
			"MCWebSocketAppender",
			null,
			PatternLayout.newBuilder().withPattern("%msg").build(),
			false
		);
		
		this.mod = mod;
		
	}
	
	@Override
	public void append(LogEvent event) {	
		event = event.toImmutable();
		mod.getWSServer().broadcastMessageToAuthed(new ConsoleMessage(event.getThreadName(), event.getLevel().toString(), event.getMessage().getFormattedMessage()));
	}
	
	@Override
	public boolean isStarted() {
		return true;
	}
	
}
