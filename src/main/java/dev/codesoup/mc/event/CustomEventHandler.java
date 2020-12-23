package dev.codesoup.mc.event;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.GenericToggleManager;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.PowerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class CustomEventHandler {

	private boolean PVPEnabled;
	private CustomMod mod;
	
	// Whose territory the player is currently standing on
	private Map<EntityPlayer, UUID> occupiedTerritory;
	private transient GenericToggleManager isAutoclaiming;
	public Map<UUID, Integer> numPeopleOnClaim; 
	
	// Cooldown field
	private static Field targetField = EntityLivingBase.class.getDeclaredFields()[24]; 
	
	static {
		targetField.setAccessible(true);
	}
	
	public CustomEventHandler(CustomMod mod) {
		this.PVPEnabled = true;
		this.mod = mod;
		this.occupiedTerritory = new WeakHashMap<EntityPlayer, UUID>();
		this.isAutoclaiming = new GenericToggleManager();
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
	
	public GenericToggleManager getAutoclaimManager() {
		return this.isAutoclaiming;
	}
	
	private void onEnterClaim(UUID claimer, EntityPlayer player) {
		
		if(player.isSpectator()) return;
		
		if(claimer.equals(player.getUniqueID())) {
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are now on your own territory."));
			return;
		}
		
		boolean allied = mod.getNationManager().sameNation(player.getUniqueID(), claimer);
		String color = allied ? TextFormatting.AQUA.toString() : (TextFormatting.RED.toString() + TextFormatting.BOLD);
		
		// send message to player
		GameProfile profile = mod.getProfile(claimer);
		if(profile != null) {
			
			player.sendMessage(new TextComponentString(String.format("%sYou are now on %s's territory.", color, profile.getName())));
		
			// send message to claimer
			EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getPlayer(claimer);
			if(claimerPlayer != null) {
				claimerPlayer.sendMessage(new TextComponentString(String.format("%s%s has stepped onto your territory!", color, player.getName())));
			}
			
		}
		
		if(!allied) {
			incrementClaim(claimer);
		}
		
	}
	
	private void onExitClaim(UUID claimer, EntityPlayer player) {
		
		if(player.isSpectator()) return;
		
		// Tell them that the player left
		if(!claimer.equals(player.getUniqueID())) {
			
			EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getPlayer(claimer);
			
			if(claimerPlayer != null && !claimerPlayer.equals(player)) {
				claimerPlayer.sendMessage(new TextComponentString("§7§o" + player.getName() + " left your territory."));
			}
			
			// Only decrement if enemy is leaving, since claims can only be incremented by enemies
			if(!mod.getNationManager().sameNation(claimer, player.getUniqueID())) {
				decrementClaim(claimer);
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
		if(!(target instanceof EntityPlayer) && mod.getClaimsManager().shouldProtect(event.getEntity().getEntityWorld(), event.getEntity().getPosition(), player)) {
			event.setCanceled(true);
		}
		
	}

	@SubscribeEvent
	public void interactEvent(PlayerInteractEvent event) {
		if(!event.getWorld().isRemote)
			event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), event.getEntityPlayer()));
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		
		if(!event.getWorld().isRemote)
			event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), event.getPlayer()));
	
		BlockPos pos = event.getPos();
		if(event.getState().getBlock().equals(Blocks.DIAMOND_ORE)) {
			
			String str = String.format("%s mined diamond ore at (%d, %d, %d)", event.getPlayer().getName(), pos.getX(), pos.getY(), pos.getZ());
			mod.logger.info(str);
			
			//EntityPlayerMP player = mod.getPlayer(UUID.fromString("dd59a2b9-083e-49dc-a6c2-4c7b7997dce8"));
			//if(player != null) player.sendMessage(new TextComponentString(str));
			
			mod.broadcastToOps(str);
			
		}
	
	}
	
	@SubscribeEvent
	public void placeEvent(EntityPlaceEvent event) {
		if(event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), player));
		}
	}
	
	@SubscribeEvent
	public void trampleFarmlandEvent(FarmlandTrampleEvent event) {
		if(!event.getWorld().isRemote) {
			if(event.getEntity() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
				event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), player));
			}
		}
	}
	
	@SubscribeEvent
	public void enteringChunkEvent(EntityEvent.EnteringChunk event) {
		
		if(!(event.getEntity() instanceof EntityPlayerMP) || event.getEntity().world.isRemote) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
		UUID prevChunkClaimer = occupiedTerritory.get(player);
		UUID curChunkClaimer = this.mod.getClaimsManager().getClaim(event.getNewChunkX(), event.getNewChunkZ());

		// update current chunk
		this.occupiedTerritory.put(player, curChunkClaimer);
		
		if(prevChunkClaimer != null && !prevChunkClaimer.equals(curChunkClaimer)) {
			onExitClaim(prevChunkClaimer, player);
		}
		
		if(curChunkClaimer != null && !curChunkClaimer.equals(prevChunkClaimer)) {
			onEnterClaim(curChunkClaimer, player);
		}
		
		if(curChunkClaimer == null && prevChunkClaimer != null) {
			onEnterWilderness(player);
		}
		
		if(isAutoclaiming.get(player)) {
			mod.getClaimsManager().claim(player, player.getEntityWorld().getChunkFromBlockCoords(player.getPosition()), true);
		}
		
	}
	
	@SubscribeEvent
	public void playerLoggedInEvent(PlayerLoggedInEvent event) {
		
		if(!(event.player instanceof EntityPlayerMP))
			return;
		
		this.mod.getNationManager().listInvitations((EntityPlayerMP)event.player, false);
		
		Nation nation = mod.getNationManager().getNation(event.player);
		if(nation != null) {
			Scoreboard scoreboard = mod.getScoreboard();
			scoreboard.removePlayerFromTeams(event.player.getName());
			scoreboard.addPlayerToTeam(event.player.getName(), nation.getTeamName());
		}
		
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
	}
	
	@SubscribeEvent
	public void saveEvent(WorldEvent.Save event) {
		try {
			this.mod.saveAll();
		} catch(IOException exception) {
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
		
		if(source instanceof EntityPlayerMP) {
			
			EntityPlayerMP killer = (EntityPlayerMP)source;
			
			Nation alliance = mod.getNationManager().getNation(killer);
			if(alliance != null) {
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
			
		}
		
		BlockPos pos = player.getPosition();
		player.sendMessage(new TextComponentString(String.format("%sYou died at (%d, %d)", TextFormatting.RED, pos.getX(), pos.getZ())));

	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		try {
			if(event.getEntity() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
				if(targetField.getInt(player) < 9999) {
					targetField.set(player, 9999);
				}
			}
		} catch(IllegalAccessException exception) {
			System.out.println("Somehow, an exception occurred while trying to apply old combat. You are probably on the wrong version.");
		}
		
	}

}
