package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class BaseCommand extends ModCommandBase {

	private final static String USAGE = "/base";
	
	public BaseCommand(CustomMod mod) {
		super(mod, "base", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		_assert(player.getBedLocation() != null, "You have not set your spawn yet.");
		mod.getEventHandler().startBaseTPTimer(player);
		player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Teleporting you to your base in 10 seconds. Don't move, or it will be canceled."));
	
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
