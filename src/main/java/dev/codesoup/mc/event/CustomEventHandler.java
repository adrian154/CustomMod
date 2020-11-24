package dev.codesoup.mc.event;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.Alliance;
import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.PowerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CustomEventHandler {

	private boolean PVPEnabled;
	private CustomMod mod;
	
	// Whose territory the player is currently standing on
	private Map<EntityPlayer, UUID> occupiedTerritory;
	private transient List<UUID> toKeepInventory;
	
	public CustomEventHandler(CustomMod mod) {
		this.PVPEnabled = true;
		this.mod = mod;
		this.occupiedTerritory = new WeakHashMap<EntityPlayer, UUID>();
		this.toKeepInventory = new ArrayList<UUID>();
	}
	
	public boolean PVPEnabled() {
		return this.PVPEnabled;
	}
	
	public boolean togglePVP() {
		this.PVPEnabled = !this.PVPEnabled;
		return this.PVPEnabled;
	}

	@SubscribeEvent
	public void attackEntityEvent(AttackEntityEvent event) {

		if(event.getEntity().getEntityWorld().isRemote)
			return;
		
		Entity target = event.getTarget();
		if(target instanceof EntityPlayer && !this.PVPEnabled) {
			event.setCanceled(true);
			event.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.RED + "PVP is disabled."));
		}
		
		EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
		if(mod.getClaims().shouldProtect(event.getEntity().getEntityWorld(), event.getEntity().getPosition(), player.getUniqueID())) {
			event.setCanceled(true);
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
				
				if(curChunkClaimer.equals(player.getUniqueID())) {
					player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are now on your own territory."));
				} else {
				
					boolean allied = mod.getAllianceManager().areAllied(player.getUniqueID(), curChunkClaimer);
					String color = allied ? TextFormatting.AQUA.toString() : (TextFormatting.RED.toString() + TextFormatting.BOLD);
					
					GameProfile profile = event.getEntity().getEntityWorld().getMinecraftServer().getPlayerProfileCache().getProfileByUUID(curChunkClaimer);
					player.sendMessage(new TextComponentString(String.format("%sYou are now on %s's territory.", color, profile.getName())));
				
					EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getServer().getPlayerList().getPlayerByUUID(curChunkClaimer);
					if(claimerPlayer != null) {
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
		
		if(!(event.player instanceof EntityPlayerMP))
			return;
		
		this.mod.getAllianceManager().listInvitations((EntityPlayerMP)event.player, false);
		
	}
	
	@SubscribeEvent
	public void playerLoggedOutEvent(PlayerLoggedOutEvent event) {
		
		// TODO
		
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
		try {
			this.mod.saveAll();
		} catch(FileNotFoundException exception) {
			mod.logger.error("FAILED TO SAVE CONFIGS, THIS IS REALLY REALLY BAD!");
		}
	}
	
	@SubscribeEvent
	public void playerDropsEvent(PlayerDropsEvent event) {
		if(toKeepInventory.contains(event.getEntityPlayer().getUniqueID())) {
			for(EntityItem entityItem: event.getDrops()) {
				ItemStack stack = entityItem.getItem();
				if(stack.getItem() instanceof ItemArmor) {
					// TODO: Save armor
				} else {
					event.getEntityPlayer().inventory.addItemStackToInventory(stack);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void playerCloneEvent(PlayerEvent.Clone event) {
		
		if(event.getOriginal() != null && toKeepInventory.contains(event.getEntityPlayer().getUniqueID())) {
			
			EntityPlayer original = event.getOriginal();
			
			// TODO: Transfer armor
			
			for(ItemStack stack: original.inventory.mainInventory) {
				event.getEntityPlayer().inventory.addItemStackToInventory(stack);
			}
			
			toKeepInventory.remove(event.getEntityPlayer().getUniqueID());
			
		}
		
	}
	
	@SubscribeEvent
	public void livingDeathEvent(LivingDeathEvent event) {
		
		if(!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().getEntityWorld().isRemote) 
			return;
		
		Entity source = event.getSource().getTrueSource();
		PowerManager pm = mod.getPowerManager();
		if(source instanceof EntityPlayer) {
			
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			EntityPlayerMP killer = (EntityPlayerMP)event.getSource().getTrueSource();
			
			Alliance alliance = mod.getAllianceManager().getAlliance(killer);
			if(alliance.getMembers().contains(player.getUniqueID())) {
			
				// remove power from killer
				pm.removePower(killer);
				killer.sendMessage(new TextComponentString(TextFormatting.RED + "Don't kill people in your alliance, they won't drop their inventory!"));
				toKeepInventory.add(player.getUniqueID());
				
			} else {
			
				// remove power from killed
				pm.removePower(player);
				
				// give power to killer
				int powerDiff = pm.getTotalPower(player) - pm.getTotalPower(killer);
				int power = (int)Math.max(Math.sqrt(powerDiff), 0) + 5;
				pm.addPower(killer.getUniqueID(), power);
				
				// distribute power to members of alliance
				int distPower = Math.max(powerDiff / 4, 1);
				for(UUID uuid: alliance.getMembers()) {
					if(uuid != killer.getUniqueID())
						pm.addPower(uuid, distPower);
				}
				
			}
			
		}
		
	}
	
}
