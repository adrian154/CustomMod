package dev.codesoup.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Alliance {

	private List<UUID> members;
	private String name;
	
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
	
}
