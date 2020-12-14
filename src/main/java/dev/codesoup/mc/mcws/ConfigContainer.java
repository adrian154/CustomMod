package dev.codesoup.mc.mcws;

import java.util.ArrayList;
import java.util.List;

// only for use with gson
// this might be an antipattern, but i also have no idea what i'm doing
public class ConfigContainer {

	public List<String> keys;
	public int port;

	public ConfigContainer() {
		keys = new ArrayList<String>();
		port = 1738;
	}
	
}
