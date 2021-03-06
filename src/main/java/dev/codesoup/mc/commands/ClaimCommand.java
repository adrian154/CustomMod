package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;

public class ClaimCommand extends ModCommandBase {

	private final static String USAGE = "/claim";
	
	public ClaimCommand(CustomMod mod) {
		super(mod, "claim", "c", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		mod.getClaimsManager().claim(player, chunk, false);
		
	}

	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}

}
