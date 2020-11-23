package dev.codesoup.mc.commands;

import dev.codesoup.mc.ClaimsManager;
import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class ClaimCommand extends CommandBase {

	private CustomMod mod;
	private ClaimsManager claims;
	private final static String USAGE = "/claim";
	
	public ClaimCommand(CustomMod mod) {
		this.mod = mod;
		this.claims = mod.getClaims();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		
		
		if(claims.getClaim(chunk.x, chunk.z) == null) {
			if(mod.getPowerManager().getFreePower(player) > 0) {
				claims.setClaim(chunk.x, chunk.z, player.getUniqueID());
				player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Chunk claimed!"));
			} else {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Not enough power!"));
			}
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "That chunk is already claimed!"));
		}
		
	}
	
	@Override
	public String getName() {
		return "claim";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
