package dev.codesoup.mc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.commands.BaseCommand;
import dev.codesoup.mc.commands.ClaimCommand;
import dev.codesoup.mc.commands.GivePowerCommand;
import dev.codesoup.mc.commands.InvitationsCommand;
import dev.codesoup.mc.commands.MCWSCommand;
import dev.codesoup.mc.commands.NationChatCommand;
import dev.codesoup.mc.commands.NationCommand;
import dev.codesoup.mc.commands.PowerCommand;
import dev.codesoup.mc.commands.ProtectCommand;
import dev.codesoup.mc.commands.RerenderMarkersCommand;
import dev.codesoup.mc.commands.SetSpawnCommand;
import dev.codesoup.mc.commands.TogglePVPCommand;
import dev.codesoup.mc.commands.TopCommand;
import dev.codesoup.mc.commands.UnclaimCommand;
import dev.codesoup.mc.commands.ViewInventoryCommand;
import dev.codesoup.mc.event.CustomEventHandler;
import dev.codesoup.mc.mcws.Configuration;
import dev.codesoup.mc.mcws.CustomAppender;
import dev.codesoup.mc.mcws.MCWSEventHandler;
import dev.codesoup.mc.mcws.WSServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = CustomMod.MODID, name = CustomMod.NAME, version = CustomMod.VERSION, acceptableRemoteVersions = "*", dependencies="after:dynmap")
public class CustomMod
{
    public static final String MODID = "custommod";
    public static final String NAME = "Custom Mod";
    public static final String VERSION = "1.0";

    private MinecraftServer server;
    
    private CustomEventHandler customEventHandler;
    private MCWSEventHandler mcwsEventHandler;
    
    private ClaimsManager claimsManager;
    private NationManager nationManager;
    private PowerManager powerManager;
    private MapManager mapManager;
    
    private Configuration configuration;
    private WSServer wsServer;
    
    public Logger logger;
    public Gson gson;
    
    private ScheduledExecutorService executor;

    private Gson buildGson() {
    	
    	GsonBuilder gsonBuilder = new GsonBuilder();
    	gsonBuilder.enableComplexMapKeySerialization();
    	
    	// Manager deserializers
    	gsonBuilder.registerTypeAdapter(NationManager.class, new ManagerCreator<NationManager>(this, NationManager.class));
    	gsonBuilder.registerTypeAdapter(ClaimsManager.class, new ManagerCreator<ClaimsManager>(this, ClaimsManager.class));
    	gsonBuilder.registerTypeAdapter(PowerManager.class, new ManagerCreator<PowerManager>(this, PowerManager.class));
    	
    	gsonBuilder.registerTypeAdapter(Nation.class, new InstanceCreator<Nation>() {
    		@Override
    		public Nation createInstance(Type type) {
    			return new Nation(nationManager, false);
    		}
    	});
    	
        return gsonBuilder.create();
    	
    }
    
    private void attachAppender() {
    	
    	org.apache.logging.log4j.core.Logger srvLogger = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();
    	srvLogger.addAppender(new CustomAppender(this));
    	
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    
    	this.logger = event.getModLog();
    	this.gson = buildGson();
        
    }
    
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
   
