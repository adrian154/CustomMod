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
		return totalPower.get(uuid);
	}
	
	public int getTotalPower(EntityPlayer player) {
		return getTotalPower(player.getUniqueID());
	}
	
	public void addPower(UUID uuid, int power) {
		totalPower.replace(uuid, totalPower.get(uuid) + power);
	}
	
	public void removePower(EntityPlayerMP player) {
		
		UUID uuid = player.getUniqueID();
		if(mod.getClaims().getClaims(uuid).size() > getTotalPower(uuid) - 1) {
			Pair unclaimed = mod.getClaims().unclaimLast(uuid);
			player.sendMessage(new TextComponentString(String.format("%sWARNING: Your claim at (%d, %d) was unclaimed since you have lost too much power!", TextFormatting.RED, unclaimed.A * 16, unclaimed.B * 16)));
		}
		
		totalPower.replace(uuid, getTotalPower(uuid) - 1);
		
	}
	
}
