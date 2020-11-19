package dev.codesoup.mc.event;

import dev.codesoup.mc.CustomMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CustomEventHandler {

	private boolean PVPEnabled;
	private CustomMod mod;
	
	public CustomEventHandler(CustomMod mod) {
		this.PVPEnabled = true;
		this.mod = mod;
	}
	
	public boolean PVPEnabled() {
		return this.PVPEnabled;
	}
	
	public boolean togglePVP() {
		this.PVPEnabled = !this.PVPEnabled;
		return this.PVPEnabled;
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void attackEntityEvent(AttackEntityEvent event) {
		Entity target = event.getTarget();
		if(target instanceof EntityPlayer && !this.PVPEnabled) {
			event.setCanceled(true);
			event.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.RED + "PVP is disabled."));
		}
	}
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
	
		EntityPlayer player = event.getPlayer();
		BlockPos pos = event.getPos();
		Chunk chunk = event.getWorld().getChunkFromBlockCoords(pos);
		
		System.out.println(chunk.x + ", " + chunk.z);
		
	}
	
}
