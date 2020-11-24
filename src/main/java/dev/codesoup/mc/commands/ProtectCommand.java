package dev.codesoup.mc.commands;

import java.util.UUID;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;

public class ProtectCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/protect";
	
	public ProtectCommand(CustomMod mod) {
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
		
		if(claimer == null) {
			mod.getClaims().setClaim(chunk.x, chunk.z, UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
		} else {
			mod.getClaims().unclaim(chunk.x, chunk.z);
		}
		
	}
	
	@Override
	public String getName() {
		return "protect";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
	
}
