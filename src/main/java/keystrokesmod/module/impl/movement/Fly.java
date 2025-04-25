package keystrokesmod.module.impl.movement;


import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.*;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {
    private SliderSetting mode;
    public static SliderSetting horizontalSpeed;
    private SliderSetting verticalSpeed;
    private ButtonSetting showBPS;
    private ButtonSetting stopMotion;
    private boolean d;
    private boolean a = false;
    private int ticks;
    private int dC = 0;
    private int ticksl;
    public int offGroundTicks = 0;
    private String[] modes = new String[]{"Vanilla", "Fast", "Fast2", "Glide", "AirPlace", "VerusOld", "Verus1"};
    private keystrokesmod.event.SendPacketEvent SendPacketEvent;
    private double jumpGround = 0.0;

    public Fly() {
        super("Fly", category.movement);
        this.registerSetting(mode = new SliderSetting("Fly", modes, 0));
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 1.0, 9.0, 0.1));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 2.0, 1.0, 9.0, 0.1));
        this.registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    public void onEnable() {
        this.d = mc.thePlayer.capabilities.isFlying;
        if (mode.getInput() == 5) {
            Utils.verusTestSelfDamage();
            Utils.blinkPackets(SendPacketEvent);


            // just for the funnsies
            String message = "Verus airlines, please fasten your seatbelts as we prepare to take off!";
            C01PacketChatMessage chatPacket = new C01PacketChatMessage(message);
            mc.thePlayer.sendQueue.addToSendQueue(chatPacket);
        }

        }

    public void onUpdate() {
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
                    this.jumpGround = mc.thePlayer.posY;
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.posY > this.jumpGround + constantMotionJumpGroundValue) {
                    MoveUtil.strafe4(0.31);
                    mc.thePlayer.motionY = constantMotionValue;
                    this.jumpGround = mc.thePlayer.posY;
                }
        }
    }

    public void onDisable() {
        Utils.stopBlink();
        Utils.resetTimer();
        if (mc.thePlayer.capabilities.allowFlying) {
            mc.thePlayer.capabilities.isFlying = this.d;
        }
        else {
            mc.thePlayer.capabilities.isFlying = false;
        }
        this.d = false;
        switch ((int) mode.getInput()) {
            case 0:
            case 1: {
                mc.thePlayer.capabilities.setFlySpeed(0.05F);
                break;
            }
            case 2: {
                a = false;
                break;
            }
        }
        if (stopMotion.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (!showBPS.isToggled() || e.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
        }
        RenderUtils.renderBPS(true, false);
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
