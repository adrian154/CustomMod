package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;

public class ClaimCommand extends CommandBase {

	private CustomMod mod;
	
	public ClaimCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		mod.getClaims().setClaim(chunk.x, chunk.z, player.getUniqueID());
		
	}
	
	@Override
	public String getName() {
		return "claim";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "command.claim.usage";
	}
	
}
