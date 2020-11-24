package dev.codesoup.mc.mcws.messages;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public class OnePlayerMessage extends OutMessage {

	public UUID uuid;
	public String playerName;
	
	public OnePlayerMessage(String type, EntityPlayer player) {
		super(type);
		this.uuid = player.getUniqueID();
		this.playerName = player.getName();
	}
	
}
