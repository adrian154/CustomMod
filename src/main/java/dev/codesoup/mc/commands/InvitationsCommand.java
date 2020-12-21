package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class InvitationsCommand extends ModCommandBase {

	private final static String USAGE = "/invites";
	
	public InvitationsCommand(CustomMod mod) {
		super(mod, "invites", 0);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		mod.getNationManager().listInvitations((EntityPlayerMP)sender, true);
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
