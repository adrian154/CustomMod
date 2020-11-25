package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AllianceManager extends RequiresMod {
	
	private List<Alliance> alliances;
	private transient Map<UUID, Alliance> playerAlliances;
	
	public AllianceManager(CustomMod mod) {
		super(mod);
		this.alliances = new ArrayList<Alliance>();
		this.playerAlliances = new HashMap<UUID, Alliance>();
	}
	
	public void initPlayerAlliances() {
		for(Alliance alliance: alliances) {
			for(UUID uuid: alliance.getMembers()) {
				playerAlliances.put(uuid, alliance);
			}
		}
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
		Alliance alliance = getAlliance(A);
		return alliance == null ? false : alliance.getMembers().contains(B);
	}

	public void addPlayer(Alliance alliance, UUID uuid) {
		alliance.addMember(uuid);
		this.playerAlliances.put(uuid, alliance);
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
	
	public void listInvitations(EntityPlayerMP player, boolean listIfNone) {
		
		List<Alliance> invitedTo = new ArrayList<Alliance>();
		for(Alliance alliance: this.alliances) {
			if(alliance.hasInvitationFor(player)) {
				invitedTo.add(alliance);
			}
		}
		
		if(invitedTo.size() > 0) {
			
			String alliances = invitedTo.stream().map(alliance -> alliance.getName()).collect(Collectors.joining(", "));
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You've been invited to: " + TextFormatting.WHITE + alliances + "\n" +
													   TextFormatting.GRAY + "Do " + TextFormatting.WHITE + "/alliance accept <alliance name>" + TextFormatting.GRAY + " to accept an invitation."));
			
		} else if(listIfNone) {
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You have not been invited to any alliances."));
		}
		
	}
	
}