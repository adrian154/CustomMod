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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
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

	@SubscribeEvent
	public void interactEvent(PlayerInteractEvent event) {
		if(!event.getWorld().isRemote)
			event.setCanceled(mod.getClaims().shouldProtect(event.getWorld(), event.getPos(), event.getEntityPlayer().getUniqueID()));
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isRemote)
			event.setCanceled(mod.getClaims().shouldProtect(event.getWorld(), event.getPos(), event.getPlayer().getUniqueID()));
	}
	
	@SubscribeEvent
	public void placeEvent(EntityPlaceEvent event) {
		if(event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			event.setCanceled(mod.getClaims().shouldProtect(event.getWorld(), event.getPos(), player.getUniqueID()));
		}
	}
	
	@SubscribeEvent
	public void trampleFarmlandEvent(FarmlandTrampleEvent event) {
		if(!event.getWorld().isRemote) {
			if(event.getEntity() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
				event.setCanceled(mod.getClaims().shouldProtect(event.getWorld(), event.getPos(), player.getUniqueID()));
			}
		}
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
				
				if(curChunkClaimer == player.getUniqueID()) {
					player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are now on your own territory."));
				} else {
				
					GameProfile profile = event.getEntity().getEntityWorld().getMinecraftServer().getPlayerProfileCache().getProfileByUUID(curChunkClaimer);
					player.sendMessage(new TextComponentString(String.format(TextFormatting.RED + "You are now on %s's territory.", profile.getName())));
				
					EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getServer().getPlayerList().getPlayerByUUID(curChunkClaimer);
					if(claimerPlayer != null) {
						claimerPlayer.sendMessage(new TextComponentString(String.format("%s%s has stepped onto your territory!", TextFormatting.RED, player.getName())));
					}
					
				}
				
			} else {
				player.sendMessage(new TextComponentString(TextFormatting.GOLD + "You are now in the wilderness."));
			}
			
			this.occupiedTerritory.put(player, curChunkClaimer);
			
		}
		
	}
	
	@SubscribeEvent
	public void saveEvent(WorldEvent.Save event) {
		this.mod.saveAll();
	}
	
}
