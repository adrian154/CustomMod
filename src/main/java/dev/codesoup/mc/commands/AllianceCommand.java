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
	private static final String USAGE_UNINVITE = TextFormatting.RED + "/alliance uninvite <player>";
	private static final String USAGE_INVITE_LIST = TextFormatting.RED + "/alliance invites";
	private static final String USAGE_MEMBERS = TextFormatting.RED + "/alliance members [alliance name]";
	private static final String USAGE_ACCEPT = TextFormatting.RED + "/alliance accept <alliance name>";
	private static final String USAGE_KICK = TextFormatting.RED + "/alliance kick <player>";
	private static final String USAGE = USAGE_CREATE + "\n" +
										USAGE_RENAME + "\n" +
										USAGE_LEAVE + "\n" +
										USAGE_INVITE + "\n" +
										USAGE_UNINVITE + "\n" +
										USAGE_INVITE_LIST + "\n" +
										USAGE_MEMBERS + "\n" + 
										USAGE_ACCEPT + "\n" +
										USAGE_KICK;
	
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
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Your alliance was created.\n" + TextFormatting.GRAY + "Add people with " + TextFormatting.WHITE + "/alliance invite <player>"));
			
			
		} else if(params[0].equals("rename")) {
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
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
			
			if(params.length != 2) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You need to specify who you want to invite to the alliance.\nUsage: " + USAGE_INVITE));
				return;
			}
			
			GameProfile toInvite = mod.getServer().getPlayerProfileCache().getGameProfileForUsername(params[1]);
			if(toInvite.getId() == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "The player you're trying to invite does not exist."));
				return;
			}
			
			if(alliance.getMembers().contains(toInvite.getId())) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "The player you're trying to invite is already part of this alliance!"));
				return;
			}
			
			alliance.invite(toInvite.getId());
			EntityPlayerMP toInvitePlayer = mod.getServer().getPlayerList().getPlayerByUUID(toInvite.getId());
			if(toInvitePlayer != null) {
				toInvitePlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "You were invited to " + TextFormatting.WHITE + alliance.getName() + TextFormatting.GRAY + "."));
			}
				
			this.allianceManager.broadcastTo(alliance, player.getName() + TextFormatting.GRAY + " invited " + TextFormatting.WHITE + toInvite.getName() + TextFormatting.GRAY + " to the alliance.");
			
		} else if(params[0].equals("uninvite")) {
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
				return;
			}
			
			if(params.length != 2) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You need to specify who you want to uninvite to the alliance.\nUsage: " + USAGE_UNINVITE));
				return;
			}
			
			GameProfile toUninvite = mod.getServer().getPlayerProfileCache().getGameProfileForUsername(params[1]);
			if(toUninvite.getId() == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "The player you're trying to invite does not exist."));
				return;
			}
			
			if(!alliance.getInvitations().contains(toUninvite.getId())) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "There are no outstanding invitations for that player. Try /alliance invites to see a list."));
				return;
			}
			
			alliance.uninvite(toUninvite.getId());
			this.allianceManager.broadcastTo(alliance, player.getName() + TextFormatting.GRAY + " uninvited " + TextFormatting.WHITE + toUninvite.getName() + TextFormatting.GRAY + " from the alliance.");
			
			
		} else if(params[0].equals("invites")) {
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
				return;
			}
			
			if(alliance.getInvitations().size() == 0) {
				player.sendMessage(new TextComponentString(TextFormatting.GRAY + "There are no outstanding invites."));
				return;
			}
			
			PlayerProfileCache cache = mod.getServer().getPlayerProfileCache();
			String list = alliance.getInvitations().stream().map(uuid -> cache.getProfileByUUID(uuid)).map(gameProfile -> gameProfile.getName()).collect(Collectors.joining(", "));
			
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Outstanding invites: " + TextFormatting.WHITE + list));
			
		} else if(params[0].equals("members")) {
			
			Alliance alliance;
			
			if(params.length == 1) {
				alliance = this.allianceManager.getAlliance(player);
				if(alliance == null) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "You are not currently in an alliance."));
					return;
				}
			} else {
				alliance = this.allianceManager.getAlliance(params[1]);
				if(alliance == null) {
					player.sendMessage(new TextComponentString(TextFormatting.RED + "There's no alliance with that name."));
					return;
				}
			}
			
			
			PlayerProfileCache cache = mod.getServer().getPlayerProfileCache();
			String list = alliance.getMembers().stream().map(uuid -> cache.getProfileByUUID(uuid)).map(gameProfile -> gameProfile.getName()).collect(Collectors.joining(", "));
			
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Members of " + alliance.getName() + ": " + list));
			
		} else if(params[0].equals("accept")) {
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance != null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You must leave your current alliance to join a new one."));
				return;
			}
			
			if(params.length != 2) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Not enough parameters.\nUsage: " + USAGE_ACCEPT));
				return;
			}
			
			alliance = this.allianceManager.getAlliance(params[1]);
			
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "There's no alliance with that name."));
				return;
			}
			
			if(alliance.hasInvitationFor(player)) {
				this.allianceManager.addPlayer(alliance, player.getUniqueID());
				player.refreshDisplayName();
				player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Welcome to " + alliance.getName() + "!"));
				this.allianceManager.broadcastTo(alliance, player.getName() + TextFormatting.GRAY + " joined the alliance.");
				alliance.uninvite(player.getUniqueID());
				
			} else {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "That alliance has not invited you."));
			}
			
		} else if(params[0].equals("kick")) {
			
			if(params.length != 2) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Not enough parameters.\nUsage: " + USAGE_KICK));
			}
			
			Alliance alliance = this.allianceManager.getAlliance(player);
			if(alliance == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You're not in an alliance."));
				return;
			}
			
			if(!alliance.isLeader(player)) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "You must be the leader of your alliance to remove members."));
				return;
			}
			
			GameProfile toKick = mod.getServer().getPlayerProfileCache().getGameProfileForUsername(params[1]);
			if(toKick == null) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "The player you are trying to remove does not exist."));
				return;
			}
			
			if(!alliance.getMembers().contains(toKick.getId())) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "The player you are trying to remove is not part of this alliance."));
				return;
			}
			
			this.allianceManager.removePlayer(alliance, toKick.getId());
			this.allianceManager.broadcastTo(alliance, toKick.getName() + TextFormatting.GRAY + " was removed from the alliance.");
			
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
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
}
