package dev.codesoup.mc.mcws.messages;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.java_websocket.WebSocket;

import dev.codesoup.mc.CustomMod;
import net.minecraft.entity.player.EntityPlayerMP;

public class InMessage {

	// What the requested action is
	public String type;
	
	// Potential fields, which ones to look at is determined by `type`
	public String discordTag;
	public String message;
	public String secret;
	public String command;
	
	// No args constructor, necessary for GSON
	public InMessage() {
		
	}

	public String execAuth(CustomMod mod, WebSocket conn) {
		if(secret != null && mod.getConfiguration().verifyKey(secret)) {
			mod.getWSServer().authorize(conn);
			return "{\"success\":true}";
		} else {
			return "{\"success\":false, \"error\":\"Incorrect or missing client secret\"}";
		}
	}
	
	public String execMessage(CustomMod mod, WebSocket conn) {
		if(mod.getWSServer().isAuthorized(conn)) {
			mod.broadcast(String.format("[Discord] %s: %s", discordTag, message));
			return "{\"success\":true}";
		} else {
			return "{\"success\":false, \"error\":\"Not authorized\"}";
		}
	}
	
	public String execGetOnline(CustomMod mod, WebSocket conn) {
		return mod.gson.toJson(new OnlinePlayers(mod));
	}
	
	public String execRunCommand(CustomMod mod, WebSocket conn) {
		if(mod.getWSServer().isAuthorized(conn)) {
			mod.getServer().getCommandManager().executeCommand(mod.getServer(), command);
			return "{\"success\":true}";
		} else {
			return "{\"success\":false, \"error\":\"Not authorized\"}";
		}
	}
	
	// Return JSON string
	public String execute(CustomMod mod, WebSocket conn) {
		
		switch(type) {
			case "auth": return execAuth(mod, conn);
			case "message": return execMessage(mod, conn);
			case "getOnline": return execGetOnline(mod, conn);
			case "runCommand": return execRunCommand(mod, conn);
			default: return "";
		}
		
	}
	
	// surely there's a more elegant way to do this, but I don't know of it
	@SuppressWarnings("unused")
	private static class OnlinePlayers {

		// there's a warning but this will be serialized eventually
		public List<PlayerPair> data;
		
		public OnlinePlayers(CustomMod mod) {
			
			data = mod.getServer().getPlayerList().getPlayers().stream().map(player -> new PlayerPair(player)).collect(Collectors.toList());
			
		}
		
		private static class PlayerPair {
			
			// see comment on `data`
			public UUID uuid;
			public String name;
			
			public PlayerPair(EntityPlayerMP player) {
				this.uuid = player.getUniqueID();
				this.name = player.getName();
			}
			
		}
		
	}
	
}
