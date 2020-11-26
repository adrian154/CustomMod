package dev.codesoup.mc.mcws.messages;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class PlayerDeathMessage extends OnePlayerMessage {

	public String deathMessage;
	
	public PlayerDeathMessage(LivingDeathEvent event) {
		super("death", (EntityPlayerMP)event.getEntity());
		this.deathMessage = event.getSource().getDeathMessage(event.getEntityLiving()).getFormattedText();
	}
	
}
