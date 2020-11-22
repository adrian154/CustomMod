package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AllianceManager {
	
	private List<Alliance> alliances;
	private transient Map<UUID, Alliance> playerAlliances;
	private transient CustomMod mod;
	
	public AllianceManager(CustomMod mod) {
		this.mod = mod;
		this.alliances = new ArrayList<Alliance>();
		this.playerAlliances = new HashMap<UUID, Alliance>();
	}
	
	public Alliance getAlliance(EntityPlayer player) {
		return playerAlliances.get(player.getUniqueID());
	}
	
	public Alliance getAlliance(UUID uuid) {
		return playerAlliances.get(uuid);
	}
	
	public Alliance getAlliance(String name) {
		return alliances.stream().filter(alliance -> alliance.getName().equals(name)).findFirst().orElse(null);
	}
	
	public boolean areAllied(EntityPlayer A, EntityPlayer B) {
		return getAlliance(A).getMembers().contains(B.getUniqueID());
	}
	
	public boolean areAllied(UUID A, UUID B) {
		return getAlliance(A).getMembers().contains(B);
	}

	public void removePlayer(Alliance alliance, UUID uuid) {
		alliance.removeMember(uuid);
		this.playerAlliances.remove(uuid);
	}
	
	public void addAlliance(Alliance alliance) {
		this.alliances.add(alliance);
		for(UUID uuid: alliance.getMembers()) {
			playerAlliances.put(uuid, alliance);
		}
	}

	public void broadcastTo(Alliance alliance, String message) {
		PlayerList playerList = mod.getServer().getPlayerList();
		for(UUID uuid: alliance.getMembers()) {
			EntityPlayerMP player = playerList.getPlayerByUUID(uuid);
			if(player != null) {
				player.sendMessage(new TextComponentString(message));
			}
		}
	}
	
	public String getName(EntityPlayer player) {
		Alliance alliance = this.getAlliance(player);
		
		String prefix = "";
		if(alliance != null) {
			prefix = String.format("%s[%s]%s ", TextFormatting.YELLOW, alliance.getName(), TextFormatting.RESET);
		}
		
		return String.format("%s%s", prefix, player.getName());
	}
	
	public void refreshNames(Alliance alliance) {
		PlayerList playerList = mod.getServer().getPlayerList();
		for(UUID uuid: alliance.getMembers()) {
			EntityPlayerMP player = playerList.getPlayerByUUID(uuid);
			if(player != null) {
				player.refreshDisplayName();
			}
		}
	}
	
}