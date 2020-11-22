package dev.codesoup.mc.commands;

import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.Alliance;
import dev.codesoup.mc.AllianceManager;
import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class AllianceCommand extends CommandBase {

	private CustomMod mod;
	private AllianceManager allianceManager;
	private static final String USAGE_CREATE = TextFormatting.RED + "/alliance create <name>";
	private static final String USAGE_RENAME = TextFormatting.RED + "/alliance rename <name>";
	private static final String USAGE_LEAVE = TextFormatting.RED + "/alliance leave";
	private static final String USAGE_INVITE = TextFormatting.RED + "/alliance invite <player>";
	private static final String USAGE = USAGE_CREATE + "\n" + USAGE_RENAME + "\n" + USAGE_LEAVE + "\n" + USAGE_INVITE;
	
	public AllianceCommand(CustomMod mod) {
		this.mod = mod;
		this.allianceManager = mod.getAllianceManager();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		if(!(sender instanceof EntityPlayerMP)) {
			return;
		}
		
		if(params.length == 0) {
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Not enough parameters.\n" + USAGE));
			return;
		}
		
		EntityPlayerMP player = (EntityPlayerMP)sender;
		if(params[0].equals("create")) {
			
			if(this.allianceManager.getAlliance(player) != null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You must leave your current alliance to create a new one.\nIf you would like to do so, do /alliance leave"));
				return;
			}
			
			if(params.length != 2) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You didn't specify the right number of parameters. Make sure that your alliance name has no spaces!\nUsage: " + USAGE_CREATE));
				return;
			}
			
			if(this.allianceManager.getAlliance(params[1]) != null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "An alliance of that name exists already!"));
				return;
			}
			
			if(params[1].length() < 3 || params[1].length() > 16) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Your alliance's name must be between 3 and 16 characters long!"));
				return;
			}
			
			Alliance newAlliance = new Alliance();
			newAlliance.setName(params[1]);
			newAlliance.addMember(player);
			newAlliance.makeLeader(player);
			this.allianceManager.addAlliance(newAlliance);
			this.allianceManager.refreshNames(newAlliance);
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Your alliance was created.\nAdd people with /alliance add <player>"));
			
			
		} else if(params[0].equals("rename")) {
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You must be part of an alliance to rename it.\nTo create an alliance, do /alliance create <name>"));
				return;
			}
			
			if(!alliance.isLeader(player)) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You must be the leader of your alliance to rename it."));
				return;
			}
			
			if(params.length != 2) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You didn't specify the right number of parameters. Make sure that your alliance name has no spaces!\nUsage: " + USAGE_RENAME));
				return;
			}
			
			if(this.allianceManager.getAlliance(params[1]) != null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "An alliance of that name exists already!"));
				return;
			}
			
			if(params[1].length() < 3 || params[1].length() > 24) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Your alliance's name must be between 3 and 16 characters long!"));
				return;
			}
			
			alliance.setName(params[1]);
			this.allianceManager.refreshNames(alliance);
			this.allianceManager.broadcastTo(alliance, TextFormatting.GRAY + "Your alliance was renamed to " + TextFormatting.YELLOW + params[1] + TextFormatting.GRAY + ".");
			
		} else if(params[0].equals("leave")) {
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
				return;
			}
			
			allianceManager.removePlayer(alliance, player.getUniqueID());
			player.refreshDisplayName();
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You left your alliance."));
			this.allianceManager.broadcastTo(alliance, TextFormatting.GRAY + player.getName() + " left the alliance.");
			
		} else if(params[0].equals("invite")) {
		
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
				return;
			}
			
			if(params.length != 1) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You need to specify who you want to invite to the alliance.\nUsage: " + USAGE_INVITE));
				return;
			}
			
			GameProfile toInvite = mod.getServer().getPlayerProfileCache().getGameProfileForUsername(params[1]);
			if(toInvite == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "The player you're trying to invite seems to have never joined the server before."));
				return;
			}
			
			alliance.invite(toInvite.getId());
			this.allianceManager.broadcastTo(alliance, player.getName() + TextFormatting.GRAY + " invited " + TextFormatting.WHITE + player.getName() + TextFormatting.GRAY + " to the alliance.");
			
		} else if(params[0].equals("members")) {
			
			Alliance alliance;
			
			if(params.length == 0) {
				alliance = this.allianceManager.getAlliance(player);
				if(alliance == null) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
					return;
				}
			} else {
				alliance = this.allianceManager.getAlliance(params[1]);
				if(alliance == null) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "No alliance exists by that name."));
					return;
				}
			}
			
			
			PlayerProfileCache cache = mod.getServer().getPlayerProfileCache();
			String list = alliance.getMembers().stream().map(uuid -> cache.getProfileByUUID(uuid)).map(gameProfile -> gameProfile.getName()).collect(Collectors.joining(", "));
			
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Members of " + alliance.getName() + ": " + list));
			
		} else {
			player.sendMessage(new TextComponentString(TextFormatting.RED + "Unknown command.\nUsage: " + USAGE));
		}
		
	}
	
	@Override
	public String getName() {
		return "alliance";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
