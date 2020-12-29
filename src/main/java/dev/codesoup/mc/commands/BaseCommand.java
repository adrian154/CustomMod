package dev.codesoup.mc.commands;

import java.util.Map;
import java.util.WeakHashMap;

import dev.codesoup.mc.CustomMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class BaseCommand extends ModCommandBase {

	private final static String USAGE = "/base";
	
	private Map<EntityPlayer, Long> invokeBaseTime;
	
	public BaseCommand(CustomMod mod) {
		super(mod, "base", "b", 0);
		this.invokeBaseTime = new WeakHashMap<>();
	}
	
	public void startBaseTPTimer(EntityPlayer player) {
		invokeBaseTime.put(player, player.getEntityWorld().getTotalWorldTime() + 20 * 10);
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
	
		EntityPlayerMP player = assertIsPlayer(sender);
		_assert(player.getBedLocation() != null, "You have not set your spawn yet.");
		startBaseTPTimer(player);
		player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Teleporting you to your base in 10 seconds. Don't move, or it will be canceled."));
	
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		
		if(invokeBaseTime.containsKey(event.player) && (event.player.lastTickPosX != event.player.posX || event.player.lastTickPosY != event.player.posY || event.player.lastTickPosZ != event.player.posZ)) {
			invokeBaseTime.remove(event.player);
			event.player.sendMessage(new TextComponentString(TextFormatting.RED + "Your teleport was canceled since you moved."));
		}

	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		
		if(event.phase != TickEvent.Phase.END) return;
		
		for(Map.Entry<EntityPlayer, Long> entry: invokeBaseTime.entrySet()) {
			
			if(event.world.getTotalWorldTime() == entry.getValue()) {
				
				invokeBaseTime.remove(entry.getKey());
				
				EntityPlayer player = entry.getKey();
				BlockPos pos = player.getBedLocation();
				if(pos != null) {
					mod.getServer().getCommandManager().executeCommand(mod.getServer(), String.format("/tp %s %d %d %d", player.getName(), pos.getX(), pos.getY(), pos.getZ()));
				}
				
			}
			
		}
		
	}
	
	// Cancel teleports when the player logs off
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
		
		if(invokeBaseTime.get(event.player) != null) {
			invokeBaseTime.remove(event.player);
		}
		
	}
	
}
