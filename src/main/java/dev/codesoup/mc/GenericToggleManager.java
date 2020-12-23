package dev.codesoup.mc;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;

public class GenericToggleManager {

	private Map<UUID, Boolean> state;
	
	public GenericToggleManager() {
		state = new WeakHashMap<UUID, Boolean>();
	}
	
	private boolean ifNullFalse(Boolean value) {
		return value == null ? false : value;
	}
	
	public boolean get(EntityPlayer player) {
		return ifNullFalse(state.get(player.getUniqueID()));
	}
	
	public boolean toggle(EntityPlayer player) {
		boolean newState = !get(player);
		state.put(player.getUniqueID(), newState);
		return newState;
	}
	
	public void set(EntityPlayer player, boolean state) {
		this.state.put(player.getUniqueID(), state);
	}

}