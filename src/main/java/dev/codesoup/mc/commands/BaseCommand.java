package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class BaseCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/base";
	
	public BaseCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
		
		Integer onClaim = mod.getEventHandler().numPeopleOnClaim.get(player.getUniqueID());
		if(onClaim != null && onClaim > 0) {
			BlockPos pos = player.getBedLocation();
			if(pos != null)
				mod.getServer().getCommandManager().executeCommand(mod.getServer(), String.format("/tp %s %f %f %f", player.getName(), pos.getX(), pos.getY(), pos.getZ()));
			else
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You haven't set your spawn!"));
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You can only teleport back to your base if someone's on it."));
		}
	
	}
	
	@Override
	public String getName() {
		return "base";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
}
