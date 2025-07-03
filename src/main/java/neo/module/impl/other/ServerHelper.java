package neo.module.impl.other;

import org.joml.Vector3d;
import neo.event.*;
import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ServerHelper extends Module {
    public static String[] modes = new String[]{"Miniblox", "BloxD"};
    public static SliderSetting mode;
    private final Minecraft mc = Minecraft.getMinecraft();
    public Vector3d impulseVector;
    public Vector3d forceVector;
    public Vector3d velocityVector;
    public Vector3d gravityVector;
    public double gravityMul = 2d;
    public final double mass = 1;
    private final double delta = 1 / 30d;
    private double jumpfunny = 0;
    private long jumpticks = System.currentTimeMillis();
    private int groundTicks;

    public ServerHelper() {
        super("Helper", Module.category.other, 0);
        registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.impulseVector = new Vector3d(0, 0, 0);
        this.forceVector = new Vector3d(0, 0, 0);
        this.velocityVector = new Vector3d(0, 0, 0);
        this.gravityVector = new Vector3d(0, -10, 0);
    }


    public Vector3d getMotionForTick() {
        // forces
        double massDiv = 1 / mass;
        this.forceVector.mul(massDiv);
        // gravity
        this.forceVector.add(this.gravityVector);
        this.forceVector.mul(this.gravityMul);

        // impulses
        this.impulseVector.mul(massDiv);
        this.forceVector.mul(this.delta);
        this.impulseVector.add(this.forceVector);
        // velocity
        this.velocityVector.add(this.impulseVector);

        this.forceVector.set(0, 0, 0);
        this.impulseVector.set(0, 0, 0);

        return this.velocityVector;
    }

    @Override
    public void onUpdate() {
        if (mc.thePlayer.onGround) {
            groundTicks++;
        } else {
            groundTicks = 0;
        }
    }


    @SubscribeEvent
    public void OnPreMotion(PreMotionEvent event) {
        if (mode.getInput() == 1) {
            if (mc.thePlayer.onGround && velocityVector.y < 0) {
               velocityVector.set(0, 0, 0);
            }

            if (event.posY == (double)0.42f) {
                jumpfunny = Math.min(jumpfunny + 1, 3);
                impulseVector.add(new Vector3d(0, 8, 0));
            }

            jumpfunny = groundTicks > 5 ? 0 : jumpfunny;
            double speed = jumpticks > System.currentTimeMillis() && Utils.getTimer().timerSpeed == 1 ? 1d : (mc.thePlayer.isUsingItem() ? 0.06d : 0.26d + 0.025d * jumpfunny);

            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                switch (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()) {
                    case 0:
                        speed += 0.14d;
                        break;
                    case 1:
                        speed += 0.14d;
                        break;
                }
            }

            Vector3d moveDirection = getMoveDirection(speed);
            if (mc.theWorld.isBlockLoaded(mc.thePlayer.getPosition()) || mc.thePlayer.posY <= 0) {
                gravityMul = 2.0;
                event.posX = moveDirection.x;
                event.posY = getMotionForTick().y * (1 / 30d);
                event.posZ = moveDirection.z;
            } else {
                event.posX = 0;
                event.posY = 0;
                event.posZ = 0;
            }
        }
    }

    private Vector3d getMoveDirection(double speed) {
        float forward = this.mc.thePlayer.movementInput.moveForward;
        float strafe = this.mc.thePlayer.movementInput.moveStrafe;
        float yaw = this.mc.thePlayer.rotationYaw;
        if (forward == 0.0f && strafe == 0.0f) {
            return new Vector3d(0.0, 0.0, 0.0);
        }
        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (strafe < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            strafe = 0.0f;
            forward = forward > 0.0f ? 1.0f : -1.0f;
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double x = (double)forward * speed * cos + (double)strafe * speed * sin;
        double z = (double)forward * speed * sin - (double)strafe * speed * cos;
        return new Vector3d(x, 0.0, z);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (mode.getInput() == 1) {
            if (ReceivePacketEvent.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) ReceivePacketEvent.getPacket();
                if (mc.thePlayer != null && packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    jumpticks = System.currentTimeMillis() + 1300;
                }
            } else if (ReceivePacketEvent.getPacket() instanceof S3FPacketCustomPayload) {
                S3FPacketCustomPayload packet = (S3FPacketCustomPayload) ReceivePacketEvent.getPacket();
                if ("bloxd:resyncphysics".equals(packet.getChannelName())) {
                    PacketBuffer data = packet.getBufferData();
                    jumpfunny = 0;
                    impulseVector.set(0, 0, 0);
                    forceVector.set(0, 0, 0);
                    velocityVector.set(data.readFloat(), data.readFloat(), data.readFloat());
                }
            }
        }}

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (mode.getInput() == 0) {
            event.setMoveFix(RotationHandler.MoveFix.Strict);
        }
    }

    @SubscribeEvent
    public void onPacketSend(SendPacketEvent event) {
        if (mode.getInput() == 0) {
            if (SendPacketEvent.getPacket() instanceof C03PacketPlayer) {
                sendC0CPacket();
            }
        }
    }

    private void sendC0CPacket() {
        if (mode.getInput() == 0) {
            mc.getNetHandler().addToSendQueue(new C0CPacketInput(mc.thePlayer.moveStrafing, mc.thePlayer.moveForward, mc.thePlayer.movementInput.jump, mc.thePlayer.movementInput.sneak));
        }
    }
}
