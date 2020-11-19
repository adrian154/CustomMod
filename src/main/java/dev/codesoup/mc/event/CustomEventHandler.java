package dev.codesoup.mc.event;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CustomEventHandler {

	private boolean PVPEnabled;
	private CustomMod mod;
	
	// Whose territory the player is currently standing on
	private Map<EntityPlayer, UUID> occupiedTerritory;
	
	public CustomEventHandler(CustomMod mod) {
		this.PVPEnabled = true;
		this.mod = mod;
		this.occupiedTerritory = new WeakHashMap<EntityPlayer, UUID>();
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
	
		/*
		EntityPlayer player = event.getPlayer();
		BlockPos pos = event.getPos();
		Chunk chunk = event.getWorld().getChunkFromBlockCoords(pos);
		
		System.out.println(chunk.x + ", " + chunk.z);
		*/
		
	}

	@SubscribeEvent
	public void enteringChunkEvent(EntityEvent.EnteringChunk event) {
		
		if(!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().world.isRemote) {
			return;
		}
		
		UUID curChunkClaimer = this.mod.getClaims().getClaim(event.getNewChunkX(), event.getNewChunkZ());
		EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
		
		if(curChunkClaimer != this.occupiedTerritory.get(player)) {
			
			if(curChunkClaimer != null) {
				GameProfile profile = event.getEntity().getEntityWorld().getMinecraftServer().getPlayerProfileCache().getProfileByUUID(curChunkClaimer);
				player.sendMessage(new TextComponentString(String.format("You are now on %s's territory.", profile.getName())));
			} else {
				player.sendMessage(new TextComponentString("You are now in the wilderness."));
			}
			
			this.occupiedTerritory.put(player, curChunkClaimer);
			
		}
		
	}
	
}
