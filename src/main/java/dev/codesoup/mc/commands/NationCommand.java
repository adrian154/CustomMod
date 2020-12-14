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
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class NationCommand extends ModCommandBase {

	private NationManager nationManager;
	private static final String USAGE_CREATE = TextFormatting.RED + "/alliance create <name>";
	private static final String USAGE_RENAME = TextFormatting.RED + "/alliance rename <name>";
	private static final String USAGE_LEAVE = TextFormatting.RED + "/alliance leave";
	private static final String USAGE_INVITE = TextFormatting.RED + "/alliance invite <player>";
	private static final String USAGE_UNINVITE = TextFormatting.RED + "/alliance uninvite <player>";
	private static final String USAGE_INVITE_LIST = TextFormatting.RED + "/alliance invites";
	private static final String USAGE_MEMBERS = TextFormatting.RED + "/alliance members [alliance name]";
	private static final String USAGE_ACCEPT = TextFormatting.RED + "/alliance accept <alliance name>";
	private static final String USAGE_KICK = TextFormatting.RED + "/alliance kick <player>";
	private static final String USAGE_MAKELEADER = TextFormatting.RED + "/alliance makeleader <player>";
	private static final String USAGE = USAGE_CREATE + "\n" +
										USAGE_RENAME + "\n" +
										USAGE_LEAVE + "\n" +
										USAGE_INVITE + "\n" +
										USAGE_UNINVITE + "\n" +
										USAGE_INVITE_LIST + "\n" +
										USAGE_MEMBERS + "\n" + 
										USAGE_ACCEPT + "\n" +
										USAGE_KICK;
	
	private final String ERR_INCORRECT_USAGE = "Incorrect number of parameters.\nUsage: ";
	private final String ERR_CANNOT_BE_IN_NATION = "You must leave your current nation to create a new one.\nIf you would like to do so, do " + USAGE_LEAVE;
	private final String ERR_MUST_BE_IN_NATION = "You are not in a nation.";
	private final String ERR_NATION_NAME_NONUNIQUE = "A nation of that name exists already.";
	private final String ERR_BAD_NAME_LENGTH = "Your nation's length must be between 3 and 24 characters long.";
	private final String ERR_MUST_BE_LEADER = "You are not the leader of your nation.";
	private final String ERR_ALREADY_IN_ALLIANCE = "That player is already in the nation.";
	private final String ERR_NOT_IN_ALLIANCE = "That player is not in the nation.";
	private final String ERR_ALREADY_INVITED = "That player has already been invited to the nation.";
	private final String ERR_3P_NOT_INVITED = "That player is not invited to the nation.";
	private final String ERR_1P_NOT_INVITED = "You are not invited to that alliance.";
	private final String ERR_NO_SUCH_NATION = "No nation exists by that name.";
	private final String ERR_INVALID_COLOR = "Invalid color.";
	private final String ERR_UNKNOWN_COMMAND = "No such command.";
	
	public NationCommand(CustomMod mod) {
		super(mod);
		this.nationManager = mod.getNationManager();
	}
	
	private void assertNationName(String name) throws CommandException {
		_assert(nationManager.getNation(name) == null, ERR_NATION_NAME_NONUNIQUE);
		_assert(name.length() > 3 && name.length() < 24, ERR_BAD_NAME_LENGTH);
	}
	
	private Nation assertNation(String name) throws CommandException {
		Nation nation = nationManager.getNation(name);
		_assert(nation != null, ERR_NO_SUCH_NATION);
		return nation;
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
		Nation nation = this.nationManager.getNation(player);

		// Commands that do not require you to be in a nation...
		if(params[0].equals("create")) {
			
			_assert(nation == null, ERR_CANNOT_BE_IN_NATION);
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_CREATE);
			assertNationName(params[1]);
			
			Nation newAlliance = new Nation();
			newAlliance.setName(params[1]);
			newAlliance.addMember(player);
			newAlliance.makeLeader(player);
			this.nationManager.addNation(newAlliance);

			player.sendMessage(new TextComponentString(
				TextFormatting.GREEN + "Your alliance was created.\n" +
				TextFormatting.GRAY + "Add people with " + TextFormatting.WHITE + "/alliance invite <player>"
			));
			
			return;
			
		}
		
		if(params[0].equals("members")) {
			
			Nation theNation;
			
			if(nation == null) {
				_assert(params.length == 2, ERR_MUST_BE_IN_NATION);
				theNation = assertNation(params[1]);
			} else {
				theNation = nation;
			}
			

			PlayerProfileCache cache = mod.getServer().getPlayerProfileCache();
			String list = theNation.getMembers()
				.stream()
				.map(uuid -> cache.getProfileByUUID(uuid))
				.map(gameProfile ->
					String.format(
						"%s%s%s%s",
						nation.isLeader(gameProfile.getId()) ? TextFormatting.YELLOW : TextFormatting.WHITE,
						gameProfile.getName(),
						nation.isLeader(gameProfile.getId()) ? " (LEADER)" : "",
						TextFormatting.GRAY
					)
				)
				.collect(Collectors.joining(","));

			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Members of " + theNation.getFmtName() + TextFormatting.GRAY + ": " + list));
			
			return;
			
		}
		
		// Commands that require you to be in a nation...
		_assert(nation == null, ERR_MUST_BE_IN_NATION);
		
		// Commands that do not require you to be leader...
		if(params[0].equals("leave")) {
			
			nationManager.removePlayer(nation, player.getUniqueID());
			player.refreshDisplayName();
			player.sendMessage(new TextComponentString(TextFormatting.GRAY + "You left your alliance."));
			this.nationManager.broadcastTo(nation, TextFormatting.GRAY + player.getName() + " left the alliance.");
			
			return;
			
		} 
		
		if(params[0].equals("invite")) {
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_INVITE);
			GameProfile toInvite = assertPlayer(params[1]);
			_assert(!nationManager.sameNation(player.getUniqueID(), toInvite.getId()), ERR_ALREADY_IN_ALLIANCE);
			_assert(!nation.hasInvitationFor(toInvite.getId()), ERR_ALREADY_INVITED);
			
			nation.invite(toInvite.getId());
			EntityPlayerMP toInvitePlayer = mod.getServer().getPlayerList().getPlayerByUUID(toInvite.getId());
			if(toInvitePlayer != null) {
				toInvitePlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "You were invited to " + nation.getFmtName() + TextFormatting.GRAY + "."));
			}
			
			nationManager.broadcastTo(nation, player.getName() + TextFormatting.GRAY + " invited " + TextFormatting.WHITE + toInvite.getName() + TextFormatting.GRAY + " to the alliance.");
			
			return;
			
		}
		
		if(params[0].equals("uninvite")) {
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_UNINVITE);
			GameProfile toUninvite = assertPlayer(params[1]);
			_assert(nation.hasInvitationFor(toUninvite.getId()), ERR_3P_NOT_INVITED);
			
			nation.uninvite(toUninvite.getId());
			nationManager.broadcastTo(nation, player.getName() + TextFormatting.GRAY + " uninvited " + TextFormatting.WHITE + toUninvite.getName() + TextFormatting.GRAY + " from the alliance.");
			
			return;
			
		}
		
		if(params[0].equals("invites")) {
			
			List<UUID> invites = nation.getInvitations();
			if(invites.size() == 0) {
			
				player.sendMessage(new TextComponentString(TextFormatting.GRAY + "There are no outstanding invites."));
			
			} else {
				
				PlayerProfileCache cache = mod.getServer().getPlayerProfileCache();
				String list = nation.getInvitations().stream().map(uuid -> cache.getProfileByUUID(uuid)).map(gameProfile -> gameProfile.getName()).collect(Collectors.joining(", "));
				player.sendMessage(new TextComponentString(TextFormatting.GRAY + "Outstanding invites: " + TextFormatting.WHITE + list));
			
			}
			
			return;
			
		}
		
		if(params[0].equals("accept")) {
			
			_assert(nation == null, ERR_CANNOT_BE_IN_NATION);
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_ACCEPT);
			Nation theNation = assertNation(params[1]);
			_assert(nation.hasInvitationFor(player), ERR_1P_NOT_INVITED);
			
			nationManager.addPlayer(nation, player.getUniqueID());
			player.refreshDisplayName();
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Welcome to " + theNation.getFmtName() + TextFormatting.GREEN + "!"));
			mod.broadcast(String.format("%s%s joined %s%s.", player.getName(), TextFormatting.GRAY, theNation.getFmtName(), TextFormatting.GRAY));
			theNation.uninvite(player.getUniqueID());
			
			return;
			
		}
		
		// Commands that require you to be leader...
		_assert(nation.isLeader(player), ERR_MUST_BE_LEADER);
		
		if(params[0].equals("rename")) {

			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_RENAME);
			assertNationName(params[1]);
			
			if(params[1].length() < 3 || params[1].length() > 24) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Your alliance's name must be between 3 and 16 characters long!"));
				return;
			}
			
			nationManager.setNationName(nation, params[1]);
			
			return;
			
		} 

		if(params[0].equals("kick")) {
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_KICK); 
			GameProfile toKick = assertPlayer(params[1]);
			_assert(nationManager.sameNation(player.getUniqueID(), toKick.getId()), ERR_NOT_IN_ALLIANCE);
			
			nationManager.removePlayer(nation, toKick.getId());
			nationManager.broadcastTo(nation, toKick.getName() + TextFormatting.GRAY + " was removed from the alliance.");

			EntityPlayerMP kicked = mod.getServer().getPlayerList().getPlayerByUUID(toKick.getId());
			if(kicked != null) {
				kicked.refreshDisplayName();
			}
			
			return;
			
		}
		
		if(params[0].equals("makeleader")) { 
			
			_assert(params.length == 2, ERR_INCORRECT_USAGE + USAGE_MAKELEADER);
			GameProfile toPromote = assertPlayer(params[1]);
			_assert(nation.getMembers().contains(toPromote), ERR_NOT_IN_ALLIANCE);
			
			nation.makeLeader(toPromote.getId());
			nationManager.broadcastTo(nation, String.format("%s%s made %s%s%s the new leader of this alliance.", player.getName(), TextFormatting.GRAY, TextFormatting.WHITE, toPromote.getName(), TextFormatting.GRAY));
			
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
			this.nationManager.refreshNames(nation);
			
			return;
			
		} 
		
		throw new CommandException(ERR_UNKNOWN_COMMAND);
		
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

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
}
