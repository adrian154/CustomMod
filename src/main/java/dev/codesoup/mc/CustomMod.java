package dev.codesoup.mc;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import dev.codesoup.mc.commands.ClaimCommand;
import dev.codesoup.mc.commands.TogglePVPCommand;
import dev.codesoup.mc.event.CustomEventHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CustomMod.MODID, name = CustomMod.NAME, version = CustomMod.VERSION, acceptableRemoteVersions = "*")
public class CustomMod
{
    public static final String MODID = "custommod";
    public static final String NAME = "Custom Mod";
    public static final String VERSION = "1.0";

    private MinecraftServer server;
    
    private CustomEventHandler customEventHandler;
    private ClaimsManager claimsManager;
    private AllianceManager allianceManager;
    
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
    	
    	this.server = event.getServer();
    	
    	event.registerServerCommand(new TogglePVPCommand(this));
    	event.registerServerCommand(new ClaimCommand(this));
    	
    	try {
    		this.claimsManager = new ClaimsManager(this);
    		this.allianceManager = new AllianceManager();
    	} catch(IOException exception) {
    		this.logger.fatal("Exception while initializing: " + exception.getMessage());
    	}	
    	
    }
    
    public CustomEventHandler getEventHandler() {
    	return this.customEventHandler;
    }
    
    public ClaimsManager getClaims() {
    	return this.claimsManager;
    }
    
    public AllianceManager getAllianceManager() {
    	return this.allianceManager;
    }
    
    public void broadcast(String message) { 
    	this.server.getPlayerList().sendMessage(new TextComponentString(message));
    }
    
    public MinecraftServer getServer() {
    	return this.server;
    }
    
}
