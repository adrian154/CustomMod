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

public class NationManager extends RequiresMod {
	
	private List<Nation> nations;
	private transient Map<UUID, Nation> playerNations;
	
	public NationManager(CustomMod mod) {
		super(mod);
		mod.getServer().getWorld(0).toString();
		this.nations = new ArrayList<Nation>();
		this.playerNations = new HashMap<UUID, Nation>();
	}
	
	public void initPlayerNations() {
		for(Nation nation: nations) {
			for(UUID uuid: nation.getMembers()) {
				playerNations.put(uuid, nation);
			}
		}
	}
	
	public Nation getNation(EntityPlayer player) {
		return playerNations.get(player.getUniqueID());
	}
	
	public Nation getNation(UUID uuid) {
		return playerNations.get(uuid);
	}
	
	public Nation getNation(String name) {
		return nations.stream().filter(nation -> nation.getName().equals(name)).findFirst().orElse(null);
	}
	
	public boolean sameNation(EntityPlayer A, EntityPlayer B) {
		return sameNation(A.getUniqueID(), B.getUniqueID());
	}
	
	public boolean sameNation(UUID A, UUID B) {
		Nation nation = getNation(A);
		return nation == null ? false : nation.getMembers().contains(B);
	}

	public void addPlayer(Nation nation, UUID uuid) {
		nation.addMember(uuid);
		this.playerNations.put(uuid, nation);
	}
	
	public void removePlayer(Nation nation, UUID uuid) {
		nation.removeMember(uuid);
		this.playerNations.remove(uuid);
	}
	
	public void addNation(Nation nation) {
		
		this.nations.add(nation);
		for(UUID uuid: nation.getMembers()) {
			playerNations.put(uuid, nation);
		}
		
		this.refreshNames(nation);
		
	}

	public void setNationName(Nation nation, String string) {
		nation.setName(string);
		refreshNames(nation);
		broadcastTo(nation, TextFormatting.GRAY + "Your alliance was renamed to " + nation.getColor() + string + TextFormatting.GRAY + ".");
	}
	
	public void broadcastTo(Nation nation, String message) {
		PlayerList playerList = mod.getServer().getPlayerList();
		for(UUID uuid: nation.getMembers()) {
			EntityPlayerMP player = playerList.getPlayerByUUID(uuid);
			if(player != null) {
				player.sendMessage(new TextComponentString(message));
			}
		}
	}
	
	public String getName(EntityPlayer player) {
		
		Nation nation = this.getNation(player);
		
		String prefix = "";
		if(nation != null) {
			prefix = String.format("%s[%s]%s ", nation.getColor(), nation.getName(), TextFormatting.RESET);
		}
		
		return String.format("%s%s", prefix, player.getName());
	
	}
	
	public void refreshNames(Nation nation) {
		PlayerList playerList = mod.getServer().getPlayerList();
		for(UUID uuid: nation.getMembers()) {
			EntityPlayerMP player = playerList.getPlayerByUUID(uuid);
			if(player != null) {
				player.refreshDisplayName();
			}
		}
	}
	
	public void listInvitations(EntityPlayerMP player, boolean listIfNone) {
		
		List<Nation> invitedTo = new ArrayList<Nation>();
		for(Nation nation: this.nations) {
			if(nation.hasInvitationFor(player)) {
				invitedTo.add(nation);
			}
		}
		
		if(invitedTo.size() > 0) {
			
			String nations = invitedTo.stream().map(nation -> nation.getName()).collect(Collectors.joining(", "));
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You've been invited to: " + TextFormatting.WHITE + nations + "\n" +
													   TextFormatting.GRAY + "Do " + TextFormatting.WHITE + "/nation accept <nation name>" + TextFormatting.GRAY + " to accept an invitation."));
			
		} else if(listIfNone) {
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You have not been invited to any nations."));
		}
		
	}
	
}