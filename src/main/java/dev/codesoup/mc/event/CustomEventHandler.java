package dev.codesoup.mc.event;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.Nation;
import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.PowerManager;
import dev.codesoup.mc.mcws.messages.PlayerChatMessage;
import dev.codesoup.mc.mcws.messages.PlayerDeathMessage;
import dev.codesoup.mc.mcws.messages.PlayerJoinMessage;
import dev.codesoup.mc.mcws.messages.PlayerQuitMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class CustomEventHandler {

	private boolean PVPEnabled;
	private CustomMod mod;
	
	// Whose territory the player is currently standing on
	private Map<EntityPlayer, UUID> occupiedTerritory;
	public Map<UUID, Integer> numPeopleOnClaim; 
	
	public CustomEventHandler(CustomMod mod) {
		this.PVPEnabled = true;
		this.mod = mod;
		this.occupiedTerritory = new WeakHashMap<EntityPlayer, UUID>();
		this.numPeopleOnClaim = new HashMap<UUID, Integer>();
	}
	
	public boolean PVPEnabled() {
		return this.PVPEnabled;
	}
	
	public boolean togglePVP() {
		this.PVPEnabled = !this.PVPEnabled;
		return this.PVPEnabled;
	}
	
	public void incrementClaim(UUID uuid) {
		if(numPeopleOnClaim.get(uuid) == null) {
			numPeopleOnClaim.put(uuid, 1);
		} else {
			numPeopleOnClaim.put(uuid, numPeopleOnClaim.get(uuid) + 1);
		}
	}
	
	public void decrementClaim(UUID uuid) {
		if(numPeopleOnClaim.get(uuid) == null) {
			numPeopleOnClaim.put(uuid, 0);
		} else {
			numPeopleOnClaim.put(uuid, Math.max(numPeopleOnClaim.get(uuid) - 1, 0));
		}
	}

	private void onEnterClaim(UUID claimer, EntityPlayer player) {
		
		if(claimer.equals(player.getUniqueID())) {
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are now on your own territory."));
			return;
		}
		
		incrementClaim(claimer);
		
		boolean allied = mod.getNationManager().sameNation(player.getUniqueID(), claimer);
		String color = allied ? TextFormatting.AQUA.toString() : (TextFormatting.RED.toString() + TextFormatting.BOLD);
		
		// send message to player
		GameProfile profile = mod.getServer().getPlayerProfileCache().getProfileByUUID(claimer);
		if(profile != null) {
			
			player.sendMessage(new TextComponentString(String.format("%sYou are now on %s's territory.", color, profile.getName())));
		
			// send message to claimer
			EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getServer().getPlayerList().getPlayerByUUID(claimer);
			if(claimerPlayer != null) {
				claimerPlayer.sendMessage(new TextComponentString(String.format("%s%s has stepped onto your territory!", color, player.getName())));
			}
			
		}
		
	}
	
	private void onExitClaim(UUID claimer, EntityPlayer player) {
		
		// Tell them that the player left
		if(!claimer.equals(player.getUniqueID())) {
			EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getServer().getPlayerList().getPlayerByUUID(claimer);
			if(claimerPlayer != null && !claimerPlayer.equals(player)) {
				claimerPlayer.sendMessage(new TextComponentString("§7§o" + player.getName() + " left your territory."));
			}
		}
		
	}
	
	private void onEnterWilderness(EntityPlayer player) {
		
		player.sendMessage(new TextComponentString(TextFormatting.GOLD + "You are now in the wilderness."));
		
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
		if(!(target instanceof EntityPlayer) && mod.getClaims().shouldProtect(event.getEntity().getEntityWorld(), event.getEntity().getPosition(), player.getUniqueID())) {
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

		EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
		UUID curChunkClaimer = this.mod.getClaims().getClaim(event.getNewChunkX(), event.getNewChunkZ());
		UUID prevChunkClaimer = occupiedTerritory.get(player);
		this.occupiedTerritory.put(player, curChunkClaimer);
		
		// If the player is entering an unclaimed chunk...
		if(curChunkClaimer == null && prevChunkClaimer != null) {
	
			onExitClaim(prevChunkClaimer, player);
			onEnterWilderness(player);
			
		} else if(!curChunkClaimer.equals(prevChunkClaimer)) {
		
			onEnterClaim(curChunkClaimer, player);
			if(prevChunkClaimer != null) {
				onExitClaim(prevChunkClaimer, player);
			}
			
		}
		
	}
	
	@SubscribeEvent
	public void playerLoggedInEvent(PlayerLoggedInEvent event) {
		
		if(!(event.player instanceof EntityPlayerMP))
			return;
		
		this.mod.getNationManager().listInvitations((EntityPlayerMP)event.player, false);
		
		// MCWS integration
		mod.getWSServer().broadcastMessage(new PlayerJoinMessage(event));
		
	}
	
	@SubscribeEvent
	public void playerLoggedOutEvent(PlayerLoggedOutEvent event) {
		
		// MCWS integration
		mod.getWSServer().broadcastMessage(new PlayerQuitMessage(event));
		
	}
	
	@SubscribeEvent
	public void nameFormatEvent(NameFormat event) {		
		EntityPlayer player = event.getEntityPlayer();
		String name = this.mod.getNationManager().getName(player);
		event.setDisplayname(name);
		player.setCustomNameTag(name);
	}
	
	@SubscribeEvent
	public void chatEvent(ServerChatEvent event) {
	
		event.setComponent(new TextComponentString(event.getPlayer().getDisplayNameString() + ": " + event.getMessage()));
		
		// MCWS integration
		mod.getWSServer().broadcastMessage(new PlayerChatMessage(event));
	
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
	public void livingDeathEvent(LivingDeathEvent event) {
		
		if(!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().getEntityWorld().isRemote) 
			return;
		
		Entity source = event.getSource().getTrueSource();
		EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
		PowerManager pm = mod.getPowerManager();
		
		if(source instanceof EntityPlayer) {
			
			EntityPlayerMP killer = (EntityPlayerMP)event.getSource().getTrueSource();
			
			Nation alliance = mod.getNationManager().getNation(killer);
			if(alliance.getMembers().contains(player.getUniqueID())) {
			
				// remove power from killer
				pm.removePower(killer, 10);

			} else {
			
				// remove power from killed
				pm.removePower(player, 5);
				
				// give power to killer
				int powerDiff = pm.getTotalPower(player) - pm.getTotalPower(killer);
				int power = (int)Math.max(3 * Math.sqrt(powerDiff), 5);
				pm.addPower(killer.getUniqueID(), power);
				
				// distribute power to members of alliance
				int distPower = Math.max(powerDiff / 4, 1);
				for(UUID uuid: alliance.getMembers()) {
					if(!uuid.equals(killer.getUniqueID())) {
						pm.addPower(uuid, distPower);
					}
				}
				
			}
			
		}
		
		BlockPos pos = player.getPosition();
		player.sendMessage(new TextComponentString(String.format("%sYou died at (%d, %d)", TextFormatting.RED, pos.getX(), pos.getZ())));
		
		mod.getWSServer().broadcastMessage(new PlayerDeathMessage(event));
		
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
		
		if(event.getWorld().isRemote) {
			return;
		}
		
		Entity entity = event.getEntity();
		if(entity instanceof EntityMob && Math.random() > 0.75) {

			// Deny the spawn
			event.setCanceled(true);
			
			// Spawn pigman
			EntityPigZombie pigman = new EntityPigZombie(event.getWorld());
			pigman.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
			event.getWorld().spawnEntity(pigman);
			
		}
		
	}
	
	@SubscribeEvent
	public void livingDropsEvent(LivingDropsEvent event) {
		
		if(event.getEntity() instanceof EntityPigZombie) {

			event.setCanceled(true);
			
			int which = (int)Math.floor(Math.random() * 7);
			Item item = null; int quantityMax = 0;
			switch(which) {
				case 0: item = Items.BLAZE_ROD; quantityMax = 5; break;
				case 1: item = Items.GHAST_TEAR; quantityMax = 3; break;
				case 2: item = Items.NETHER_WART; quantityMax = 5; break;
				case 3: item = Item.getItemFromBlock(Blocks.NETHERRACK); quantityMax = 16; break;
				case 4: item = Item.getItemFromBlock(Blocks.SOUL_SAND); quantityMax = 16; break;
				case 5: item = Items.QUARTZ; quantityMax = 32; break;
				case 6: item = Item.getItemFromBlock(Blocks.MAGMA); quantityMax = 16; break;
			}
			
			event.getEntity().dropItem(item, (int)Math.floor(Math.random() * quantityMax));
			
		}
		
	}
	
}
