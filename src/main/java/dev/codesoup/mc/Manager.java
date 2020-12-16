package dev.codesoup.mc;

public abstract class Manager {

	protected transient CustomMod mod;
	
	public Manager(CustomMod mod) {
		this.mod = mod;
	}

	public CustomMod getMod() {
		return mod;
	}
	
}
