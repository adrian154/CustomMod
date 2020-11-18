package dev.codesoup.mc.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomEventHandler {

	private boolean PVPEnabled;
	
	public CustomEventHandler() {
		this.PVPEnabled = true;
	}
	
	public boolean PVPEnabled() {
		return this.PVPEnabled;
	}
	
	public boolean togglePVP() {
		this.PVPEnabled = !this.PVPEnabled;
		return this.PVPEnabled;
	}
	
	@SubscribeEvent
	public void attackEntity(AttackEntityEvent event) {
		Entity target = event.getTarget();
		if(target instanceof EntityPlayer) {
			event.setCanceled(true);
			event.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.RED + "PVP is disabled."));
		}
	}
	
}
