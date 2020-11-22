package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public class Alliance {

	private List<UUID> members;
	private String name;
	private UUID leader;
	
	public Alliance() {
		members = new ArrayList<UUID>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public List<UUID> getMembers() {
		return this.members;
	}
	
	public void addMember(UUID player) {
		this.members.add(player);
	}
	
	public void addMember(EntityPlayer player) {
		this.addMember(player.getUniqueID());
	}
	
	public void removeMember(UUID uuid) {
		this.members.remove(uuid);
	}
	
	public void makeLeader(UUID newLeader) {
		this.leader = newLeader;
	}
	
	public void makeLeader(EntityPlayer player) {
		this.makeLeader(player.getUniqueID());
	}
	
	public UUID getLeader() {
		return this.leader;
	}
	
	public boolean isLeader(EntityPlayer player) {
		return this.leader.equals(player.getUniqueID());
	}
	
}
