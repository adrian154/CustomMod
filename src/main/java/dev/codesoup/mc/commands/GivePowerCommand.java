package dev.codesoup.mc.commands;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class GivePowerCommand extends ModCommandBase {

	public GivePowerCommand(CustomMod mod) {
		super(mod, "givepower", 4);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		_assert(params.length == 2, ERR_INCORRECT_USAGE + "/givepower <user> <amount>");

		GameProfile profile = assertPlayer(params[0]);
		int power = parseInt(params[1]);
		mod.getPowerManager().addPower(profile.getId(), power);
		sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Gave " + profile.getName() + " " + power + " power"));
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "/givepower <person> <amount>";
	}
	
}
