package dev.codesoup.mc.commands;

import java.util.Arrays;
import java.util.List;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.RequiresMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class ModCommands extends RequiresMod {
	
	public final AutoclaimCommand AUTOCLAIM_COMMAND;
	public final BaseCommand BASE_COMMAND;
	public final ClaimCommand CLAIM_COMMAND;
	public final GivePowerCommand GIVE_POWER_COMMAND;
	public final InvitationsCommand INVITATIONS_COMMAND;
	public final MCWSCommand MCWS_COMMAND;
	public final NationChatCommand NATION_CHAT_COMMAND;
	public final NationCommand NATION_COMMAND;
	public final PowerCommand POWER_COMMAND;
	public final ProtectCommand PROTECT_COMMAND;
	public final RerenderMarkersCommand RERENDER_MARKERS_COMMAND;
	public final SendPowerCommand SEND_POWER_COMMAND;
	public final SetSpawnCommand SET_SPAWN_COMMAND;
	public final TogglePVPCommand TOGGLE_PVP_COMMAND;
	public final TopCommand TOP_COMMAND;
	public final UnclaimCommand UNCLAIM_COMMAND;
	public final GodCommand GOD_COMMAND;
	private final List<ModCommandBase> commands;
	
	public ModCommands(CustomMod mod) {
		
		super(mod);
		
		this.AUTOCLAIM_COMMAND = new AutoclaimCommand(mod);
		this.BASE_COMMAND = new BaseCommand(mod);
		this.CLAIM_COMMAND = new ClaimCommand(mod);
		this.GIVE_POWER_COMMAND = new GivePowerCommand(mod);
		this.INVITATIONS_COMMAND = new InvitationsCommand(mod);
		this.MCWS_COMMAND = new MCWSCommand(mod);
		this.NATION_CHAT_COMMAND = new NationChatCommand(mod);
		this.NATION_COMMAND = new NationCommand(mod);
		this.POWER_COMMAND = new PowerCommand(mod);
		this.PROTECT_COMMAND = new ProtectCommand(mod);
		this.RERENDER_MARKERS_COMMAND = new RerenderMarkersCommand(mod);
		this.SEND_POWER_COMMAND = new SendPowerCommand(mod);
		this.SET_SPAWN_COMMAND = new SetSpawnCommand(mod);
		this.TOGGLE_PVP_COMMAND = new TogglePVPCommand(mod);
		this.TOP_COMMAND = new TopCommand(mod);
		this.UNCLAIM_COMMAND = new UnclaimCommand(mod);
		this.GOD_COMMAND = new GodCommand(mod);
		
		commands = Arrays.asList(
			this.AUTOCLAIM_COMMAND,
			this.BASE_COMMAND,
			this.CLAIM_COMMAND,
			this.GIVE_POWER_COMMAND,
			this.INVITATIONS_COMMAND,
			this.MCWS_COMMAND,
			this.NATION_CHAT_COMMAND,
			this.NATION_COMMAND,
			this.POWER_COMMAND,
			this.PROTECT_COMMAND,
			this.RERENDER_MARKERS_COMMAND,
			this.SEND_POWER_COMMAND,
			this.SET_SPAWN_COMMAND,
			this.TOGGLE_PVP_COMMAND,
			this.TOP_COMMAND,
			this.UNCLAIM_COMMAND,
			this.GOD_COMMAND
		);
		
	}
	
	public void onInit(FMLInitializationEvent event) {
		
		// register event handlers
		for(ModCommandBase command: commands) {
			MinecraftForge.EVENT_BUS.register(command);
		}
		
	}
	
	public void onServerStart(FMLServerStartingEvent event) {
		
		// register commands
		for(ModCommandBase command: commands) {
			event.registerServerCommand(command);
		}
		
	}
	
}
