package neo.util.player.move;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;

public class PlayerData {
    public double speed;
    public EntityPlayer player;
    public boolean onGround;
    public int aboveVoidTicks;
    public int fastTick;
    public int autoBlockTicks;
    public int ticksExisted;
    public int lastSneakTick;
    public int sneakTicks;
    public int noSlowTicks;
    public double deltaX;
    public double deltaY;
    public double deltaZ;
    public double yaw = Double.NaN;
    public double prevYaw;
    public boolean sneaking;
    public double serverPosX;
    public double serverPosY;
    public double serverPosZ;
    public double motionY = Double.NaN;
    public double motionX = Double.NaN;
    public double motionZ = Double.NaN;
    public double lastMotionY;
    public double lastMotionX;
    public double lastMotionZ;
    public double airTicks;
    public double prediction = 0.9800000190734863;
    public int ticksNoMotionY;
    public int moveTicks;
    public int offGroundTicks;

    public void update(EntityPlayer entityPlayer) {
        player = entityPlayer;
        if (!Double.isNaN(this.motionX) && !Double.isNaN(this.motionY) && !Double.isNaN(this.motionZ)) {
            this.lastMotionX = this.motionX;
            this.lastMotionY = this.motionY;
            this.lastMotionZ = this.motionZ;
        }
        final int ticksExisted = entityPlayer.ticksExisted;
        if (!Double.isNaN(this.yaw)) {
            this.prevYaw = this.yaw;
        }
        if (MoveUtil.isMoving(entityPlayer)) {
            moveTicks++;
        } else {
            moveTicks = 0;
        }
        if (entityPlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }
        this.yaw = entityPlayer.rotationYaw;
        this.deltaX = entityPlayer.posX - entityPlayer.lastTickPosX;
        this.deltaY = entityPlayer.posY - entityPlayer.lastTickPosY;
        this.deltaZ = entityPlayer.posZ - entityPlayer.lastTickPosZ;
        this.motionX = entityPlayer.motionX;
        this.motionY = entityPlayer.motionY;
        this.motionZ = entityPlayer.motionX;
        this.prediction = 0.9800000190734863;
        onGround = entityPlayer.onGround;
        this.ticksNoMotionY = (this.motionY == 0) ? this.ticksNoMotionY + 1 : 0;

        if (entityPlayer.onGround) {
           airTicks = 0;
        } else {
            airTicks++;
        }

        this.speed = Math.max(Math.abs(this.deltaX), Math.abs(this.deltaZ));
        if (this.speed >= 0.07) {
            ++this.fastTick;
            this.ticksExisted = ticksExisted;
        }
        else {
            this.fastTick = 0;
        }
        if (Math.abs(this.deltaY) >= 0.1) {
            this.aboveVoidTicks = ticksExisted;
        }
        if (entityPlayer.isSneaking()) {
            this.lastSneakTick = ticksExisted;
        }
        if (entityPlayer.isSwingInProgress && entityPlayer.isBlocking()) {
            ++this.autoBlockTicks;
        }
        else {
            this.autoBlockTicks = 0;
        }
        if (entityPlayer.isSprinting() && entityPlayer.isUsingItem()) {
            ++this.noSlowTicks;
        }
        else {
            this.noSlowTicks = 0;
        }
        if (entityPlayer.rotationPitch >= 70.0f && entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().getItem() instanceof ItemBlock) {
            if (entityPlayer.swingProgressInt == 1) {
                if (!this.sneaking && entityPlayer.isSneaking()) {
                    ++this.sneakTicks;
                }
                else {
                    this.sneakTicks = 0;
                }
            }
        }
        else {
            this.sneakTicks = 0;
        }
    }

    public void updateSneak(final EntityPlayer entityPlayer) {
        this.sneaking = entityPlayer.isSneaking();
    }

    public void updateServerPos(EntityPlayer entityPlayer) {
        this.serverPosX = entityPlayer.serverPosX / 32;
        this.serverPosY = entityPlayer.serverPosY / 32;
        this.serverPosZ = entityPlayer.serverPosZ / 32;
    }
}
