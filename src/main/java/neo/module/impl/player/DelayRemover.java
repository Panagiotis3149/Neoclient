package neo.module.impl.player;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.other.java.Reflection;
import neo.util.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DelayRemover extends Module { // from b4 src
    public static ButtonSetting oldReg, removeJumpTicks;

    public DelayRemover() {
        super("DelayRemover", category.player, 0);
        this.registerSetting(oldReg = new ButtonSetting("1.7 hitreg", true));
        this.registerSetting(removeJumpTicks = new ButtonSetting("Remove jump ticks", false));
    }

    @SubscribeEvent
    public void onTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !mc.inGameHasFocus || !Utils.isnull()) {
            return;
        }
        if (oldReg.isToggled()) {
            try {
                Reflection.leftClickCounter.set(mc, 0);
            } catch (IllegalAccessException ex) {
            } catch (IndexOutOfBoundsException ex2) {
            }
        }
        if (removeJumpTicks.isToggled()) {
            try {
                Reflection.jumpTicks.set(mc.thePlayer, 0);
            } catch (IllegalAccessException ex3) {
            } catch (IndexOutOfBoundsException ex4) {
            }
        }
    }
}
