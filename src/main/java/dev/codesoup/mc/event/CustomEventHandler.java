package dev.codesoup.mc.event;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.PowerManager;
import dev.codesoup.mc.RequiresMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

// Kitchen-sink for small gameplay changes
public class CustomEventHandler extends RequiresMod {

	// Cooldown field
	private static Field targetField = EntityLivingBase.class.getDeclaredFields()[24]; 
	
	static {
		targetField.setAccessible(true);
	}
	
	public CustomEventHandler(CustomMod mod) {
		super(mod);
	}
	
	// Nerf axe damage
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
			for(Entity entity: list) {
				if(entity instanceof EntityAgeable)
					mobs += list.size();
			}
		}
		
		if(mobs > 15) {
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
	

}
