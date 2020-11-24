package dev.codesoup.mc.commands;

import java.util.UUID;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class UnclaimCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/unclaim";
	
	public UnclaimCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		
		UUID claimer = mod.getClaims().getClaim(chunk.x, chunk.z);
		if(!claimer.equals(player.getUniqueID())) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't own this territory."));
			return;
		}
		
		mod.getClaims().unclaim(chunk.x, chunk.z);
		player.sendMessage(new TextComponentString(TextFormatting.GREEN + "+1 power"));
		player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Chunk unclaimed!"));
		
	}
	
	@Override
	public String getName() {
		return "unclaim";
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
