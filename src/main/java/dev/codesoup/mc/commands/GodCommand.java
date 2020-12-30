package dev.codesoup.mc.commands;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.GenericToggleManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class GodCommand extends ModCommandBase {

	private static final String USAGE = "/god";
	
	private GenericToggleManager isGodmode;
	
	public GodCommand(CustomMod mod) {
		super(mod, "god", 4);
		this.isGodmode = new GenericToggleManager();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
		
		EntityPlayerMP player = assertIsPlayer(sender);
		if(isGodmode.toggle(player)) {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "You are now in godmode."));
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "You are no longer in godmode."));
		}
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	public boolean isGodmode(EntityPlayer player) {
		return isGodmode.get(player);
	}
	
}
