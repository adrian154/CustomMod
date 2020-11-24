package dev.codesoup.mc.mcws.messages;

import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class PlayerQuitMessage extends OnePlayerMessage {

	public PlayerQuitMessage(PlayerLoggedOutEvent event) {
		super("quit", event.player);
	}
	
}
