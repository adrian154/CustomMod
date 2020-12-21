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

public class ProtectCommand extends ModCommandBase {

	private final static String USAGE = "/protect";
	
	// Protected chunks are claimed under a UUID that will never join the server
	// In this case, it's Notch :)
	public final static UUID PROTECTED_UUID = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
	
	public ProtectCommand(CustomMod mod) {
		super(mod, "protect", 4);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		EntityPlayerMP player = assertIsPlayer(sender);
		Chunk chunk = player.getEntityWorld().getChunkFromBlockCoords(player.getPosition());
		UUID claimer = mod.getClaimsManager().getClaim(chunk.x, chunk.z);
		
		if(claimer == null) {
			mod.getClaimsManager().setClaim(chunk.x, chunk.z, PROTECTED_UUID);
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "This chunk is now protected."));
		} else {
			mod.getClaimsManager().unclaim(chunk.x, chunk.z);
			player.sendMessage(new TextComponentString(TextFormatting.GOLD + "This chunk is no longer protected."));
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
