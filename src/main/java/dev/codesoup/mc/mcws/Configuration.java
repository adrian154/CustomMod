package dev.codesoup.mc.mcws;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;

public class Configuration {

	private File configFile;
	private Gson gson;
	private ConfigContainer configContainer;
	
	public Configuration() throws IOException {
		this.gson = new Gson();
		this.configFile = new File("mcws-config.json");
		this.reload();
	}
	
	public void reload() throws IOException {
		
		if(configFile.exists()) {
			String str = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
			configContainer = gson.fromJson(str, ConfigContainer.class);
		} else {
			configContainer = new ConfigContainer();
		}
		
	}
	
	public void save() throws IOException {
		
		PrintWriter pw = new PrintWriter(configFile.getPath());
		pw.println(gson.toJson(configContainer));
		pw.close();
		
	}
	
	public void addKey(String key) {
		configContainer.keys.add(key);
	}
	
	public boolean verifyKey(String key) {
		return configContainer.keys.contains(key);
	}
	
	public int getPort() {
		return configContainer.port;
	}

}