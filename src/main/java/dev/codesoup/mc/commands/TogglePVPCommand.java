package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TogglePVPCommand extends ModCommandBase {

	private final static String USAGE = "/pvp";
	
	private boolean pvpEnabled;
	
	public TogglePVPCommand(CustomMod mod) {
		super(mod, "pvp", 4);
		pvpEnabled = true;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(pvpEnabled = !pvpEnabled) {
			mod.broadcast(TextFormatting.RED + "PVP is now enabled.");
		} else {
			mod.broadcast(TextFormatting.GREEN + "PVP is now disabled.");
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		
		if(event.getEntity().getEntityWorld().isRemote) return;
		
		Entity target = event.getTarget();
		if(target instanceof EntityPlayer && !pvpEnabled) {
			event.setCanceled(true);
			event.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.RED + "PVP is disabled."));
		}
		
	}
	
}
