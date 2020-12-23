package dev.codesoup.mc.commands;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.Colors;
import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.Nation;
import dev.codesoup.mc.NationManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class NationCommand extends ModCommandBase {

	private NationManager nationManager;
	private static final String USAGE_CREATE = TextFormatting.RED + "/nation create <name>";
	private static final String USAGE_RENAME = TextFormatting.RED + "/nation rename <name>";
	private static final String USAGE_LEAVE = TextFormatting.RED + "/nation leave";
	private static final String USAGE_INVITE = "/nation invite <player>";
	private static final String USAGE_UNINVITE = TextFormatting.RED + "/nation uninvite <player>";
	private static final String USAGE_INVITE_LIST = TextFormatting.RED + "/nation invites";
	private static final String USAGE_MEMBERS = TextFormatting.RED + "/nation members [nation name]";
	private static final String USAGE_ACCEPT = TextFormatting.RED + "/nation accept <nation name>";
	private static final String USAGE_KICK = TextFormatting.RED + "/nation kick <player>";
	private static final String USAGE_MAKELEADER = TextFormatting.RED + "/nation makeleader <player>";
	private static final String USAGE = USAGE_CREATE + "\n" +
										USAGE_RENAME + "\n" +
										USAGE_LEAVE + "\n" +
										USAGE_INVITE + "\n" +
										USAGE_UNINVITE + "\n" +
										USAGE_INVITE_LIST + "\n" +
										USAGE_MEMBERS + "\n" + 
										USAGE_ACCEPT + "\n" +
										USAGE_KICK + "\n" + 
										USAGE_MAKELEADER;
	
	private final String ERR_CANNOT_BE_IN_NATION = "You must leave your current nation to create a new one.\nIf you would like to do so, do " + USAGE_LEAVE;
	private final String ERR_MUST_BE_IN_NATION = "You are not in a nation.";
	private final String ERR_NATION_NAME_NONUNIQUE = "A nation of that name exists already.";
	private final String ERR_BAD_NAME_LENGTH = "Your nation's length must be between 3 and 24 characters long.";
	private final String ERR_MUST_BE_LEADER = "You are not the leader of your nation.";
	private final String ERR_ALREADY_IN_NATION = "That player is already in the nation.";
	private final String ERR_NOT_IN_NATION = "That player is not in the nation.";
	private final String ERR_ALREADY_INVITED = "That player has already been invited to the nation.";
	private final String ERR_3P_NOT_INVITED = "That player is not invited to the nation.";
	private final String ERR_1P_NOT_INVITED = "You are not invited to that nation.";
	private final String ERR_NO_SUCH_NATION = "No nation exists by that name.";
	private final String ERR_INVALID_COLOR = "Invalid color.";
	private final String ERR_UNKNOWN_COMMAND = "No such command.";
	
	public NationCommand(CustomMod mod) {
		super(mod, "nation", 0);
		this.nationManager = mod.getNationManager();
	}
	
	private void assertNationName(String name) throws CommandException {
		_assert(nationManager.getNation(name) == null, ERR_NATION_NAME_NONUNIQUE);
		_assert(name.length() >= 3 && name.length() <= 24, ERR_BAD_NAME_LENGTH);
	}
	
	private Nation assertNation(String name) throws CommandException {
		Nation nation = nationManager.getNation(name);
		_assert(nation != null, ERR_NO_SUCH_NATION);
		return nation;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		_assert(params.length > 0, ERR_INCORRECT_USAGE + USAGE);
		Nation nation = this.nationManager.getNation(player);

		// Commands that do not require you to be in a nation...
		if(params[0].equals("create")) {
			
			_assert(nation == null, ERR_CANNOT_BE_IN_NATION);
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_CREATE);
			assertNationName(params[1]);
			
			Nation newAlliance = new Nation(mod.getNationManager());
			newAlliance.setName(params[1]);
			newAlliance.addMember(player);
			newAlliance.makeLeader(player);
			this.nationManager.addNation(newAlliance);

			player.sendMessage(new TextComponentString(
				TextFormatting.GREEN + "Your nation was created.\n" +
				TextFormatting.GRAY + "Add people with " + TextFormatting.WHITE + USAGE_INVITE
			));
			
			return;
			
		}
		
		if(params[0].equals("accept")) {
			
			_assert(nation == null, ERR_CANNOT_BE_IN_NATION);
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_ACCEPT);
			Nation theNation = assertNation(params[1]);
			_assert(theNation.hasInvitationFor(player), ERR_1P_NOT_INVITED);
			
			nationManager.addPlayer(theNation, player.getUniqueID());
			player.refreshDisplayName();
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Welcome to " + theNation.getFmtName() + TextFormatting.GREEN + "!"));
			mod.broadcast(String.format("%s%s joined %s%s.", player.getName(), TextFormatting.GRAY, theNation.getFmtName(), TextFormatting.GRAY));
			theNation.uninvite(player.getUniqueID());
			
			return;
			
		}
		
		if(params[0].equals("members")) {
			
			Nation theNation;
			
			if(params.length == 1) {
				_assert(nation != null, ERR_MUST_BE_IN_NATION);
				theNation = nation;
			} else {
				theNation = assertNation(params[1]);
			}
			
			String list = theNation.getMembers()
				.stream()
				.map(uuid -> mod.getProfile(uuid))
				.map(gameProfile ->
					String.format(
						"%s%s%s%s",
						theNation.isLeader(gameProfile.getId()) ? TextFormatting.YELLOW : TextFormatting.WHITE,
						gameProfile.getName(),
						theNation.isLeader(gameProfile.getId()) ? " (LEADER)" : "",
						TextFormatting.GRAY
					)
				)
				.collect(Collectors.joining(", "));

			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Members of " + theNation.getFmtName() + TextFormatting.GRAY + ": " + list));
			
			return;
			
		}
		
		// Commands that require you to be in a nation...
		_assert(nation != null, ERR_MUST_BE_IN_NATION);
		
		// Commands that do not require you to be leader...
		if(params[0].equals("leave")) {
			
			nationManager.removePlayer(nation, player.getUniqueID());
			player.refreshDisplayName();
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You left your nation."));
			nation.broadcast(TextFormatting.GRAY + player.getName() + " left the nation.");
			
			return;
			
		} 
		
		if(params[0].equals("invite")) {
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_INVITE);
			GameProfile toInvite = assertPlayer(params[1]);
			_assert(!nationManager.sameNation(player.getUniqueID(), toInvite.getId()), ERR_ALREADY_IN_NATION);
			_assert(!nation.hasInvitationFor(toInvite.getId()), ERR_ALREADY_INVITED);
			
			nation.invite(toInvite.getId());
			EntityPlayerMP toInvitePlayer = mod.getPlayer(toInvite.getId());
			if(toInvitePlayer != null) {
				toInvitePlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "You were invited to " + nation.getFmtName() + TextFormatting.GRAY + "."));
			}
			
			nation.broadcast(player.getName() + TextFormatting.GRAY + " invited " + TextFormatting.WHITE + toInvite.getName() + TextFormatting.GRAY + " to the nation.");
			
			return;
			
		}
		
		if(params[0].equals("uninvite")) {
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_UNINVITE);
			GameProfile toUninvite = assertPlayer(params[1]);
			_assert(nation.hasInvitationFor(toUninvite.getId()), ERR_3P_NOT_INVITED);
			
			nation.uninvite(toUninvite.getId());
			nation.broadcast(player.getName() + TextFormatting.GRAY + " uninvited " + TextFormatting.WHITE + toUninvite.getName() + TextFormatting.GRAY + " from the nation.");
			
			return;
			
		}
		
		if(params[0].equals("invites")) {
			
			List<UUID> invites = nation.getInvitations();
			if(invites.size() == 0) {
			
				player.sendMessage(new TextComponentString(TextFormatting.GRAY + "There are no outstanding invites."));
			
			} else {
				
				String list = nation.getInvitations().stream().map(uuid -> mod.getProfile(uuid)).map(gameProfile -> gameProfile.getName()).collect(Collectors.joining(", "));
				player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Outstanding invites: " + TextFormatting.WHITE + list));
			
			}
			
			return;
			
		}
		
		// Commands that require you to be leader...
		_assert(nation.isLeader(player), ERR_MUST_BE_LEADER);
		
		if(params[0].equals("rename")) {

			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_RENAME);
			assertNationName(params[1]);
			
			nationManager.setNationName(nation, params[1]);
			
			return;
			
		} 

		if(params[0].equals("kick")) {
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_KICK); 
			GameProfile toKick = assertPlayer(params[1]);
			_assert(nationManager.sameNation(player.getUniqueID(), toKick.getId()), ERR_NOT_IN_NATION);
			
			nationManager.removePlayer(nation, toKick.getId());
			nation.broadcast(toKick.getName() + TextFormatting.GRAY + " was removed from the nation.");

			EntityPlayerMP kicked = mod.getPlayer(toKick.getId());
			if(kicked != null) {
				kicked.refreshDisplayName();
				kicked.sendMessage(new TextComponentString(TextFormatting.RED + "You were kicked from your nation."));
			}
			
			return;
			
		}
		
		if(params[0].equals("makeleader")) { 
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_MAKELEADER);
			GameProfile toPromote = assertPlayer(params[1]);
			_assert(nation.getMembers().contains(toPromote.getId()), ERR_NOT_IN_NATION);
			
			nation.makeLeader(toPromote.getId());
			nation.broadcast(String.format("%s%s made %s%s%s the new leader of this nation.", player.getName(), TextFormatting.GRAY, TextFormatting.WHITE, toPromote.getName(), TextFormatting.GRAY));
			
			return;
			
		}
		
		if(params[0].equals("color")) {
			
			if(params.length == 1) {
				String colors = Colors.colors.keySet().stream().map(name -> String.format("%s%s", Colors.fromString(name), name)).collect(Collectors.joining(TextFormatting.GRAY + ", "));
				sender.sendMessage(new TextComponentString("Available colors: " + colors));
				return;
			}
			
			
			TextFormatting color = Colors.fromString(params[1]);
			_assert(color != null, ERR_INVALID_COLOR);
			
			nation.setColor(color);
			
			return;
			
		} 
		
		throw new CommandException(ERR_UNKNOWN_COMMAND);
		
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
}
