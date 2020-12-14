package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

public class Nation {

	private List<UUID> members;
	private List<UUID> outstandingInvitations;
	private String name;
	private TextFormatting color;
	private UUID leader;
	
	public Nation() {
		this.members = new ArrayList<UUID>();
		this.outstandingInvitations = new ArrayList<UUID>();
		this.color = TextFormatting.YELLOW;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getFmtName() {
		return color + this.name;
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
	
	public boolean isLeader(UUID uuid) {
		return this.leader.equals(uuid);
	}
	
	public boolean isLeader(EntityPlayer player) {
		return this.leader.equals(player.getUniqueID());
	}
	
	public void invite(UUID uuid) {
		this.outstandingInvitations.add(uuid);
	}
	
	public void uninvite(UUID uuid) {
		this.outstandingInvitations.remove(uuid);
	}
	
	public boolean hasInvitationFor(UUID uuid) {
		return this.outstandingInvitations.contains(uuid);
	}
	
	public boolean hasInvitationFor(EntityPlayer player) {
		return hasInvitationFor(player.getUniqueID());
	}
	
	public void setColor(TextFormatting color) {
		this.color = color;
	}
	
	public TextFormatting getColor() {
		return this.color;
	}
	
	public List<UUID> getInvitations() {
		return this.outstandingInvitations;
	}
	
}
