package dev.codesoup.mc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class PowerManager extends Manager {

	private Map<UUID, Integer> totalPower;

	public PowerManager(CustomMod mod) {
		super(mod);
		totalPower = new HashMap<UUID, Integer>();
	}
	
	public int getTotalPower(UUID uuid) {
		if(!totalPower.containsKey(uuid))
			totalPower.put(uuid, 16);
		return totalPower.get(uuid);
	}
	
	public int getTotalPower(EntityPlayer player) {
		return getTotalPower(player.getUniqueID());
	}
	
	public int getFreePower(EntityPlayer player) {
		return getTotalPower(player.getUniqueID());
	}
	
	public void addPower(UUID uuid, int power) {
		
		EntityPlayerMP player;
		if((player = mod.getServer().getPlayerList().getPlayerByUUID(uuid)) != null) {
			player.sendMessage(new TextComponentString(TextFormatting.ITALIC.toString() + TextFormatting.GREEN.toString() + "+" + power + " power"));
		}
		
		totalPower.replace(uuid, totalPower.get(uuid) + power);
	}
	
	public boolean removeFreePower(EntityPlayerMP player) {
		if(mod.getClaims().getNumClaims(player.getUniqueID()) >= getTotalPower(player)) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have enough power!"));
			return false;
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.ITALIC.toString() + TextFormatting.RED.toString() + "-1 power"));
			return true;
		}
	}
	
	public void removePower(EntityPlayerMP player) {
		
		UUID uuid = player.getUniqueID();
		
		int actualAmt = getTotalPower(player) > 0 ? 1 : 0;
		if(actualAmt == 1) {
			player.sendMessage(new TextComponentString(TextFormatting.ITALIC.toString() + TextFormatting.RED.toString() + "-1 power"));
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.ITALIC.toString() + TextFormatting.GRAY.toString() + "-0 power"));
		}
		
		if(mod.getClaims().getNumClaims(uuid) > getTotalPower(uuid) - 1) {
			Pair unclaimed = mod.getClaims().unclaimLast(uuid);
			player.sendMessage(new TextComponentString(String.format("%sWARNING: Your claim at (%d, %d) was unclaimed since you have lost too much power!", TextFormatting.RED, unclaimed.A * 16, unclaimed.B * 16)));
		}
		
		totalPower.replace(uuid, getTotalPower(uuid) - actualAmt);
		
	}
	
}
