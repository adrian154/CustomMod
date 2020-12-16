package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class RerenderMarkersCommand extends ModCommandBase {

	private final static String USAGE = "/rem";
	
	public RerenderMarkersCommand(CustomMod mod) {
		super(mod, "rem", 4);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		mod.getMapManager().initClaims(mod.getClaimsManager());
		sender.sendMessage(new TextComponentString("Working..."));
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
