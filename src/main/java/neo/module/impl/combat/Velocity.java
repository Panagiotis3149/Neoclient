package neo.module.impl.combat;

import neo.event.ReceivePacketEvent;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {
    public static SliderSetting mode;
    public static SliderSetting horizontal;
    public static SliderSetting vertical;
    private final SliderSetting chance;
    private final ButtonSetting onlyWhileTargeting;
    private final ButtonSetting disableS;
    private final ButtonSetting onlyInAir;

    private static final String[] modes = {"Custom", "CancelPacket"};

    public Velocity() {
        super("Velocity", category.combat, 0);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(horizontal = (SliderSetting) new SliderSetting("Horizontal", 90.0D, 0.0D, 100.0D, 1.0D)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(vertical   = (SliderSetting) new SliderSetting("Vertical",   100.0D, 0.0D, 100.0D, 1.0D)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(chance     = (SliderSetting) new SliderSetting("Chance",     100.0D, 0.0D, 100.0D, 1.0D, "%")
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(onlyWhileTargeting = (ButtonSetting) new ButtonSetting("Only while targeting", false)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(disableS   = (ButtonSetting) new ButtonSetting("Disable while holding S", false)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(onlyInAir  = (ButtonSetting) new ButtonSetting("OnlyInAir", true)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!this.isEnabled()) return;
        switch ((int) mode.getInput()) {
            case 1: // CancelPacket
                Packet<?> packet = ReceivePacketEvent.getPacket();
                if (packet instanceof S12PacketEntityVelocity) {
                    event.cancelEvent();
                }
                break;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent ev) {
        if (!this.isEnabled()) return;
        switch ((int) mode.getInput()) {
            case 0:
                if (Utils.isnull()) return;
                if (!ModuleManager.bedAura.cancelKnockback()) return;
                if (ModuleManager.antiKnockback.isEnabled()) return;
                if (onlyInAir.isToggled() && mc.thePlayer.onGround) return;
                if (mc.thePlayer.maxHurtTime <= 0 || mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime) return;
                if (onlyWhileTargeting.isToggled()
                        && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) return;
                if (disableS.isToggled()
                        && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) return;
                if (chance.getInput() == 0) return;
                if (chance.getInput() != 100
                        && Math.random() >= chance.getInput() / 100.0D) return;

                if (horizontal.getInput() != 100.0D) {
                    mc.thePlayer.motionX *= horizontal.getInput() / 100;
                    mc.thePlayer.motionZ *= horizontal.getInput() / 100;
                }
                if (vertical.getInput() != 100.0D) {
                    mc.thePlayer.motionY *= vertical.getInput() / 100;
                }
                break;
            // future modes go here
        }
    }
}
