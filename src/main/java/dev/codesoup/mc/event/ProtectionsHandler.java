package dev.codesoup.mc.event;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.RequiresMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.FarmlandTrampleEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProtectionsHandler extends RequiresMod {
	
	public ProtectionsHandler(CustomMod mod) {
		super(mod);
	}
	
	@SubscribeEvent
	public void interactEvent(PlayerInteractEvent event) {
		if(!event.getWorld().isRemote)
			event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), event.getEntityPlayer()));
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		
		if(!event.getWorld().isRemote)
			event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), event.getPlayer()));
		
	}
	
	@SubscribeEvent
	public void placeEvent(EntityPlaceEvent event) {
		if(event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), player));
		}
	}
	
	@SubscribeEvent
	public void trampleFarmlandEvent(FarmlandTrampleEvent event) {
		if(!event.getWorld().isRemote) {
			if(event.getEntity() instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
				event.setCanceled(mod.getClaimsManager().shouldProtect(event.getWorld(), event.getPos(), player));
			}
		}
	}
	
	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		if(!event.getEntity().getEntityWorld().isRemote && !(event.getEntity() instanceof EntityPlayer)) {
			if(mod.getClaimsManager().shouldProtect(event.getEntity().getEntityWorld(), event.getEntity().getPosition(), event.getEntityPlayer())) {
				event.setCanceled(true);
			}
		}
	}
	
}
