package dev.codesoup.mc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class PowerManager extends RequiresMod {

	private Map<UUID, Integer> totalPower;
	
	public PowerManager(CustomMod mod) {
		super(mod);
		totalPower = new HashMap<UUID, Integer>();
		startPassivePowerTask();
	}

	private void startPassivePowerTask() {
		Runnable timerTask = new GivePowerTask();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(timerTask, 0, 60 * 5, TimeUnit.SECONDS);
	} 
	
	public Set<UUID> getKeys() {
		return this.totalPower.keySet();
	}
	
	public int getTotalPower(Nation nation) {
		int totalPower = 0;
		for(UUID uuid: nation.getMembers()) {
			totalPower += this.getTotalPower(uuid);
		}
		return totalPower;
	}
	
	public int getTotalPower(UUID uuid) {
		if(!totalPower.containsKey(uuid))
			totalPower.put(uuid, 12);
		return totalPower.get(uuid);
	}
	
	public int getTotalPower(EntityPlayer player) {
		return getTotalPower(player.getUniqueID());
	}
	
	public int getFreePower(UUID uuid) {
		return getTotalPower(uuid) - mod.getClaimsManager().getNumClaims(uuid);
	}
	
	public int getFreePower(EntityPlayer player) {
		return getFreePower(player.getUniqueID());
	}
	
	private TextComponentString powerDiff(int amt) {
		return new TextComponentString(String.format("%s%d power", amt < 0 ? TextFormatting.RED : (TextFormatting.GREEN + "+"), amt));
	}
	
	public void addPower(EntityPlayerMP player, int power) {
		addPower(player.getUniqueID(), power);
	}
	
	public void addPower(UUID uuid, int power) {
		
		EntityPlayerMP player;
		if((player = mod.getServer().getPlayerList().getPlayerByUUID(uuid)) != null) {
			player.sendMessage(powerDiff(power));
		}
		
		totalPower.replace(uuid, getTotalPower(uuid) + power);
		
	}
	
	public boolean removeFreePower(EntityPlayerMP player, int amount) {
		if(getFreePower(player) < amount) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have enough power!"));
			return false;
		} else {
			removePower(player, amount);
			return true;
		}
	}
	
	public boolean removeClaimPower(EntityPlayer player, int amount) {
		if(getFreePower(player) < amount) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have enough power!"));
			return false;
		} else {
			return true;
		}
	}
	
	public void removePower(EntityPlayerMP player, int amount) {
		
		UUID uuid = player.getUniqueID();
		
		player.sendMessage(powerDiff(-amount));

		int overdraw = amount - getFreePower(player);
		for(int i = 0; i < overdraw; i++) {
			XZPair unclaimed = mod.getClaimsManager().unclaimLast(uuid);
			player.sendMessage(new TextComponentString(String.format("%sWARNING: Your claim at (%d, %d) was unclaimed since you have lost too much power!", TextFormatting.RED, unclaimed.A * 16, unclaimed.B * 16)));
		}

		totalPower.replace(uuid, getTotalPower(uuid) - amount);
		
	}
	
	private class GivePowerTask implements Runnable {
		 
    	public void run() {
    		try {
    			for(EntityPlayerMP player: mod.getServer().getPlayerList().getPlayers()) {
    	    		addPower(player, 1);
    	    	}
    		} catch(Exception exception) {
    			exception.printStackTrace();
    		}
    	}
    	
    }
	
}
