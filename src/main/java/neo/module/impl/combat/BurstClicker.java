package neo.module.impl.combat;

import neo.Neo;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.other.java.Reflection;
import neo.util.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class BurstClicker extends Module {
    private final SliderSetting clicks;
    private final SliderSetting delay;
    private final ButtonSetting delayRandomizer;
    private final ButtonSetting placeWhenBlock;
    private boolean l_c = false;
    private boolean l_r = false;



    /**
     * @deprecated This class is replaced by {@link AutoClicker}
     */

    public BurstClicker() {
        super("DRAGCLICKER (DEPRECATED)", category.combat, 0);
        this.registerSetting(new DescriptionSetting("Autoclicker that dragclicks. Best with delay remover."));
        this.registerSetting(clicks = new SliderSetting("Clicks", 0.0D, 0.0D, 50.0D, 1.0D));
        this.registerSetting(delay = new SliderSetting("Delay (ms)", 5.0D, 1.0D, 40.0D, 1.0D));
        this.registerSetting(delayRandomizer = new ButtonSetting("Delay randomizer", true));
        this.registerSetting(placeWhenBlock = new ButtonSetting("Place when block", false));
    }

    public void onEnable() {
        if (clicks.getInput() != 0.0D && mc.currentScreen == null && mc.inGameHasFocus) {
            Neo.getExecutor().execute(() -> {
                try {
                    int cl = (int) clicks.getInput();
                    int del = (int) delay.getInput();

                    for (int i = 0; i < cl * 2 && this.isEnabled() && Utils.isnull() && mc.currentScreen == null && mc.inGameHasFocus; ++i) {
                        if (i % 2 == 0) {
                            this.l_c = true;
                            if (del != 0) {
                                int realDel = del;
                                if (delayRandomizer.isToggled()) {
                                    realDel = del + Utils.getRandom().nextInt(25) * (Utils.getRandom().nextBoolean() ? -1 : 1);
                                    if (realDel <= 0) {
                                        realDel = del / 3 - realDel;
                                    }
                                }

                                Thread.sleep(realDel);
                            }
                        } else {
                            this.l_r = true;
                        }
                    }

                    this.disable();
                } catch (InterruptedException var5) {
                }

            });
        } else {
            this.disable();
        }
    }

    public void onDisable() {
        this.l_c = false;
        this.l_r = false;
    }

    @SubscribeEvent
    public void r(RenderTickEvent ev) {
        if (Utils.isnull()) {
            if (this.l_c) {
                this.c(true);
                this.l_c = false;
            } else if (this.l_r) {
                this.c(false);
                this.l_r = false;
            }
        }

    }

    private void c(boolean st) {
        boolean r = placeWhenBlock.isToggled() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock;
        if (r) {
            Reflection.rightClick();
        } else {
            int key = mc.gameSettings.keyBindAttack.getKeyCode();
            KeyBinding.setKeyBindState(key, st);
            if (st) {
                KeyBinding.onTick(key);
            }
        }

        Reflection.setButton(r ? 1 : 0, st);
    }
}
