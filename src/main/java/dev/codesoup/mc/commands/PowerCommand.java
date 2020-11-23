package dev.codesoup.mc.commands;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class PowerCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/power";
	
	public PowerCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		UUID uuid;
		String name;
		if(params.length == 0) {
			if(sender instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP)sender;
				uuid = player.getUniqueID();
				name = player.getName();
			} else {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Get outta here, robot!"));
				return;
			}
		} else {
			GameProfile target = mod.getServer().getPlayerProfileCache().getGameProfileForUsername(params[1]);
			uuid = target.getId();
			name = target.getName();
		}
		
		sender.sendMessage(new TextComponentString(String.format("%s%s%s has %s%d%s total power and %s%d%s available power.", TextFormatting.WHITE, name, TextFormatting.GRAY, TextFormatting.WHITE, mod.getPowerManager().getTotalPower(uuid), TextFormatting.GRAY, TextFormatting.WHITE, mod.getPowerManager().getFreePower(uuid), TextFormatting.WHITE)));
		
	}
	
	@Override
	public String getName() {
		return "power";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(2, "");
	}
	
}
