package dev.codesoup.mc.commands;

import java.util.UUID;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class UnclaimCommand extends ModCommandBase {

	private final static String USAGE = "/unclaim";
	
	public UnclaimCommand(CustomMod mod) {
		super(mod, "unclaim", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		EntityPlayerMP player = assertIsPlayer(sender);
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		
		UUID claimer = mod.getClaims().getClaim(chunk.x, chunk.z);
		if(claimer == null || !claimer.equals(player.getUniqueID())) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't own this territory."));
			return;
		}
		
		mod.getClaims().unclaim(chunk.x, chunk.z);
		player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Chunk unclaimed!"));
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
