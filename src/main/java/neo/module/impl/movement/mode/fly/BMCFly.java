package neo.module.impl.movement.mode.fly;

import neo.module.ModuleManager;
import neo.module.impl.movement.Fly;
import neo.util.Utils;
import neo.util.player.move.MoveUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;

import static neo.util.Utils.mc;


public class BMCFly {

    public static void BMCFly() {
        if (Fly.index == 6) {
            mc.thePlayer.posY = Fly.floatPos + 0.42;
        }

        if (mc.thePlayer.onGround && Fly.index == 5) {
            Fly.index++;

            if (MoveUtil.isMoving()) {
                mc.thePlayer.jump();
                MoveUtil.strafec(mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.6 : 0.49);
            }

        } else if (!mc.thePlayer.onGround && Fly.index == 5) {
            ModuleManager.fly.toggle();
        }

        if (Fly.offGroundTicks == 1) {
            Utils.getTimer().timerSpeed = 1.05F;
            MoveUtil.strafec(MoveUtil.getSpeed() * 1.08);
        } else if (Fly.offGroundTicks == 2) {
            Utils.getTimer().timerSpeed = 1.15F;
            MoveUtil.strafec(MoveUtil.getSpeed() * 1.08);
        } else if (Fly.offGroundTicks == 3) {
            Utils.getTimer().timerSpeed = 1.25F;
            MoveUtil.strafec(MoveUtil.getSpeed() * 1.06);
        } else if (Fly.offGroundTicks >= 4) {
            Utils.getTimer().timerSpeed = 2.25F;
            MoveUtil.strafec(MoveUtil.getSpeed() * 1.02);
        }

        if (Fly.offGroundTicks >= 10) {ModuleManager.fly.toggle();}

        if (Fly.index < 5) {
            Fly.index++;
        }

        if (Fly.index < 4) {
            Utils.getTimer().timerSpeed = 0.5F;
            mc.thePlayer.setSprinting(true);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        }
        MoveUtil.strafec(MoveUtil.getSpeed());
    }

}
