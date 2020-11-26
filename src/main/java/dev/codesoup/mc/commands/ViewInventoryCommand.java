package dev.codesoup.mc.commands;

import java.util.stream.Collectors;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ViewInventoryCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/vi <player1> <player2> (...)";
	
	public ViewInventoryCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(params.length < 1) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + USAGE));
		}
		
		for(String playername: params) {
			EntityPlayer player = mod.getServer().getPlayerList().getPlayerByUsername(playername);
			String itemList = player.inventory.mainInventory.stream().map(stack -> String.format("%d x %s", stack.getCount(), stack.getDisplayName())).collect(Collectors.joining(", "));
			sender.sendMessage(new TextComponentString(playername + ": " + itemList));
		}
		
	}
	
	@Override
	public String getName() {
		return "vi";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
