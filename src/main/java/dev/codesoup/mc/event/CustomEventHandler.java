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
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
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
	public void attackEvent(AttackEntityEvent event) {
		
		if(!(event.getEntityPlayer() instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
		if(mod.getClaims().shouldProtect(event.getEntity().getEntityWorld(), event.getEntity().getPosition(), player.getUniqueID())) {
			event.setCanceled(true);
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
				
					boolean allied = mod.getAllianceManager().areAllied(player.getUniqueID(), curChunkClaimer);
					String color = allied ? TextFormatting.AQUA.toString() : (TextFormatting.RED.toString() + TextFormatting.BOLD);
					
					GameProfile profile = event.getEntity().getEntityWorld().getMinecraftServer().getPlayerProfileCache().getProfileByUUID(curChunkClaimer);
					player.sendMessage(new TextComponentString(String.format("%sYou are now on %s's territory.", color, profile.getName())));
				
					EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getServer().getPlayerList().getPlayerByUUID(curChunkClaimer);
					if(claimerPlayer != null && curChunkClaimer != player.getUniqueID()) {
						claimerPlayer.sendMessage(new TextComponentString(String.format("%s%s has stepped onto your territory!", color, player.getName())));
					}
					
				}
				
			} else {
				player.sendMessage(new TextComponentString(TextFormatting.GOLD + "You are now in the wilderness."));
			}
			
			this.occupiedTerritory.put(player, curChunkClaimer);
			
		}
		
	}
	
	@SubscribeEvent
	public void playerLoggedInEvent(PlayerLoggedInEvent event) {
		
	}
	
	@SubscribeEvent
	public void playerLoggedOutEvent(PlayerLoggedOutEvent event) {
		
	}
	
	@SubscribeEvent
	public void nameFormatEvent(NameFormat event) {
		
		EntityPlayer player = event.getEntityPlayer();
		String name = this.mod.getAllianceManager().getName(player);
		event.setDisplayname(name);
		player.setCustomNameTag(name);

	}
	
	@SubscribeEvent
	public void chatEvent(ServerChatEvent event) {
		event.setComponent(new TextComponentString(event.getPlayer().getDisplayNameString() + ": " + event.getMessage()));
	}
	
	@SubscribeEvent
	public void saveEvent(WorldEvent.Save event) {
		this.mod.saveAll();
	}
	
}
