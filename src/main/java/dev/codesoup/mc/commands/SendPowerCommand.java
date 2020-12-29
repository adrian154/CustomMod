package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class SendPowerCommand extends ModCommandBase {

	public static final String USAGE = "/sendpower <player> <amount>";
	
	public SendPowerCommand(CustomMod mod) {
		super(mod, "sendpower", "sp", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
