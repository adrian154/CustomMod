package dev.codesoup.mc;

public abstract class RequiresMod {

	protected transient CustomMod mod;
	
	public RequiresMod(CustomMod mod) {
		this.mod = mod;
	}
	
}
