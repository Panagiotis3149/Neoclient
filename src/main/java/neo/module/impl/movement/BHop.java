package neo.module.impl.movement;

import neo.event.*;
import neo.module.Module;
import neo.module.impl.movement.mode.speed.*;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.player.move.MoveUtil;
import neo.util.Utils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import net.minecraft.potion.Potion;


public class BHop extends Module {
    public static SliderSetting mode;
    public static SliderSetting speed;
    public static ButtonSetting autoJump;
    private final ButtonSetting liquidDisable;
    private final ButtonSetting sneakDisable;
    private final ButtonSetting stopMotion;
    public static String[] modes = new String[]{"Strafe", "Ground", "NCP", "Legit", "Ground2 (Hypixel)", "Vulcan", "Strafe2", "Verus", "OldMiniblox", "Karhu", "VanillaX", "Mospixel", "BMC", "Matrix"};
    public boolean hopping;
    private int ticks = 0;
    private int ticksl = 0;
    private int tickone = 0;
    public static int offGroundTicks = 0;
    private double lastAngle = 999;
    private int ticksSinceVelocity = 0;

    public BHop() {
        super("Speed", Module.category.movement, 44);
        registerSetting(mode = new SliderSetting("Mode", modes, 0));
        registerSetting(speed = new SliderSetting("Speed", 2.0, 0.5, 8, 0.1));
        registerSetting(autoJump = new ButtonSetting("Auto jump", true));
        registerSetting(liquidDisable = new ButtonSetting("Disable in liquid", true));
        registerSetting(sneakDisable = new ButtonSetting("Disable while sneaking", true));
        registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    @Override
    public void onUpdate() {
        if (mc.thePlayer.onGround) offGroundTicks = 0; else offGroundTicks++;
        ticks++; if (ticks > 20) ticks = 0;
        ticksl++; if (ticksl > 200) ticksl = 0;
        tickone++; if (tickone > 1) tickone = 0;
        if (((mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) && liquidDisable.isToggled()) || (mc.thePlayer.isSneaking() && sneakDisable.isToggled())) {
            return;
        }
        switch ((int) mode.getInput()) {
            case 0:
                if (Utils.isMoving()) {
                    if (mc.thePlayer.onGround && autoJump.isToggled()) {
                        mc.thePlayer.jump();
                    }
                    mc.thePlayer.setSprinting(true);
                    Utils.setSpeed(Utils.getHorizontalSpeed() + 0.005 * speed.getInput());
                    hopping = true;
                    break;
                }
                break;
            case 1:
                if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null) {
                    if (!mc.thePlayer.onGround) {
                        break;
                    }
                    if (autoJump.isToggled()) {
                        mc.thePlayer.jump();
                    } else if (!Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !autoJump.isToggled()) {
                        return;
                    }
                    mc.thePlayer.setSprinting(true);
                    double horizontalSpeed = Utils.getHorizontalSpeed();
                    double speedModifier = 0.4847;
                    final int speedAmplifier = Utils.getSpeedAmplifier();
                    switch (speedAmplifier) {
                        case 1:
                            speedModifier = 0.5252;
                            break;
                        case 2:
                            speedModifier = 0.587;
                            break;
                        case 3:
                            speedModifier = 0.6289;
                            break;
                    }
                    double additionalSpeed = speedModifier * ((speed.getInput() - 1.0) / 3.0 + 1.0);
                    if (horizontalSpeed < additionalSpeed) {
                        horizontalSpeed = additionalSpeed;
                    }
                    Utils.setSpeed(horizontalSpeed);
                    hopping = true;
                }
                break;
            case 2:
                Utils.resetTimer();
                if (MoveUtil.isMoving()) {
                    Utils.getTimer().timerSpeed = 1.021F;
                    if (mc.thePlayer.onGround) {
                        MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() + 0.003, mc.thePlayer);
                        if (!Utils.jumpDown())
                            mc.thePlayer.jump();
                    } else {
                        MoveUtil.strafea();
                    }
                    if (offGroundTicks == 4 || offGroundTicks == 7) {
                        mc.thePlayer.motionY *= 1.005F;
                    }
                }
                hopping = true;
                break;
            case 3:
                Utils.resetTimer();
                if (Utils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                    mc.thePlayer.setSprinting(true);
                    Utils.getTimer().timerSpeed = 1.004f;
                    hopping = true;
                }
                break;
            case 4:
                if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null) {
                    mc.thePlayer.setSprinting(true);
                    if (mc.thePlayer.onGround) {
                        MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f, mc.thePlayer);
                        mc.thePlayer.jump();

                        double angle = Math.atan(mc.thePlayer.motionX / mc.thePlayer.motionZ) * (180 / Math.PI);

                        if (lastAngle != 999 && Math.abs(lastAngle - angle) > 20 && ticksSinceVelocity > 20) {
                            int speed = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;

                            switch (speed) {
                                case 0:
                                    MoveUtil.moveFlying(-0.005);
                                    break;

                                case 1:
                                    MoveUtil.moveFlying(-0.035);
                                    break;

                                default:
                                    MoveUtil.moveFlying(-0.04);
                                    break;
                            }
                        }
                        lastAngle = angle;
                    }
                }
                break;
            case 5:
                break;
            case 6:
                if (!MoveUtil.isMoving()) return;
                if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
                    mc.thePlayer.jump();
                }
                MoveUtil.strafea();
                break;
            case 7:
                Utils.resetTimer();
                if (!MoveUtil.isMoving()) return;
                if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
                    mc.thePlayer.jump();
                }
                MoveUtil.strafe(0.345);
                break;
            case 8:
                if (!MoveUtil.isMoving()) return;
                if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
                    mc.thePlayer.jump();
                }
                Utils.setSpeed(0.36);
                MoveUtil.strafea();
                break;
            case 9: KarhuSpeed.KarhuSpeed(); break;
            case 10:
                Utils.resetTimer();
                float xd = 2.5f;
                if (!MoveUtil.isMoving()) return;
                if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
                    MoveUtil.jump(0.46f);
                }
                if (Utils.getHorizontalSpeed() <= xd) {
                    Utils.setSpeed(Utils.getHorizontalSpeed() + 0.08);
                }
                Utils.getTimer().timerSpeed = 1.05f;
                break;
            case 11: MospixelSpeed.MospixelSpeed(); break;
            case 12: BMCSpeed.BMCSpeed(); break;
        }
    }


    @Override
    public void onDisable() {
        BMCSpeed.speed = 0;
        MospixelSpeed.speed = 0;
        Utils.resetTimer();
        if (stopMotion.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
        hopping = false;
        ticksl = 0;
        ticks = 0;
        tickone = 0;
    }

    @Override
    public void onEnable() {
        Utils.resetTimer();
        ticksl = 0;
        ticks = 0;
        tickone = 0;
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (ReceivePacketEvent.getPacket() instanceof S12PacketEntityVelocity) {
            ticksSinceVelocity = 0;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (mode.getInput() == 5) {
            VulcanSpeed.onTick(e);
        }
    }

    @SubscribeEvent
    public void onStrafe(StrafeEvent e) {
        if (mode.getInput() == 13) {
                MatrixSpeed.MatrixSpeed(e);
        }
        if (mode.getInput() == 5) {
            VulcanSpeed.VulcanSpeed(e);
        }
    }
}