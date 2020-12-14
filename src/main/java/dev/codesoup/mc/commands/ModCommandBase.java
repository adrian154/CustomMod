package dev.codesoup.mc.commands;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;

public abstract class ModCommandBase extends CommandBase {

	protected CustomMod mod;
	
	private final String ERR_USER_NONEXISTANT = "That user does not exist.";
	
	public ModCommandBase(CustomMod mod) {
		this.mod = mod;
	}
	
	protected void _assert(boolean condition, String message) throws CommandException {
		if(!condition) throw new CommandException(message);
	}
	
	protected GameProfile assertPlayer(String playerName) throws CommandException {
		GameProfile profile = mod.getServer().getPlayerProfileCache().getGameProfileForUsername(playerName);
		_assert(profile != null, ERR_USER_NONEXISTANT);
		return profile;
	}
	
}
