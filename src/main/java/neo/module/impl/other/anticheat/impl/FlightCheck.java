package neo.module.impl.other.anticheat.impl;

import net.minecraft.entity.player.EntityPlayer;
import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.player.move.PlayerData;

public class FlightCheck implements AnticheatComponent {
    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat anticheat) {
        if (!anticheat.flight.isToggled()) return;


        if (player.capabilities.isCreativeMode
                || player.capabilities.isFlying
                || player.isOnLadder()
                || player.isInWater()) return;

        if (player.ticksExisted >= 100) {

            double motionY = data.motionY;

            if (data.offGroundTicks >= 2) {
                double predictedMotionY = data.lastMotionY;
                predictedMotionY -= 0.08;
                predictedMotionY *= data.prediction;

                if (Math.abs(predictedMotionY) < 0.005) {
                    predictedMotionY = 0;
                }

                if (motionY == 0 && data.moveTicks == 0 && ++data.ticksNoMotionY <= 2) {
                    return;
                }

                double mE = 1E-14;

                if (data.moveTicks == 1) {
                    mE += 0.03;
                } else if (data.moveTicks == 2) {
                    mE += 0.06;
                }

                if (motionY > predictedMotionY + mE) {
                    anticheat.alert(player, anticheat.flight, "Experimental" + "Details: Motion Y/predicted motion Y: " + motionY + " / " + predictedMotionY);
                } else {
                    data.ticksNoMotionY = 0;
                }
            } else {
                data.ticksNoMotionY = 0;
            }
        }
    }
}