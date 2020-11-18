package dev.codesoup.mc;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import dev.codesoup.mc.commands.TogglePVPCommand;
import dev.codesoup.mc.event.CustomEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CustomMod.MODID, name = CustomMod.NAME, version = CustomMod.VERSION)
public class CustomMod
{
    public static final String MODID = "custommod";
    public static final String NAME = "Custom Mod";
    public static final String VERSION = "1.0";

    private CustomEventHandler customEventHandler;
    private ClaimsManager claimsManager;
    public Logger logger;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	this.logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
   
    	this.customEventHandler = new CustomEventHandler(this);
    	MinecraftForge.EVENT_BUS.register(this.customEventHandler);
    	
    }
    
    @EventHandler
    public void init(FMLServerStartingEvent event) {
    	event.registerServerCommand(new TogglePVPCommand(this));
    	
    	try {
    		this.claimsManager = new ClaimsManager(this);
    	} catch(IOException exception) {
    		this.logger.fatal("Exception while initializing claims: " + exception.getMessage());
    	}
    
    }
    
    public CustomEventHandler getEventHandler() {
    	return this.customEventHandler;
    }
    
    public ClaimsManager getClaims() {
    	return this.claimsManager;
    }
    
}
