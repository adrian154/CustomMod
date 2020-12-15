package dev.codesoup.mc.commands;

import java.util.Arrays;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.NationManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class NationChatCommand extends ModCommandBase {

	private NationManager nationManager;
	private final static String USAGE = "/nc";
	
	public NationChatCommand(CustomMod mod) {
		super(mod, "nc", 0);
		this.nationManager = mod.getNationManager();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		Nation nation = this.nationManager.getNation(player);
		
		_assert(nation != null, "You are not in a nation.");
		_assert(params.length >= 1, "You did not specify a message.");

		String message = String.join(" ", (String [])Arrays.copyOfRange(params, 0, params.length));
		
		this.nationManager.broadcastTo(
			nation,
			String.format(
				"%s(nation)%s %s: %s",
				nation.getColor(),
				TextFormatting.RESET,
				player.getName(),
				message
			)
		);
		
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
