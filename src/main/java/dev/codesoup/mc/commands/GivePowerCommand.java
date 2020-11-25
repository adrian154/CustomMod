package dev.codesoup.mc.commands;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class GivePowerCommand extends CommandBase {

	private CustomMod mod;

	public GivePowerCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(params.length != 2) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /givepower <user> <amount>"));
			return;
		}
		
		GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(params[0]);
		int power;
		
		try {
			power = Integer.parseInt(params[1]);
		} catch(NumberFormatException exception) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid amount of power."));
			return;
		}
		
		if(profile == null) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "The player you are trying to send power to does not exist."));
			return;
		}
		
		mod.getPowerManager().addPower(profile.getId(), power);
		sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Gave " + profile.getName() + " " + power + " power"));
		
	}
	
	@Override
	public String getName() {
		return "givepower";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "/givepower <person> <amount>";
	}
	
}
