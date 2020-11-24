package dev.codesoup.mc.mcws.messages;

import net.minecraftforge.event.ServerChatEvent;

public class PlayerChatMessage extends OnePlayerMessage {

	public String message;
	
	public PlayerChatMessage(ServerChatEvent event) {
		super("chat", event.getPlayer());
		type = "chat";
		this.message = event.getMessage();
	}
	
}
