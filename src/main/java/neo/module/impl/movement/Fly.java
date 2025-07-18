package neo.module.impl.movement;

import neo.event.BlockAABBEvent;
import neo.event.MoveInputEvent;
import neo.event.PreMotionEvent;
import neo.event.ReceivePacketEvent;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.client.Notifications;
import neo.module.impl.other.SlotHandler;
import neo.module.impl.render.BPSCounter;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.*;
import neo.util.packet.PacketUtils;
import neo.util.player.DamageUtil;
import neo.util.player.move.MoveUtil;
import neo.util.player.move.RotationUtils;
import neo.util.render.RenderUtils;
import neo.util.world.block.BlockUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {
    private final SliderSetting mode;
    public static SliderSetting horizontalSpeed;
    private final SliderSetting verticalSpeed;
    private final ButtonSetting showBPS;
    private final ButtonSetting stopMotion;
    private double moveSpeed;
    private boolean d;
    private int stage, ticks, ticksl;
    public static int offGroundTicks;
    private int i;
    private double floatPos;
    private final String[] modes = new String[]{"Vanilla", "Fast", "Fast2", "Glide", "AirPlace", "VerusOld", "Verus", "Mospixel", "BMC", "Test"};
    private double jumpGround = 0.0;
    private boolean wE;
    private double sP;
    private double phase = 1;

    public Fly() {
        super("Fly", category.movement);
        registerSetting(mode = new SliderSetting("Fly", modes, 0));
        registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 1.0, 9.0, 0.1));
        registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 2.0, 1.0, 9.0, 0.1));
        registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    public void onEnable() {
        d = mc.thePlayer.capabilities.isFlying;
        sP = mc.thePlayer.posY;
        moveSpeed = 0;
        stage = mc.thePlayer.onGround ? 0 : -1;
        ticks = 0;
        i = 0;
        floatPos = mc.thePlayer.posY;
        wE = ModuleManager.bpsCounter.isEnabled();
        if (!wE && showBPS.isToggled()) {
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
                setSpeed(0.85 * horizontalSpeed.getInput());
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
                setSpeed(0.4 * horizontalSpeed.getInput());
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
                Utils.setSpeed(0.7);
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
            case 7:
                if (!MoveUtil.isMoving() || mc.thePlayer.isCollidedHorizontally) {
                    stage = -1;
                }


                if (ticksl == 125) {
                    stage = -1;
                    ticks = 0;
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 5, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 5, true));
                    // return;
                    ticksl = 0;
                }

                switch (stage) {
                    case -1:
                        mc.thePlayer.motionY = 0;
                        mc.thePlayer.motionY = (-0.00001);
                        return;
                    case 0:
                        moveSpeed = 0.3;
                        break;
                    case 1:
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.motionY = 0.3999;
                            moveSpeed *= 2.14;
                        }
                        break;
                    case 2:
                        moveSpeed = 1.5;
                        break;
                    default:
                        moveSpeed -= moveSpeed / 109;
                        mc.thePlayer.motionY = 0;
                        mc.thePlayer.motionY = (-0.00001);
                        break;
                }

                mc.thePlayer.jumpMovementFactor = 0F;
                MoveUtil.strafe4(Math.max(moveSpeed, MoveUtil.getAllowedHorizontalDistance()));
                // MoveUtil.setSpeedMTest(Math.max(moveSpeed, MoveUtil.getAllowedHorizontalDistance()));
                stage++;
                break;
            case 8:
                if (i == 6) {
                    mc.thePlayer.posY = floatPos + 0.42;
                }

                if (mc.thePlayer.onGround && i == 5) {
                    i++;

                    if (MoveUtil.isMoving()) {
                        mc.thePlayer.jump();
                        MoveUtil.strafec(mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.6 : 0.49);
                    }

                } else if (!mc.thePlayer.onGround && i == 5) {
                    this.toggle();
                }

                if (offGroundTicks == 1) {
                    Utils.getTimer().timerSpeed = 1.05F;
                    MoveUtil.strafec(MoveUtil.getSpeed() * 1.08);
                } else if (offGroundTicks == 2) {
                    Utils.getTimer().timerSpeed = 1.15F;
                    MoveUtil.strafec(MoveUtil.getSpeed() * 1.08);
                } else if (offGroundTicks == 3) {
                    Utils.getTimer().timerSpeed = 1.25F;
                    MoveUtil.strafec(MoveUtil.getSpeed() * 1.06);
                } else if (offGroundTicks >= 4) {
                    Utils.getTimer().timerSpeed = 2.25F;
                    MoveUtil.strafec(MoveUtil.getSpeed() * 1.02);
                }

                if (offGroundTicks >= 10) {this.toggle();}

                if (i < 5) {
                    i++;
                }

                if (i < 4) {
                    Utils.getTimer().timerSpeed = 0.5F;
                    mc.thePlayer.setSprinting(true);
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                } else {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                }
                MoveUtil.strafec(MoveUtil.getSpeed());
                break;
            case 9:
                double lastY = mc.thePlayer.prevPosY;
                double targetDY = 0.05999999821185753;
                Utils.getTimer().timerSpeed = 0.9f;
                if (phase == 1) {
                    mc.thePlayer.motionZ = 0;
                    mc.thePlayer.motionX = 0;
                    for (double val : MoveUtil.getWaterValues()) {
                        if (Math.abs((mc.thePlayer.posY - lastY) - val) < 1.0E-6) {
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
            case 10:

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
        sP = Double.NaN;
        if (mc.thePlayer.capabilities.allowFlying) {
            mc.thePlayer.capabilities.isFlying = d;
        }
        else {
            mc.thePlayer.capabilities.isFlying = false;
        }
        d = false;
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
        if (!wE && showBPS.isToggled()) {
            ModuleManager.bpsCounter.toggle();
        }
    }


   @SubscribeEvent
   public void onPreMotion(PreMotionEvent e) {
        if (mode.getInput() == 7) {
            e.setOnGround(true);
        }
   }


    public static void setSpeed(final double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
            return;
        }
        double n3 = mc.thePlayer.movementInput.moveForward;
        double n4 = mc.thePlayer.movementInput.moveStrafe;
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (n3 == 0.0 && n4 == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
        }
        else {
            if (n3 != 0.0) {
                if (n4 > 0.0) {
                    rotationYaw += ((n3 > 0.0) ? -45 : 45);
                }
                else if (n4 < 0.0) {
                    rotationYaw += ((n3 > 0.0) ? 45 : -45);
                }
                n4 = 0.0;
                if (n3 > 0.0) {
                    n3 = 1.0;
                }
                else if (n3 < 0.0) {
                    n3 = -1.0;
                }
            }
            final double radians = Math.toRadians(rotationYaw + 90.0f);
            final double sin = Math.sin(radians);
            final double cos = Math.cos(radians);
            mc.thePlayer.motionX = n3 * n * cos + n4 * n * sin;
            mc.thePlayer.motionZ = n3 * n * sin - n4 * n * cos;
        }
    }

}
