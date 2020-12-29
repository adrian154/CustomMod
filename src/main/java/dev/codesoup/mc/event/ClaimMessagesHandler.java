package dev.codesoup.mc.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.RequiresMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Handles claim entry/exits
public class ClaimMessagesHandler extends RequiresMod {

	private Map<EntityPlayer, UUID> occupiedTerritory;
	
	public ClaimMessagesHandler(CustomMod mod) {
		super(mod);
		this.occupiedTerritory = new HashMap<>();
	}
	

	private static class CustomBossInfo extends BossInfo {
		
		private static final Map<TextFormatting, BossInfo.Color> COLOR_MAPPINGS;
		
		static {
			COLOR_MAPPINGS = new HashMap<>();
			COLOR_MAPPINGS.put(TextFormatting.AQUA, BossInfo.Color.BLUE);
			COLOR_MAPPINGS.put(TextFormatting.BLACK, BossInfo.Color.WHITE);
			COLOR_MAPPINGS.put(TextFormatting.BLUE, BossInfo.Color.BLUE);
			COLOR_MAPPINGS.put(TextFormatting.DARK_AQUA, BossInfo.Color.BLUE);
			COLOR_MAPPINGS.put(TextFormatting.DARK_BLUE, BossInfo.Color.BLUE);
			COLOR_MAPPINGS.put(TextFormatting.DARK_GRAY, BossInfo.Color.WHITE);
			COLOR_MAPPINGS.put(TextFormatting.DARK_GREEN, BossInfo.Color.GREEN);
			COLOR_MAPPINGS.put(TextFormatting.DARK_PURPLE, BossInfo.Color.PURPLE);
			COLOR_MAPPINGS.put(TextFormatting.DARK_RED, BossInfo.Color.RED);
			COLOR_MAPPINGS.put(TextFormatting.GOLD, BossInfo.Color.YELLOW);
			COLOR_MAPPINGS.put(TextFormatting.GRAY, BossInfo.Color.WHITE);
			COLOR_MAPPINGS.put(TextFormatting.GREEN, BossInfo.Color.GREEN);
			COLOR_MAPPINGS.put(TextFormatting.LIGHT_PURPLE, BossInfo.Color.PURPLE);
			COLOR_MAPPINGS.put(TextFormatting.RED, BossInfo.Color.RED);
			COLOR_MAPPINGS.put(TextFormatting.WHITE, BossInfo.Color.WHITE);
			COLOR_MAPPINGS.put(TextFormatting.YELLOW, BossInfo.Color.YELLOW);
		}
		
		public CustomBossInfo(CustomMod mod, Nation nation) {
			super(nation.getID(), new TextComponentString(nation.getName()), COLOR_MAPPINGS.get(nation.getColor()), BossInfo.Overlay.PROGRESS);
		}
		
		public CustomBossInfo(CustomMod mod, UUID player) {
			super(player, new TextComponentString(mod.getName(player)), BossInfo.Color.WHITE, BossInfo.Overlay.PROGRESS);
		}
		
	}
	
	private void doEnemyBossbar(EntityPlayerMP player, Nation nation, boolean state) {
		sendBossbarPacket(player, new CustomBossInfo(mod, nation), state);
	}
	
	private void doEnemyBossbar(EntityPlayerMP player, UUID claimer, boolean state) {
		sendBossbarPacket(player, new CustomBossInfo(mod, claimer), state);
	}
	
	private void sendBossbarPacket(EntityPlayerMP player, CustomBossInfo bossInfo, boolean state) {
		player.connection.sendPacket(new SPacketUpdateBossInfo(state ? SPacketUpdateBossInfo.Operation.ADD : SPacketUpdateBossInfo.Operation.REMOVE, bossInfo));
	}
	
	private void onEnterClaim(UUID claimer, EntityPlayerMP player) {

		boolean allied = mod.getNationManager().sameNation(player.getUniqueID(), claimer);

		GameProfile claimerProfile = mod.getProfile(claimer);
		if(claimerProfile == null) return;
		
		Nation nation = mod.getNationManager().getNation(claimer);
		
		if(claimer.equals(player.getUniqueID())) {
		
			// Entering your own territory is always a status.
			player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "You are now on your own territory."), true);
		
		} else {
			
			// Send notification as status (minor) if allied and as a full message if not.
			if(allied)
				player.sendStatusMessage(new TextComponentString(String.format("%sYou are now on %s's territory.", TextFormatting.AQUA, claimerProfile.getName())), allied);
			else
				if(nation != null)
					doEnemyBossbar(player, nation, true);
				else
					doEnemyBossbar(player, claimer, true);
				
		}
		
		// send other player message
		if(!player.isSpectator() && !claimer.equals(player.getUniqueID())) {
			
			EntityPlayer claimerPlayer = mod.getPlayer(claimer);
			if(claimerPlayer != null && !allied) {
				claimerPlayer.sendMessage(new TextComponentString(String.format("%s%s has entered your territory.", TextFormatting.RED, player.getName())));
			}
			
		}
		
	}
	
	private void onExitClaim(UUID claimer, EntityPlayerMP player) {
		
		if(player.isSpectator()) return;
		
		// Tell them that the player left
		if(!claimer.equals(player.getUniqueID())) {
			
			EntityPlayerMP claimerPlayer = (EntityPlayerMP)this.mod.getPlayer(claimer);
			
			if(claimerPlayer != null && !claimerPlayer.equals(player) && !mod.getNationManager().sameNation(player, claimerPlayer)) {
				claimerPlayer.sendMessage(new TextComponentString("§7§o" + player.getName() + " left your territory."));
			}
			
		}
		
		if(claimer != null) {
			
			Nation nation = mod.getNationManager().getNation(claimer);
			if(nation != null)
				doEnemyBossbar(player, nation, false);
			else
				doEnemyBossbar(player, claimer, false);
			
		}
		
	}
	
	private void onEnterWilderness(EntityPlayerMP player) {
		
		// Entering the wilderness is also a status.
		player.sendStatusMessage(new TextComponentString(TextFormatting.GOLD + "You are now in the wilderness."), true);
		
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
		
	}
	
}
