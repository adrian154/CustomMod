package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class TogglePVPCommand extends ModCommandBase {

	private final static String USAGE = "/pvp";
	
	public TogglePVPCommand(CustomMod mod) {
		super(mod, "pvp", 4);
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
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
