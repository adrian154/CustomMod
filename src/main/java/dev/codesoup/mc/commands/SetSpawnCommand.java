package dev.codesoup.mc.commands;

import java.util.UUID;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class SetSpawnCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/setspawn";
	
	public SetSpawnCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
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
	public String getName() {
		return "setspawn";
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
