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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.mojang.authlib.GameProfile;

import dev.codesoup.mc.commands.ModCommands;
import dev.codesoup.mc.event.ClaimMessagesHandler;
import dev.codesoup.mc.event.CustomEventHandler;
import dev.codesoup.mc.event.ProtectionsHandler;
import dev.codesoup.mc.mcws.Configuration;
import dev.codesoup.mc.mcws.CustomAppender;
import dev.codesoup.mc.mcws.MCWSEventHandler;
import dev.codesoup.mc.mcws.WSServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = CustomMod.MODID, name = CustomMod.NAME, version = CustomMod.VERSION, acceptableRemoteVersions = "*", dependencies = "after:dynmap")
public class CustomMod {

	public static final String MODID = "custommod";
	public static final String NAME = "Custom Mod";
	public static final String VERSION = "1.0";

	private MinecraftServer server;

	private ClaimsManager claimsManager;
	private NationManager nationManager;
	private PowerManager powerManager;
	private MapManager mapManager;

	private ModCommands commands;
	private Configuration configuration;
	private WSServer wsServer;

	public Logger logger;
	public Gson gson;

	private Gson buildGson() {

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.enableComplexMapKeySerialization();

		// Manager deserializers
		gsonBuilder.registerTypeAdapter(NationManager.class, new ManagerCreator<NationManager>(this, NationManager.class));
		gsonBuilder.registerTypeAdapter(ClaimsManager.class, new ManagerCreator<ClaimsManager>(this, ClaimsManager.class));
		gsonBuilder.registerTypeAdapter(PowerManager.class, new ManagerCreator<PowerManager>(this, PowerManager.class));

		return gsonBuilder.create();

	}

	private void attachAppender() {
		org.apache.logging.log4j.core.Logger srvLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
		srvLogger.addAppender(new CustomAppender(this));
	}

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent event) {
		this.logger = event.getModLog();
		this.gson = buildGson();
		this.commands = new ModCommands(this);
	}

	@EventHandler
	public void onInit(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ClaimMessagesHandler(this));
		MinecraftForge.EVENT_BUS.register(new CustomEventHandler(this));
		MinecraftForge.EVENT_BUS.register(new MCWSEventHandler(this));
		MinecraftForge.EVENT_BUS.register(new ProtectionsHandler(this));
		commands.onInit(event);
	}

	@EventHandler
	public void onServerStart(FMLServerStartingEvent event) {

		try {
			this.loadAll();
		} catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException
				| NoSuchMethodException exception) {
			this.logger.fatal("Failed to load something from configuration.");
			exception.printStackTrace();
		}

		this.server = event.getServer();
		this.wsServer = new WSServer(this);
		this.mapManager = new MapManager(this);

		commands.onServerStart(event);
		attachAppender();

	}

	@EventHandler
	public void stopping(FMLServerStoppingEvent event) {

		try {
			this.wsServer.stop();
		} catch (IOException | InterruptedException exception) {
			this.logger.error("Failed to close websocket.");
			exception.printStackTrace();
		}

	}

	/* GETTERS */
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

	public String getName(EntityPlayer player) {
		return getName(player.getGameProfile());
	}

	public String getName(UUID uuid) {
		return getName(getProfile(uuid));
	}

	public String getName(GameProfile profile) {

		if (profile == null)
			return "Unknown";

		Nation nation = nationManager.getNation(profile.getId());

		String prefix = "";
		if (nation != null) {
			prefix = String.format("%s[%s]%s ", nation.getColor(), nation.getName(), TextFormatting.RESET);
		}

		return String.format("%s%s", prefix, profile.getName());

	}

	/* UTILITIES */
	public void broadcast(String message) {
		this.server.getPlayerList().sendMessage(new TextComponentString(message));
	}

	public void broadcastToOps(String message) {
		for (EntityPlayerMP player : this.server.getPlayerList().getPlayers()) {
			if (player.canUseCommand(4, "")) {
				player.sendMessage(new TextComponentString(message));
			}
		}
	}

	public static String readConfigFile(String pathStr) throws IOException {
		File file = new File(pathStr);
		if (file.exists()) {
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

	private <T extends RequiresMod> T loadFromConfig(Class<T> clazz, String configName) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
		String config = readConfigFile(configName);
		if (config == null) {
			logger.debug(String.format("No config file \"%s\", creating new instance...", configName));
			return clazz.getConstructor(CustomMod.class).newInstance(this);
		} else {
			logger.debug(String.format("Loaded manager from config file \"%s\"", configName));
			return gson.fromJson(config, clazz);
		}
	}

	private void loadAll() throws IOException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException {

		this.claimsManager = loadFromConfig(ClaimsManager.class, "claims.dat");
		this.nationManager = loadFromConfig(NationManager.class, "nations.dat");
		this.powerManager = loadFromConfig(PowerManager.class, "power.dat");
		this.configuration = new Configuration();

		// Postinit step
		this.nationManager.postInit();

	}

	public void saveAll() throws FileNotFoundException, IOException {
		configuration.save();
		saveConfig("claims.dat", gson.toJson(this.claimsManager));
		saveConfig("nations.dat", gson.toJson(this.nationManager));
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
			} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exception) {
				return null;
			}
		}

	}

}
