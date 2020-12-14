package dev.codesoup.mc.commands;

import java.util.UUID;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class SetSpawnCommand extends ModCommandBase {
	
	private final static String USAGE = "/setspawn";
	
	public SetSpawnCommand(CustomMod mod) {
		super(mod, "setspawn", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		
		UUID claim = mod.getClaims().getClaim(chunk.x, chunk.z);
		if(claim != null && claim.equals(player.getUniqueID())) {
			BlockPos pos = sender.getPosition();
			server.getCommandManager().executeCommand(mod.getServer(), String.format("/spawnpoint %s %d %d %d", sender.getName(), pos.getX(), pos.getY(), pos.getZ()));
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Set your spawnpoint."));
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You can only permanently set your spawn on your own territory."));
		}
	
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
