package neo.mixins.impl.client;

import neo.event.MoveInputEvent;
import neo.event.PostPlayerInputEvent;
import neo.event.PrePlayerInputEvent;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {
    @Shadow
    @Final
    private GameSettings gameSettings;

    @Overwrite
    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        MoveInputEvent moveInputEvent = new MoveInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);
        PrePlayerInputEvent prePlayerInputEvent = new PrePlayerInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);

        MinecraftForge.EVENT_BUS.post(moveInputEvent);
        MinecraftForge.EVENT_BUS.post(prePlayerInputEvent);

        double sneakMultiplier = moveInputEvent.getSneakSlowDown();
        this.moveForward = moveInputEvent.getForward();
        this.moveStrafe = moveInputEvent.getStrafe();
        this.jump = moveInputEvent.isJump();
        this.sneak = moveInputEvent.isSneak();

        if (this.sneak) {
            this.moveStrafe = (float) ((double) this.moveStrafe * sneakMultiplier);
            this.moveForward = (float) ((double) this.moveForward * sneakMultiplier);
        }

        MinecraftForge.EVENT_BUS.post(new PostPlayerInputEvent());
    }
}
