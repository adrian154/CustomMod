package dev.codesoup.mc.event;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.GenericToggleManager;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.PowerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class CustomEventHandler {

	private boolean PVPEnabled;
	private CustomMod mod;
	
	// Whose territory the player is currently standing on
	private Map<EntityPlayer, UUID> occupiedTerritory;
	private Map<EntityPlayer, Long> invokeBaseTime;
	private transient GenericToggleManager isAutoclaiming;

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
		this.invokeBaseTime = new WeakHashMap<>();
	}
	
	public boolean PVPEnabled() {
		return this.PVPEnabled;
	}
	
	public boolean togglePVP() {
		this.PVPEnabled = !this.PVPEnabled;
		return this.PVPEnabled;
	}

	public GenericToggleManager getAutoclaimManager() {
		return this.isAutoclaiming;
		
	}
	
	private void onEnterClaim(UUID claimer, EntityPlayer player) {

		boolean allied = mod.getNationManager().sameNation(player.getUniqueID(), claimer);
		String format = allied ? TextFormatting.AQUA.toString() : TextFormatting.RED.toString() + TextFormatting.BOLD;
		
		GameProfile claimerProfile = mod.getProfile(claimer);
		if(claimerProfile == null) return;
			
		if(claimer.equals(player.getUniqueID())) {
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are now on your own territory."));
		} else {
			player.sendMessage(new TextComponentString(String.format("%sYou are now on %s's territory.", format, claimerProfile.getName())));
		}
		
		// send other player message
		if(!player.isSpectator() && !claimer.equals(player.getUniqueID())) {
			
			EntityPlayer claimerPlayer = mod.getPlayer(claimer);
			if(claimerPlayer != null) {
				claimerPlayer.sendMessage(new TextComponentString(String.format("%s%s has entered your territory.", format, player.getName())));
			}
			
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
	public void onLivingHurt(LivingHurtEvent event) {
		
		if(event.getAmount() > 0) {
			
			Entity source = event.getSource().getImmediateSource();
			if(source != null && source instanceof EntityPlayerMP) {
			
				EntityPlayerMP player = (EntityPlayerMP)source;
				if(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemAxe) {
					
					ItemAxe axe = (ItemAxe)player.getHeldItem(EnumHand.MAIN_HAND).getItem();
					int id = Item.getIdFromItem(axe);
					
					float damage = 1.0F;
					if(id == 275) damage = 5.0F;
					if(id == 258) damage = 6.0F;
					if(id == 279) damage = 7.0F;
					
					event.setAmount(damage);
					
				}
			
			}
			
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
	
	@SubscribeEvent
	public void onBabyEntitySpawn(BabyEntitySpawnEvent event) {
		
		Chunk chunk = event.getChild().getEntityWorld().getChunkFromBlockCoords(event.getParentA().getPosition());
		
		int mobs = 0;
		for(ClassInheritanceMultiMap<Entity> list: chunk.getEntityLists()) {
			mobs += list.size();
		}
		
		System.out.println(mobs);
		if(mobs > 20) {
			event.setCanceled(true);
			
			if(Math.random() > 0.5) {
				
				EntityLiving toKill;
				if(Math.random() > 0.5)
					toKill = event.getParentA();
				else
					toKill = event.getParentB();
				
				toKill.attackEntityFrom(DamageSource.MAGIC, toKill.getHealth());
				
			}
			
		}
		
	}
	
	public void startBaseTPTimer(EntityPlayer player) {
		invokeBaseTime.put(player, player.getEntityWorld().getTotalWorldTime() + 20 * 10);
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		
		if(invokeBaseTime.containsKey(event.player) && (event.player.lastTickPosX != event.player.posX || event.player.lastTickPosY != event.player.posY || event.player.lastTickPosZ != event.player.posZ)) {
			invokeBaseTime.remove(event.player);
			event.player.sendMessage(new TextComponentString(TextFormatting.RED + "Your teleport was canceled since you moved."));
		}
		
	}
	
	@SubscribeEvent
	public void worldTickEvent(WorldTickEvent event) {
		
		if(event.phase != TickEvent.Phase.END) return;
		
		for(Map.Entry<EntityPlayer, Long> entry: invokeBaseTime.entrySet()) {
			
			if(event.world.getTotalWorldTime() == entry.getValue()) {
				
				invokeBaseTime.remove(entry.getKey());
				
				EntityPlayer player = entry.getKey();
				BlockPos pos = player.getBedLocation();
				if(pos != null) {
					mod.getServer().getCommandManager().executeCommand(mod.getServer(), String.format("/tp %s %d %d %d", player.getName(), pos.getX(), pos.getY(), pos.getZ()));
				}
				
			}
			
		}
		
	}

}
