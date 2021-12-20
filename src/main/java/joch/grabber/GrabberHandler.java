package joch.grabber;

import com.google.gson.JsonArray;
import joch.grabber.grabbers.*;
import joch.grabber.utils.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GrabberHandler {

    private static final File craftsFile = new File(Main.configDir, "crafts.json");

    public static void Grab() {

        JsonArray result = new JsonArray();

        System.out.println("grabbing;;");

        /*System.out.println("mods:::");
        for(Map.Entry<?, ?> test : Loader.instance().getIndexedModList().entrySet()){
            System.out.println( "\n" + test.getKey().toString() );
        }*/

        result.addAll(CraftsGrabber.Grab());
        result.addAll(FurnaceGrabber.Grab());
        result.addAll(ThermalGrabber.Grab());
        result.addAll(EnderIOGrabber.Grab());
        result.addAll(OreDictionaryGrabber.Grab());
        result.addAll(IC2Grabber.Grab());
        result.addAll(ImmersiveGrabber.Grab());
        result.addAll(AvaritiaGrabber.Grab());
        result.addAll(AppEngGrabber.Grab());
        result.addAll(ThaumCraftGrabber.Grab());
        result.addAll(ExNihilio.Grab());
        result.addAll(BotaniaGrabber.Grab());

        Logger.Save();

        try {
            if (craftsFile.exists()) {
                craftsFile.delete();
            }
            craftsFile.createNewFile();

            FileWriter writer = new FileWriter(craftsFile);
            writer.write(result.toString());
            writer.close();

        } catch (IOException e) {}

        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("Done!"));
        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("Checkout §c" + Main.configDir.getPath()));
        Minecraft.getMinecraft().thePlayer.playSound("entity.experience_orb.pickup", 1, 1);
    }
}