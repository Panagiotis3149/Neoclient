package neo.module.impl.player;

import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.other.java.Reflection;
import neo.util.Utils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class FastPlace extends Module {
    public SliderSetting tickDelay;
    public ButtonSetting blocksOnly, pitchCheck;

    public FastPlace() {
        super("FastPlace", Module.category.player, 0);
        this.registerSetting(tickDelay = new SliderSetting("Tick delay", 1.0, 1.0, 3.0, 1.0));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));
        this.registerSetting(pitchCheck = new ButtonSetting("Pitch check", false));
    }

    @SubscribeEvent
    public void a(PlayerTickEvent e) {
        if (e.phase == Phase.END) {
            if (ModuleManager.scaffold.stopFastPlace()) {
                return;
            }
            if (Utils.isntnull() && mc.inGameHasFocus && Reflection.rightClickDelayTimerField != null) {
                if (blocksOnly.isToggled()) {
                    ItemStack item = mc.thePlayer.getHeldItem();
                    if (item == null || !(item.getItem() instanceof ItemBlock)) {
                        return;
                    }
                }

                try {
                    int c = (int) tickDelay.getInput();
                    if (c == 0) {
                        Reflection.rightClickDelayTimerField.set(mc, 0);
                    } else {
                        if (c == 4) {
                            return;
                        }

                        int d = Reflection.rightClickDelayTimerField.getInt(mc);
                        if (d == 4) {
                            Reflection.rightClickDelayTimerField.set(mc, c);
                        }
                    }
                } catch (IllegalAccessException var4) {
                } catch (IndexOutOfBoundsException var5) {
                }
            }
        }
    }
}
