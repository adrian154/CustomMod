package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AutoclaimCommand extends ModCommandBase {

	private static final String USAGE = "/autoclaim";
	
	public AutoclaimCommand(CustomMod mod) {
		super(mod, "autoclaim", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		EntityPlayerMP player = assertIsPlayer(sender);
		if(mod.getEventHandler().isAutoclaiming(player)) {
			mod.getEventHandler().stopAutoclaiming(player);
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Stopped autoclaiming."));
		} else {
			mod.getEventHandler().startAutoclaiming(player);
			player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Started autoclaiming."));
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
