package dev.codesoup.mc.commands;

import java.util.stream.Collectors;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class ViewInventoryCommand extends ModCommandBase {

	private final static String USAGE = "/vi <player1> <player2> (...)";
	
	public ViewInventoryCommand(CustomMod mod) {
		super(mod, "vi", 4);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		_assert(params.length == 1, ERR_INCORRECT_USAGE + USAGE);
		
		for(String playername: params) {
			EntityPlayer player = assertOnline(playername);
			String itemList = player.inventory.mainInventory.stream().map(stack -> String.format("%d x %s", stack.getCount(), stack.getDisplayName())).collect(Collectors.joining(", "));
			sender.sendMessage(new TextComponentString(playername + ": " + itemList));
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
