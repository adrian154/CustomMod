package dev.codesoup.mc.commands;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class SendPowerCommand extends ModCommandBase {

	public static final String USAGE = "/sendpower <player> <amount>";

	public SendPowerCommand(CustomMod mod) {
		super(mod, "sendpower", "sp", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		EntityPlayerMP senderPlayer = assertIsPlayer(sender);
		_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE);
		GameProfile player = assertPlayer(params[0]);
		int amount = parseInt(params[1]);

		if(mod.getPowerManager().removeFreePower(senderPlayer, amount)) {
			mod.getPowerManager().addPower(player.getId(), amount);
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
