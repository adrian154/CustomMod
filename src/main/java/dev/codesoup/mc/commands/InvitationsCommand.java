package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class InvitationsCommand extends CommandBase {

	private CustomMod mod;
	private final static String USAGE = "/invites";
	
	public InvitationsCommand(CustomMod mod) {
		this.mod = mod;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		mod.getAllianceManager().listInvitations((EntityPlayerMP)sender, true);
		
	}
	
	@Override
	public String getName() {
		return "invites";
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
