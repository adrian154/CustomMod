package dev.codesoup.mc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import dev.codesoup.mc.commands.AllianceCommand;
import dev.codesoup.mc.commands.BaseCommand;
import dev.codesoup.mc.commands.ClaimCommand;
import dev.codesoup.mc.commands.GivePowerCommand;
import dev.codesoup.mc.commands.InvitationsCommand;
import dev.codesoup.mc.commands.PowerCommand;
import dev.codesoup.mc.commands.ProtectCommand;
import dev.codesoup.mc.commands.SetSpawnCommand;
import dev.codesoup.mc.commands.TogglePVPCommand;
import dev.codesoup.mc.commands.UnclaimCommand;
import dev.codesoup.mc.event.CustomEventHandler;
import dev.codesoup.mc.mcws.Configuration;
import dev.codesoup.mc.mcws.WSServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

@Mod(modid = CustomMod.MODID, name = CustomMod.NAME, version = CustomMod.VERSION, acceptableRemoteVersions = "*", dependencies="after:dynmap")
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
    private MapManager mapManager;
    
    private Configuration configuration;
    private WSServer wsServer;
    
    public Logger logger;
    public Gson gson;
    
    private ScheduledExecutorService executor;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    
    	this.logger = event.getModLog();
    	
    	GsonBuilder gsonBuilder = new GsonBuilder();
    	gsonBuilder.enableComplexMapKeySerialization();
    	gsonBuilder.registerTypeAdapter(AllianceManager.class, new ManagerCreator<AllianceManager>(this, AllianceManager.class));
    	gsonBuilder.registerTypeAdapter(ClaimsManager.class, new ManagerCreator<ClaimsManager>(this, ClaimsManager.class));
    	gsonBuilder.registerTypeAdapter(PowerManager.class, new ManagerCreator<PowerManager>(this, PowerManager.class));
        this.gson = gsonBuilder.create();
        
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
    		this.loadAll();
    	} catch(IOException exception) {
    		this.logger.fatal("Exception while initializing: " + exception.getMessage());
    	}	
    	
    	registerCommands(event);
    	startPassivePowerTask();
    	
    	this.wsServer = new WSServer(this);
    	wsServer.start();
    	
    	this.mapManager = new MapManager(this);

    }
    
    @EventHandler
    public void init(FMLServerStoppingEvent event) {
    	
    	try {
    		this.wsServer.stop();
    	} catch(IOException | InterruptedException exception) {
    		this.logger.error("Uh-oh, super bad thingy: " + exception.getMessage());
    	}
    	
    }
    
    @EventHandler
    public void entityRegistration(RegistryEvent.Register<EntityEntry> event) {
    	
    }
    
    private void startPassivePowerTask() {
    	Runnable timerTask = new GivePowerTask(this);
    	executor = Executors.newScheduledThreadPool(1);
    	executor.scheduleAtFixedRate(timerTask, 0, 60 * 5, TimeUnit.SECONDS);
    }
    
    private void registerCommands(FMLServerStartingEvent event) {
    	event.registerServerCommand(new TogglePVPCommand(this));
    	event.registerServerCommand(new ClaimCommand(this));
    	event.registerServerCommand(new AllianceCommand(this));
    	event.registerServerCommand(new InvitationsCommand(this));
    	event.registerServerCommand(new PowerCommand(this));
    	event.registerServerCommand(new UnclaimCommand(this));
    	event.registerServerCommand(new SetSpawnCommand(this));
    	event.registerServerCommand(new BaseCommand(this));
    	event.registerServerCommand(new ProtectCommand(this));
    	event.registerServerCommand(new GivePowerCommand(this));
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
    
    public Configuration getConfiguration() {
    	return this.configuration;
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
    
    private String readConfigFile(String pathStr) throws IOException {
    	Path path = Paths.get(pathStr);
    	if(Files.exists(path)) {
    		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    	} else {
    		Files.createFile(path);
    		return null;
    	}
    }
    
    private void saveConfig(String path, String contents) throws FileNotFoundException {
    	PrintWriter out = new PrintWriter(path);
    	out.println(contents);
    	out.close();
    }
    
    private void loadAll() throws IOException {
    
    	String claimsData = readConfigFile("claims.dat");
    	String alliancesData = readConfigFile("alliances.dat");
    	String powerData = readConfigFile("power.dat");

    	this.claimsManager = claimsData != null ? this.gson.fromJson(claimsData, ClaimsManager.class) : new ClaimsManager(this);
    	this.allianceManager = alliancesData != null ? this.gson.fromJson(alliancesData, AllianceManager.class) : new AllianceManager(this);
    	this.powerManager = powerData != null ? this.gson.fromJson(powerData, PowerManager.class) : new PowerManager(this);

    	this.configuration = new Configuration();
    	
    	// necessary postloading step
    	this.allianceManager.initPlayerAlliances();
    
    }
  
 
    public void saveAll() throws FileNotFoundException {
    	saveConfig("claims.dat", gson.toJson(this.claimsManager));
    	saveConfig("alliances.dat", gson.toJson(this.allianceManager));
    	saveConfig("power.dat", gson.toJson(this.powerManager));
    }
    
    private class ManagerCreator<T extends RequiresMod> implements InstanceCreator<T> {
    
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
    
    private void givePower() {
    	for(EntityPlayerMP player: this.getServer().getPlayerList().getPlayers()) {
    		powerManager.addPower(player, 1);
    	}
    }
    
    private class GivePowerTask implements Runnable {
    	
    	private CustomMod mod;
    	
    	public GivePowerTask(CustomMod mod) {
    		this.mod = mod;
    	}
    	
    	public void run() {
    		try {
    			mod.givePower();
    		} catch(Exception exception) {
    			exception.printStackTrace();
    		}
    	}
    	
    }
    
}