    	this.customEventHandler = new CustomEventHandler(this);
    	this.mcwsEventHandler = new MCWSEventHandler(this);
    	MinecraftForge.EVENT_BUS.register(this.customEventHandler);
    	MinecraftForge.EVENT_BUS.register(this.mcwsEventHandler);
    	
    }
    
    @EventHandler
    public void init(FMLServerStartingEvent event) {
    	
    	try {
    		this.loadAll();
    	} catch(IOException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException exception) {
    		this.logger.fatal("Failed to load something from configuration.");
    		exception.printStackTrace();
    	}	
    	
    	this.server = event.getServer();
    	this.wsServer = new WSServer(this);
    	this.mapManager = new MapManager(this);
    	
    	registerCommands(event);
    	startPassivePowerTask();
    	attachAppender();
    	
    }
    
    @EventHandler
    public void stopping(FMLServerStoppingEvent event) {
    	
    	try {
    		this.wsServer.stop();
    	} catch(IOException | InterruptedException exception) {
    		this.logger.error("Failed to close websocket.");
    		exception.printStackTrace();
    	}
    	
    }
    
    private void startPassivePowerTask() {
    	Runnable timerTask = new GivePowerTask();
    	executor = Executors.newScheduledThreadPool(1);
    	executor.scheduleAtFixedRate(timerTask, 0, 60 * 5, TimeUnit.SECONDS);
    }
    
    private void registerCommands(FMLServerStartingEvent event) {
    	event.registerServerCommand(new TogglePVPCommand(this));
    	event.registerServerCommand(new ClaimCommand(this));
    	event.registerServerCommand(new NationCommand(this));
    	event.registerServerCommand(new InvitationsCommand(this));
    	event.registerServerCommand(new PowerCommand(this));
    	event.registerServerCommand(new UnclaimCommand(this));
    	event.registerServerCommand(new SetSpawnCommand(this));
    	event.registerServerCommand(new BaseCommand(this));
    	event.registerServerCommand(new ProtectCommand(this));
    	event.registerServerCommand(new GivePowerCommand(this));
    	event.registerServerCommand(new NationChatCommand(this));
    	event.registerServerCommand(new RerenderMarkersCommand(this));
    	event.registerServerCommand(new ViewInventoryCommand(this));
    	event.registerServerCommand(new TopCommand(this));
    	event.registerServerCommand(new MCWSCommand(this));
    }
    
    public CustomEventHandler getEventHandler() {
    	return this.customEventHandler;
    }
    
    public ClaimsManager getClaimsManager() {
    	return this.claimsManager;
    }
    
    public NationManager getNationManager() {
    	return this.nationManager;
    }
    
    public PowerManager getPowerManager() {
    	return this.powerManager;
    }
    
    public Configuration getConfiguration() {
    	return this.configuration;
    }
    
    public MapManager getMapManager() {
    	return this.mapManager;
    }
    
    public void broadcast(String message) { 
    	this.server.getPlayerList().sendMessage(new TextComponentString(message));
    }
    
    public MinecraftServer getServer() {
    	return this.server;
    }
    
    public WSServer getWSServer() {
    	return this.wsServer;
    }
    
    public Scoreboard getScoreboard() {
    	return this.server.getWorld(0).getScoreboard();
    }
    
    public EntityPlayerMP getPlayer(String name) {
    	return server.getPlayerList().getPlayerByUsername(name);
    }
    
    public EntityPlayerMP getPlayer(UUID uuid) {
    	return server.getPlayerList().getPlayerByUUID(uuid);
    }
    
    public GameProfile getProfile(String name) {
    	return server.getPlayerProfileCache().getGameProfileForUsername(name);
    }
    
    public GameProfile getProfile(UUID uuid) {
    	return server.getPlayerProfileCache().getProfileByUUID(uuid);
    }

    public static String readConfigFile(String pathStr) throws IOException {
    	File file = new File(pathStr);
    	if(file.exists()) {
    		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    	} else {
    		file.createNewFile();
    		return null;
    	}
    }
    
    public static void saveConfig(String path, String contents) throws FileNotFoundException {
    	PrintWriter out = new PrintWriter(path);
    	out.println(contents);
    	out.close();
    }
    
    private <T extends Manager> T loadFromConfig(Class<T> clazz, String configName) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
    	String config = readConfigFile(configName);
    	if(config == null) {
    		logger.debug(String.format("No config file \"%s\", creating new instance...", configName));
    		return clazz.getConstructor(CustomMod.class).newInstance(this);
    	} else {
    		logger.debug(String.format("Loaded manager from config file \"%s\"", configName));
    		return gson.fromJson(config, clazz);
    	}
    }
    
    private void loadAll() throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    
    	this.claimsManager = loadFromConfig(ClaimsManager.class, "claims.dat");
    	this.nationManager = loadFromConfig(NationManager.class, "nations.dat");
    	this.powerManager = loadFromConfig(PowerManager.class, "power.dat");	    	
    	this.configuration = new Configuration();
    	
    	// Postinit step
    	this.nationManager.initPlayerNations();
    	
    }
  
 
    public void saveAll() throws FileNotFoundException, IOException {
    	configuration.save();
    	saveConfig("claims.dat", gson.toJson(this.claimsManager));
    	saveConfig("nations.dat", gson.toJson(this.nationManager));
    	saveConfig("power.dat", gson.toJson(this.powerManager));
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

    private class GivePowerTask implements Runnable {
 
    	public void run() {
    		try {
    			for(EntityPlayerMP player: server.getPlayerList().getPlayers()) {
    	    		powerManager.addPower(player, 1);
    	    	}
    		} catch(Exception exception) {
    			exception.printStackTrace();
    		}
    	}
    	
    }
    
}
