package dev.codesoup.mc;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;

public class GenericToggleManager {

	private Map<UUID, Boolean> state;
	private boolean defaultValue;
	
	public GenericToggleManager(boolean defaultValue) {
		state = new WeakHashMap<UUID, Boolean>();
		this.defaultValue = defaultValue;
	}
	
	public GenericToggleManager() {
		this(false);
	}
	
	private boolean ifNullDefault(Boolean value) {
		return value == null ? defaultValue : value;
	}
	
	public boolean get(EntityPlayer player) {
		return ifNullDefault(state.get(player.getUniqueID()));
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