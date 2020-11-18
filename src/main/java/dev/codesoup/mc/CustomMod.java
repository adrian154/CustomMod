package dev.codesoup.mc;

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
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    	
    	this.customEventHandler = new CustomEventHandler(this);
    	MinecraftForge.EVENT_BUS.register(this.customEventHandler);
    	
    }
    
    @EventHandler
    public void init(FMLServerStartingEvent event) {
    	event.registerServerCommand(new TogglePVPCommand(this));
    }
    
    public CustomEventHandler getEventHandler() {
    	return this.customEventHandler;
    }
    
}
