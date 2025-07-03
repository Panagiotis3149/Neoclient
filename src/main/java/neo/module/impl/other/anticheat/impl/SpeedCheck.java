package neo.module.impl.other.anticheat.impl;

import net.minecraft.entity.player.EntityPlayer;
import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.player.move.PlayerData;

public class SpeedCheck implements AnticheatComponent {
    private double lastDeltaXZ = 0.0;

    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat anticheat) {
        if (!anticheat.speed.isToggled()) return;

        if (player.isSwingInProgress && player.getFoodStats().getFoodLevel() <= 6) {
            anticheat.alert(player, anticheat.speed, "Low Hunger");
        }

        if (data.deltaX == 0 && data.deltaZ == 0) return;

        float deltaYaw = (float) Math.abs(data.yaw - data.prevYaw);

        double deltaXZ = Math.sqrt(data.deltaX * data.deltaX + data.deltaZ * data.deltaZ);
        double accel = Math.abs(deltaXZ - lastDeltaXZ);
        double sqAccel = accel * 100;

        if (deltaYaw > 1.5F && deltaXZ > .15D && sqAccel < 1.0E-5) {
            anticheat.alert(player, anticheat.speed, "Invalid Strafe");
        }

        lastDeltaXZ = deltaXZ;

    }
}
