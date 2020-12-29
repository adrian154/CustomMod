package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.GenericToggleManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoclaimCommand extends ModCommandBase {

	private static final String USAGE = "/autoclaim";

	private GenericToggleManager isAutoclaiming; 
	
	public AutoclaimCommand(CustomMod mod) {
		super(mod, "autoclaim", "ac", 0);
		this.isAutoclaiming = new GenericToggleManager();		
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		EntityPlayerMP player = assertIsPlayer(sender);
		if(isAutoclaiming.toggle(player)) {
			player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Started autoclaiming."));
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Stopped autoclaiming."));
		}

	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@SubscribeEvent
	public void onEnterChunk(EntityEvent.EnteringChunk event) {
		if(event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			if(isAutoclaiming.get(player)) {
				mod.getClaimsManager().claim(player, player.getEntityWorld().getChunkFromBlockCoords(player.getPosition()), true);
			}
		}
	}
	
}
