package dev.codesoup.mc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;

import dev.codesoup.mc.commands.AllianceCommand;
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
    private PowerManager powerManager;
    
    public Logger logger;
    public Gson gson;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	this.logger = event.getModLog();
    	this.gson = new Gson();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
   
    	this.customEventHandler = new CustomEventHandler(this);
    	MinecraftForge.EVENT_BUS.register(this.customEventHandler);
    	
    }
    
    @EventHandler
    public void init(FMLServerStartingEvent event) {
    	
    	this.server = event.getServer();
    	
    	try {
    		this.claimsManager = new ClaimsManager(this);
    		this.allianceManager = new AllianceManager(this);
    		this.powerManager = new PowerManager(this);
    	} catch(IOException exception) {
    		this.logger.fatal("Exception while initializing: " + exception.getMessage());
    	}	
    	
    	registerCommands(event);
    	
    	
    }
    
    private void registerCommands(FMLServerStartingEvent event) {
    	event.registerServerCommand(new TogglePVPCommand(this));
    	event.registerServerCommand(new ClaimCommand(this));
    	event.registerServerCommand(new AllianceCommand(this));
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
    
    public PowerManager getPowerManager() {
    	return this.powerManager;
    }
    
    public void broadcast(String message) { 
    	this.server.getPlayerList().sendMessage(new TextComponentString(message));
    }
    
    public MinecraftServer getServer() {
    	return this.server;
    }
 
    public void saveAll() {
    	logger.debug(this.gson.toJson(this.claimsManager));
    	logger.debug(this.gson.toJson(this.allianceManager));
    	logger.debug(this.gson.toJson(this.powerManager));
    }
    
    private class ManagerCreator<T extends Manager> implements InstanceCreator<T> {
    
    	private CustomMod mod;
    	private Class<T> clazz;
    	
    	public ManagerCreator(CustomMod mod, Class<T> clazz) {
    		this.mod = mod;
    		this.clazz = clazz;
    	}
    	
    	public T createInstance(Type type) {
    		try {
    			return clazz.getDeclaredConstructor(CustomMod.class).newInstance(mod);
    		} catch(NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exception) {
    			return null;
    		}
    	}
    	
    }
    
}
