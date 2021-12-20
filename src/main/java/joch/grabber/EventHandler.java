package joch.grabber;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import joch.grabber.utils.EventHelper;
import org.lwjgl.input.Keyboard;

public class EventHandler {
    public EventHandler() {
        EventHelper.register(this);
    }

    @SubscribeEvent
    public void keyEvent(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_G)) {
            Main.OnGrabButton();
        }
    }
}