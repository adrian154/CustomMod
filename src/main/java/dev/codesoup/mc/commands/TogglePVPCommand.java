package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class TogglePVPCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/pvp";
	
	public TogglePVPCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(mod.getEventHandler().togglePVP()) {
			mod.broadcast(TextFormatting.RED + "PVP is now enabled.");
		} else {
			mod.broadcast(TextFormatting.GREEN + "PVP is now disabled.");
		}
		
	}
	
	@Override
	public String getName() {
		return "pvp";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(2, "");
	}
	
}
