package dev.codesoup.mc.commands;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

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
		
		_assert(params.length >= 1, ERR_INCORRECT_USAGE + USAGE);
		
		if(params[0].equals("reload")) {
			
			try {
				mod.getConfiguration().reload();
			} catch(IOException exception) {
				exception.printStackTrace();
				throw new CommandException("The configuration could not be reloaded: " + exception.getMessage());
			}
			
		} else if(params[0].equals("genkey")) {
			
			_assert(params.length < 2, ERR_INCORRECT_USAGE + USAGE);
			
			int count = 1;
			if(params.length == 2) count = parseInt(params[1]);
			
			SecureRandom random = new SecureRandom();
			for(int i = 0; i < count; i++) {
				byte bytes[] = new byte[64];
				random.nextBytes(bytes);
				String key = Base64.getEncoder().encodeToString(bytes);
				mod.getConfiguration().addKey(key);
			}
			
			try {
				mod.getConfiguration().save();
			} catch(IOException exception) {
				throw new CommandException("Failed to save configuration.");
			}
			
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
