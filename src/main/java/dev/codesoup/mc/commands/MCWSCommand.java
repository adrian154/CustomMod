package dev.codesoup.mc.commands;

import java.io.IOException;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MCWSCommand extends ModCommandBase {

	private final static String USAGE_RELOAD = "/mcws reload";
	private final static String USAGE_GENKEY = "/mcws genkey [count]";
	private final static String USAGE = USAGE_RELOAD + "\n" + USAGE_GENKEY;
	
	public MCWSCommand(CustomMod mod) {
		super(mod, "mcws", 4);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		_assert(params.length > 1, ERR_INCORRECT_USAGE + USAGE);
		
		if(params[0].equals("reload")) {
			
			try {
				mod.getConfiguration().reload();
			} catch(IOException exception) {
				exception.printStackTrace();
				throw new CommandException("The configuration could not be reloaded: " + exception.getMessage());
			}
			
		} else if(params[0].equals("genkey")) {
			
			int count = 1;
			
			
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
