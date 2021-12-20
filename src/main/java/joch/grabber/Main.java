package joch.grabber;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;

import java.io.File;

@Mod(modid = "Craft Grabber", name = "Craft Grabber", version = "1.0.0")
public class Main {

    public static final File configDir = new File(System.getProperty("user.home") + "/craftsGrabber");

    public static void OnGrabButton() {
        Main.configDir.mkdir();

        GrabberHandler.Grab();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        if (e == null) {
            starter(null);
        }
    }

    @EventHandler
    public void starter(FMLLoadCompleteEvent e) {
        new joch.grabber.EventHandler();
    }

}