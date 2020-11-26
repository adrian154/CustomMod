package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class RerenderMarkersCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/rem";
	
	public RerenderMarkersCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		mod.getMapManager().initClaims(mod.getClaims());
		sender.sendMessage(new TextComponentString("Working..."));
		
	}
	
	@Override
	public String getName() {
		return "rem";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
