package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public class AllianceManager {
	
	private List<Alliance> alliances;
	private Map<UUID, Alliance> playerAlliances;
	
	public AllianceManager() {
		
		alliances = new ArrayList<Alliance>();
		playerAlliances = new HashMap<UUID, Alliance>();
		
	}
	
	public Alliance getAlliance(EntityPlayer player) {
		return playerAlliances.get(player.getUniqueID());
	}
	
	public Alliance getAlliance(UUID uuid) {
		return playerAlliances.get(uuid);
	}
	
	public boolean areAllied(EntityPlayer A, EntityPlayer B) {
		return getAlliance(A).getMembers().contains(B.getUniqueID());
	}
	
	public boolean areAllied(UUID A, UUID B) {
		return getAlliance(A).getMembers().contains(B);
	}
	
	public void addAlliance(Alliance alliance) {
		this.alliances.add(alliance);
		for(UUID uuid: alliance.getMembers()) {
			playerAlliances.put(uuid, alliance);
		}
	}
	
}
