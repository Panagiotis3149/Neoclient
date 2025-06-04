package neo.util.other;

import neo.Neo;
import neo.util.render.RenderUtils;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class DebugInfoRenderer extends net.minecraft.client.gui.Gui {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (!Neo.debugger || ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen == null) {
            RenderUtils.renderBPS(true, true);
        }
    }
}
