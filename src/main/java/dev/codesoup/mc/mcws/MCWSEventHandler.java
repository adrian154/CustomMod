package dev.codesoup.mc.mcws;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.mcws.messages.PlayerChatMessage;
import dev.codesoup.mc.mcws.messages.PlayerDeathMessage;
import dev.codesoup.mc.mcws.messages.PlayerJoinMessage;
import dev.codesoup.mc.mcws.messages.PlayerQuitMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class MCWSEventHandler {
	
	private CustomMod mod;
	
	public MCWSEventHandler(CustomMod mod) {
		this.mod = mod;
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void playerLoggedInEvent(PlayerLoggedInEvent event) {
		mod.getWSServer().broadcastMessage(new PlayerJoinMessage(event));
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void playerLoggedOutEvent(PlayerLoggedOutEvent event) {
		mod.getWSServer().broadcastMessage(new PlayerQuitMessage(event));	
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void chatEvent(ServerChatEvent event) {
		mod.getWSServer().broadcastMessage(new PlayerChatMessage(event));
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void livingDeathEvent(LivingDeathEvent event) {
		if(event.getEntity() instanceof EntityPlayerMP) {
			mod.getWSServer().broadcastMessage(new PlayerDeathMessage(event));
		}
	}
	
}
