package neo.util.player;

import neo.util.packet.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;

import static neo.util.Utils.mc;

public class DamageUtil {

    public static void damagePlayer(final double value) {
        damagePlayer(DamageType.POSITION, value, true, true);
    }

    public static void damagePlayerr(final double value) {
        damagePlayer(DamageType.POSITION_ROTATION, value, true, true);
    }

    public static void oldNCPTestSelfDamage() {
        for (int i = 0; i < 65 * 2; i++) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.049, mc.thePlayer.posZ, false));
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
        }

        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

    }

    public static void verusTestSelfDamage() {
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.25, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));

    }

    public static void testSelfDamage() {
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.0001, mc.thePlayer.posZ, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
    }

    /**
     * Attempts to damage the user via fall damage.
     *
     * @param type          - the type of self damage to use in the method
     * @param value         - the value to use for the self damage and fall distance calculation
     * @param groundCheck   - if true, you will need to be on the ground for this method to complete successfully
     * @param hurtTimeCheck - if true, you will need to be not taking damage for the method to complete successfully
     */
    public static void damagePlayer(final DamageType type, final double value, final boolean groundCheck, final boolean hurtTimeCheck) {
        if ((!groundCheck || mc.thePlayer.onGround) && (!hurtTimeCheck || mc.thePlayer.hurtTime == 0)) {
            final double x = mc.thePlayer.posX;
            final double y = mc.thePlayer.posY;
            final double z = mc.thePlayer.posZ;

            double fallDistanceReq = 3.1;

            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                final int amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
                fallDistanceReq += (float) (amplifier + 1);
            }

            final int packetCount = (int) Math.ceil(fallDistanceReq / value); // Don't change this unless you know the change won't break the self damage.
            for (int i = 0; i < packetCount; i++) {
                switch (type) {
                    case POSITION_ROTATION: {
                        PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y + value, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        break;
                    }

                    case POSITION: {
                        PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y + value, z, false));
                        PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                        break;
                    }
                }
            }
            PacketUtils.sendPacket(new C03PacketPlayer(true));
        }
    }

    /**
     * Attempts to damage the user via fall damage.
     *
     * @param type          - the type of self damage to use in the method
     * @param value         - the value to use for the self damage and fall distance calculation
     * @param packets       - the amount of packets the self damage will send in order to damage the player
     * @param groundCheck   - if true, you will need to be on the ground for this method to complete successfully
     * @param hurtTimeCheck - if true, you will need to be not taking damage for the method to complete successfully
     */
    public static void damagePlayer(final DamageType type, final double value, final int packets, final boolean groundCheck, final boolean hurtTimeCheck) {
        if ((!groundCheck || mc.thePlayer.onGround) && (!hurtTimeCheck || mc.thePlayer.hurtTime == 0)) {
            final double x = mc.thePlayer.posX;
            final double y = mc.thePlayer.posY;
            final double z = mc.thePlayer.posZ;

            for (int i = 0; i < packets; i++) {
                switch (type) {
                    case POSITION_ROTATION: {
                        PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y + value, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                        break;
                    }

                    case POSITION: {
                        PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y + value, z, false));
                        PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
                        break;
                    }
                }
            }
            PacketUtils.sendPacket(new C03PacketPlayer(true));
        }
    }

    /**
     * The types of damage methods
     */
    public enum DamageType {
        POSITION_ROTATION,
        POSITION
    }
}
