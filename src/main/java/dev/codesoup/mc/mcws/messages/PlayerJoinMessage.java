package dev.codesoup.mc.mcws.messages;

import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class PlayerJoinMessage extends OnePlayerMessage {

	public PlayerJoinMessage(PlayerLoggedInEvent event) {
		super("join", event.player);
	}
	
}
