package keystrokesmod.event;

import net.minecraftforge.common.MinecraftForge;

public class Event {

    public static void callSessionEvent() {
        SessionEvent event = new SessionEvent();
        MinecraftForge.EVENT_BUS.post(event);
    }

}
