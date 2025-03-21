package keystrokesmod.module.impl.movement;

import keystrokesmod.event.*;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.funcs.KarhuSpeed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Move;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import net.minecraft.potion.Potion;

import static keystrokesmod.Raven.mc;
import static keystrokesmod.utility.MoveUtil.direction;


public class BHop extends Module {
    private SliderSetting mode;
    public static SliderSetting speed;
    public static ButtonSetting autoJump;
    private ButtonSetting liquidDisable;
    private ButtonSetting sneakDisable;
    private ButtonSetting stopMotion;
    private String[] modes = new String[]{"Strafe", "Ground", "NCP", "Legit", "Ground2 (Hypixel)", "Vulcan", "Strafe2", "Verus", "Miniblox", "Karhu", "VanillaX", "Mospixel"};
    public boolean hopping;
    private int ticks = 0;
    private int ticksl = 0;
    private int tickone = 0;
    public static int offGroundTicks = 0;
    private double lastAngle = 999;
    private int ticksSinceVelocity = 0;


    public BHop() {
        super("Speed", Module.category.movement, 44);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(speed = new SliderSetting("Speed", 2.0, 0.5, 8, 0.1));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
        this.registerSetting(liquidDisable = new ButtonSetting("Disable in liquid", true));
        this.registerSetting(sneakDisable = new ButtonSetting("Disable while sneaking", true));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    @Override
    public void onUpdate() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }
        ticks++;
        if (ticks > 20) ticks = 0;
        ticksl++;
        if (ticksl > 200) ticksl = 0;
        tickone++;
        if (tickone > 1) tickone = 0;
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
                if (!MoveUtil.isMoving()) return;
                if (offGroundTicks == 0) {
                    if (!Utils.jumpDown()) {
                        mc.thePlayer.jump();
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MoveUtil.strafe(0.6, mc.thePlayer);
                        } else {
                            MoveUtil.strafe(0.485, mc.thePlayer);
                        }
                    }
                }
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
                MoveUtil.strafe5(0.345);
                break;
            case 8:
                if (!MoveUtil.isMoving()) return;
                if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
                    mc.thePlayer.jump();
                }
                Utils.setSpeed(0.36);
                MoveUtil.strafea();
                break;
            case 9:
                KarhuSpeed.KarhuSpeed();
                break;
            case 10:
                Utils.resetTimer();
                float xd = 2.47f;
                if (!MoveUtil.isMoving()) return;
                if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
                    MoveUtil.jump(0.46f);
                }
                if (Utils.getHorizontalSpeed() <= xd) {
                    Utils.setSpeed(Utils.getHorizontalSpeed() + 0.083);
                }
                Utils.getTimer().timerSpeed = 1.06f;
                break;
            case 11:
                Utils.resetTimer();
                if (MoveUtil.isMoving()) {
                    Utils.getTimer().timerSpeed = 1.021F;
                    if (mc.thePlayer.onGround) {
                        MoveUtil.strafe5(0.29);
                        MoveUtil.jump(0.40F);
                    }
                    MoveUtil.strafe5(0.29);
                    if (offGroundTicks > 5) {
                        mc.thePlayer.motionY -= 0.03F;
                    }
                    hopping = true;
                    break;
                }
        }
    }

    @Override
    public void onDisable() {
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
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            ticksSinceVelocity = 0;
        }
    }
}