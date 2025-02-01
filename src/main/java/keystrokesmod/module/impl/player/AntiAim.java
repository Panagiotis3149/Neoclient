package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AntiAim extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private float rotationSpeed = 10.0f; // Speed of the spinning effect

    public AntiAim() {
        super("AntiAim", category.player);
    }

    @SubscribeEvent
    public void onTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !mc.inGameHasFocus) {
            return;
        }

        // Get current rotation
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;

        // Increment yaw to create spinning effect
        float newYaw = currentYaw + rotationSpeed;

        // Wrap yaw to stay within -180 to 180 degrees
        if (newYaw > 180) newYaw -= 360;
        if (newYaw < -180) newYaw += 360;

        // Update position and rotation
        mc.thePlayer.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, newYaw, currentPitch);
    }
}
