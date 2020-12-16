package dev.codesoup.mc.commands;

import dev.codesoup.mc.ClaimsManager;
import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class ClaimCommand extends ModCommandBase {

	private ClaimsManager claims;
	private final static String USAGE = "/claim";
	
	public ClaimCommand(CustomMod mod) {
		super(mod, "claim", 0);
		this.claims = mod.getClaimsManager();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		
		if(claims.getClaim(chunk.x, chunk.z) == null) {
			if(mod.getPowerManager().removeFreePower(player, 1)) {
				claims.setClaim(chunk.x, chunk.z, player.getUniqueID());
				player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Chunk claimed!"));
			}
		} else {
			throw new CommandException("That chunk is already claimed!");
		}
		
	}

	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}

}
