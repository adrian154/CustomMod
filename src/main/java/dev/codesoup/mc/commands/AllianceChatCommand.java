package dev.codesoup.mc.commands;

import java.util.Arrays;

import dev.codesoup.mc.Alliance;
import dev.codesoup.mc.AllianceManager;
import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AllianceChatCommand extends CommandBase {

	private CustomMod mod;
	private AllianceManager allianceManager;
	private final static String USAGE = "/ac";
	
	public AllianceChatCommand(CustomMod mod) {
		this.mod = mod;
		this.allianceManager = mod.getAllianceManager();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
		Alliance alliance = this.allianceManager.getAlliance(player);
		
		if(alliance == null) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You aren't in an alliance."));
			return;
		}
		
		if(params.length < 1) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You didn't specify a message."));
			return;
		}
		
		String message = String.join(" ", (String [])Arrays.copyOfRange(params, 0, params.length));
		
		this.allianceManager.broadcastTo(
			alliance,
			String.format(
				"%s(alliance)%s %s: %s",
				TextFormatting.DARK_GREEN,
				TextFormatting.RESET,
				player.getName(),
				message
			)
		);
		
	}
	
	@Override
	public String getName() {
		return "ac";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
	
}
