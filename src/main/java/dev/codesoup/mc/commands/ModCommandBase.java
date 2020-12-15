package dev.codesoup.mc.commands;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class ModCommandBase extends CommandBase {

	protected CustomMod mod;
	protected String commandName;
	protected int requiredPermissionLevel;
	
	private final String ERR_USER_NONEXISTANT = "That user does not exist.";
	private final String ERR_USER_OFFLINE = "That user does not exist or is offline.";
	private final String ERR_MUST_BE_PLAYER = "You must be a player to use this command.";
	protected final String ERR_INCORRECT_USAGE = "Incorrect number of parameters.\nUsage: ";
	
	public ModCommandBase(CustomMod mod, String commandName, int requiredPermissionLevel) {
		this.mod = mod;
		this.commandName = commandName;
		this.requiredPermissionLevel = requiredPermissionLevel;
	}
	
	protected void _assert(boolean condition, String message) throws CommandException {
		if(!condition) throw new CommandException(message);
	}
	
	protected GameProfile assertPlayer(String playerName) throws CommandException {
		GameProfile profile = mod.getProfile(playerName);
		_assert(profile != null, ERR_USER_NONEXISTANT);
		return profile;
	}
	
	protected EntityPlayerMP assertOnline(String playerName) throws CommandException {
		EntityPlayerMP player = mod.getPlayer(playerName);
		_assert(player != null, ERR_USER_OFFLINE);
		return player;
	}
	
	protected EntityPlayerMP assertIsPlayer(ICommandSender sender) throws CommandException {
		_assert(sender instanceof EntityPlayerMP, ERR_MUST_BE_PLAYER);
		return (EntityPlayerMP)sender;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return this.requiredPermissionLevel;
	}
	
	@Override
	public String getName() {
		return this.commandName;
	}
	
}
