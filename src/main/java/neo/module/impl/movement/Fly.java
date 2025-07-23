package neo.module.impl.movement;

import neo.event.*;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.movement.mode.fly.BMCFly;
import neo.module.impl.movement.mode.fly.MospixelFly;
import neo.module.impl.other.SlotHandler;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.*;
import neo.util.other.MathUtil;
import neo.util.player.move.MoveUtil;
import neo.util.player.move.RotationUtils;
import neo.util.world.block.BlockUtils;
import net.minecraft.block.BlockAir;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {

    private final SliderSetting mode;
    public static SliderSetting horizontalSpeed;
    private final SliderSetting verticalSpeed;
    private final ButtonSetting showBPS;
    private final ButtonSetting stopMotion;
    public static double moveSpeed;
    private boolean isFlying;
    public static int stage, ticks, ticksl;
    public static int offGroundTicks;
    public static int index;
    public static double floatPos;
    private double jumpGround = 0.0;
    private boolean showCounter;
    private double sP;
    private double phase = 1;
    int phase1 = -1;
    int speedType = 0;
    boolean time = true;

    public Fly() {
        super("Fly", category.movement);
        String[] modes = new String[]{"Vanilla", "Fast", "Fast2", "Glide", "AirPlace", "VerusOld", "Verus", "Mospixel", "BMC", "Test", "Test2"};
        registerSetting(mode = new SliderSetting("Fly", modes, 0));
        registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 1.0, 9.0, 0.1));
        registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 2.0, 1.0, 9.0, 0.1));
        registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    public void onEnable() {
        isFlying = mc.thePlayer.capabilities.isFlying;
        sP = mc.thePlayer.posY;
        moveSpeed = 0;
        stage = mc.thePlayer.onGround ? 0 : -1;
        ticks = 0;
        index = 0;
        floatPos = mc.thePlayer.posY;
        showCounter = ModuleManager.bpsCounter.isEnabled();
        if (!showCounter && showBPS.isToggled()) {
            ModuleManager.bpsCounter.toggle();
        }
    }

    public void onUpdate() {
        ticksl++;
        ticks++;
        if (ticks > 20) ticks = 0;
        if (!mc.thePlayer.onGround) {
            offGroundTicks++;
        } else {
            offGroundTicks = 0;
        }
        switch ((int) mode.getInput()) {
            case 0:
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.capabilities.setFlySpeed((float) (0.05000000074505806 * horizontalSpeed.getInput()));
                mc.thePlayer.capabilities.isFlying = true;
                break;
            case 1:
                mc.thePlayer.onGround = true;
                if (mc.currentScreen == null) {
                    if (Utils.jumpDown()) {
                        mc.thePlayer.motionY = 0.3 * verticalSpeed.getInput();
                    } else if (Utils.jumpDown()) {
                        mc.thePlayer.motionY = -0.3 * verticalSpeed.getInput();
                    } else {
                        mc.thePlayer.motionY = 0.0;
                    }
                } else {
                    mc.thePlayer.motionY = 0.0;
                }
                mc.thePlayer.capabilities.setFlySpeed(0.2f);
                mc.thePlayer.capabilities.isFlying = true;
                MoveUtil.setFSpeed(0.85 * horizontalSpeed.getInput());
                break;
            case 2:
                double nextDouble = RandomUtils.nextDouble(1.0E-7, 1.2E-7);
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    nextDouble = -nextDouble;
                }
                if (!mc.thePlayer.onGround) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + nextDouble, mc.thePlayer.posZ);
                }
                mc.thePlayer.motionY = 0.0;
                MoveUtil.setFSpeed(0.4 * horizontalSpeed.getInput());
                break;
            case 3:
                double v = 0.01 + 1E-6;
                if (mc.thePlayer.isAirBorne) mc.thePlayer.motionY = -0.03;
                if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) mc.thePlayer.motionY = v;
                break;
            case 4:
                SlotHandler.setCurrentSlot(Utils.getSlot());

                if (mc.thePlayer.onGround) {
                    if (!Utils.jumpDown())
                        mc.thePlayer.jump();
                } else if (mc.thePlayer.motionY < 0) {
                    if (!Utils.jumpDown() && mc.thePlayer.motionY > -0.25) {
                        return;
                    }

                    BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down();
                    if (BlockUtils.replaceable(pos)) {
                        mc.playerController.onPlayerRightClick(
                                mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem(),
                                pos, EnumFacing.UP, new Vec3(mc.thePlayer.posX, pos.getY(), mc.thePlayer.posZ)
                        );
                        RotationUtils.renderPitch = 90;
                        mc.thePlayer.swingItem();
                    }
                }
                break;
            case 5:
                if (mc.thePlayer.isSneaking() || !Utils.isMoving()) return;
                if (MoveUtil.canSprint(true)) mc.thePlayer.setSprinting(true);
                if (mc.thePlayer.isAirBorne) mc.thePlayer.motionY = -0.08;
                Utils.setSpeed(0.6);
                mc.thePlayer.setJumping(false);
                mc.thePlayer.onGround = true;
                break;
            case 6:
                final double constantMotionValue = 0.41999998688697815;
                final float constantMotionJumpGroundValue = 0.76f;
                if (mc.thePlayer.onGround) {
                    jumpGround = mc.thePlayer.posY;
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.posY > jumpGround + constantMotionJumpGroundValue) {
                    MoveUtil.strafe4(0.31);
                    mc.thePlayer.motionY = constantMotionValue;
                    jumpGround = mc.thePlayer.posY;
                }
                break;
            case 7:
                MospixelFly.MospixelFly();
                break;
            case 8:
                BMCFly.BMCFly();
                break;
            case 9:
                double lastY = mc.thePlayer.prevPosY;
                double targetDY = 0.05999999821185753;
                Utils.getTimer().timerSpeed = 0.9f;
                if (phase == 1) {
                    mc.thePlayer.motionZ = 0;
                    mc.thePlayer.motionX = 0;
                    for (double val : MoveUtil.getWaterValues()) {
                        if (Math.abs((mc.thePlayer.posY - lastY) - val) < 1.0E-14) {
                            targetDY = val;
                            break;
                        }
                    }

                    double newY = Utils.bypass(lastY + targetDY);
                    mc.thePlayer.setPosition(mc.thePlayer.lastTickPosX, newY, mc.thePlayer.lastTickPosZ);
                    if (mc.thePlayer.posY > sP + 0.95) {
                        phase = 2;
                    }
                }

                if (phase == 2) {
                    mc.thePlayer.motionY = 0.0010261658043049238;
                    if (mc.thePlayer.ticksExisted % 2 == 0 || !Utils.isBypass(mc.thePlayer.posY)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, Utils.bypass(mc.thePlayer.posY), mc.thePlayer.posZ);
                    }
                }

                break;
        }
    }


    @SubscribeEvent
    public void onMove(MoveEvent e) {
        if (mode.getInput() == 10) {
            if (phase1 == -1) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.3999;
                } else {
                    if (mc.thePlayer.motionY < 0) {
                        phase1 = 1;
                    }
                }
                speedType = 0;
            }
            if (phase1 == 1) {
                if (time) {
                    e.y += .05;
                    time = false;
                    speedType = 0;
                }
                mc.thePlayer.motionY = -0.00000001;
                e.setY(mc.thePlayer.ticksExisted % 2 == 0 ? 1E-9 : -1E-9);
                if (!time) {
                    speedType = 1;
                }
            }
            if (speedType == 1) {
                MoveUtil.strafe5(MoveUtil.getAllowedHorizontalDistance() + .01);
            }
            if (speedType == 0) {
                MoveUtil.strafe5(MoveUtil.getAllowedHorizontalDistance() + (Utils.getHorizontalSpeed() / 109));
            }
            if (MathUtil.goofB(mc.thePlayer.ticksExisted, 40, 1) && speedType == 1) {
                MoveUtil.strafe5(1);
            }
        }
    }

    @SubscribeEvent
    public void onBlockAABB(BlockAABBEvent event) {
        if (mode.getInput() == 9 && phase == 2) {
            if (event.getBlock() instanceof BlockAir && !mc.thePlayer.isSneaking()) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        }
    }


    public void onDisable() {
        Utils.stopBlink();
        Utils.resetTimer();
        phase = 1;
        phase1 = -1;
        speedType = 0;
        time = true;
        sP = Double.NaN;
        if (mc.thePlayer.capabilities.allowFlying) {
            mc.thePlayer.capabilities.isFlying = isFlying;
        } else {
            mc.thePlayer.capabilities.isFlying = false;
        }
        isFlying = false;
        switch ((int) mode.getInput()) {
            case 0:
            case 1: {
                mc.thePlayer.capabilities.setFlySpeed(0.05F);
                break;
            }
            case 2: {
                break;
            }
        }
        if (stopMotion.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
        ticksl = 0;
        moveSpeed = 0;
        if (!showCounter && showBPS.isToggled()) {
            ModuleManager.bpsCounter.toggle();
        }
    }


    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        if (mode.getInput() == 7) {
            e.setOnGround(true);
        }
    }
}
