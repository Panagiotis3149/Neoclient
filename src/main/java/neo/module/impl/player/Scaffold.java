package neo.module.impl.player;

import neo.event.*;
import neo.module.Module;
import neo.module.impl.other.RotationHandler;
import neo.module.impl.other.SlotHandler;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.*;
import neo.util.aim.QuantumAim;
import neo.util.aim.YawPitchHelper;
import neo.util.other.MathUtil;
import neo.util.packet.PacketUtils;
import neo.util.player.move.RotationUtils;
import neo.util.render.Theme;
import neo.util.render.animation.Timer;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.player.move.MoveUtil;
import neo.util.render.RenderUtils;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import neo.util.world.block.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class Scaffold extends Module {
    private int ticks = 0;
    private int unlticks = 0;
    private final SliderSetting motion;
    private final SliderSetting rotation;
    private final SliderSetting fastScaffold;
    private final SliderSetting precision;
    private final SliderSetting multiPlace;
    private final ButtonSetting autoSwap;
    private final ButtonSetting fastOnRMB;
    private final SliderSetting tower;
    private final ButtonSetting highlightBlocks;
    public ButtonSetting safeWalk;
    private final ButtonSetting showBlockCount;
    private final ButtonSetting delayOnJump;
    private final ButtonSetting silentSwing;
    private final ButtonSetting bypass;
    private MovingObjectPosition placeBlock;
    private final ButtonSetting moveFix;
    private int lastSlot;
    private final String[] rotationModes = new String[]{"None", "Simple", "Strict", "Precise", "Legit"};
    private final String[] fastScaffoldModes = new String[]{"Disabled", "Sprint", "Edge", "Jump A", "Jump B", "Jump C", "KeepY", "Verus", "VerusFast", "Legit (T)", "BMC"};
    private final String[] precisionModes = new String[]{"Very low", "Low", "Moderate", "High", "Very high"};
    private final String[] towerModes = new String[]{"None", "Vanilla", "NCP"};
    private final String[] multiPlaceModes = new String[]{"Disabled", "1 extra", "2 extra"};
    public float placeYaw;
    public float placePitch;
    public int at;
    public int index;
    public boolean rmbDown;
    private double startPos = -1;
    private final Map<BlockPos, Timer> highlight = new HashMap<>();
    private boolean forceStrict;
    private boolean down;
    private boolean delay;
    private boolean place;
    private int add;
    private int towerTick;
    private boolean placedUp;
    ButtonSetting shader ;
    private float[] previousRotation;
    private int blockSlot = -1;
    public int blocksPlaced;
    boolean rotated = false;
    int offGroundTicks = 0;
    private int StartY;
    public BlockPos posa;
    public int lastGroundY;
    BlockPos BlockPosBelow;
    BlockPos LastGroundBlockPosBelow;
    public BlockPos previousBlock;
    private final EnumFacing[] facings = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP};
    private final BlockPos[] offsets = {new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(0, -1, 0)};
    private ReceivePacketEvent ReceivePacketEvent;
    public static SliderSetting theme;
    private int is;
    private int floatTick;
    private boolean ground;
    private int onGroundTicks;
    double original = startPos;
    private boolean start = true;
    private MovingObjectPosition objectPosition;
    private float[] rots = new float[] { 0.0f, 0.0f };
    private double[] xyz = new double[3];
    private final HashMap<float[], MovingObjectPosition> hashMap = new HashMap<float[], MovingObjectPosition>();;

    public Scaffold() {
        super("Scaffold", category.player);
        this.registerSetting(motion = new SliderSetting("Motion", 1.0, 0.5, 1.2, 0.01, "x"));
        this.registerSetting(rotation = new SliderSetting("Rotation", rotationModes, 1));
        this.registerSetting(fastScaffold = new SliderSetting("Fast scaffold", fastScaffoldModes, 0));
        this.registerSetting(precision = new SliderSetting("Precision", precisionModes, 4));
        this.registerSetting(multiPlace = new SliderSetting("Multi-place", multiPlaceModes, 0));
        this.registerSetting(theme = new SliderSetting("Theme (For Highlight)", Theme.themes, 0));
        this.registerSetting(tower = new SliderSetting("Tower Mode", towerModes, 0));
        this.registerSetting(autoSwap = new ButtonSetting("Auto Swap", true)); // Fixed (05/02/25)
        this.registerSetting(delayOnJump = new ButtonSetting("Delay on jump", true));
        this.registerSetting(fastOnRMB = new ButtonSetting("Fast on RMB", false));
        this.registerSetting(highlightBlocks = new ButtonSetting("Highlight blocks", true));
        this.registerSetting(safeWalk = new ButtonSetting("Safewalk", true));
        this.registerSetting(showBlockCount = new ButtonSetting("Show block count", true));
        this.registerSetting(shader = new ButtonSetting("Block Counter Shaders", true));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(bypass = new ButtonSetting("Cancel Sprint Packet", false));
        this.registerSetting(moveFix = new ButtonSetting("MoveFix", false));
    }

    public void onDisable() {
        Utils.resetTimer();
        placeBlock = null;
        if (lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
            lastSlot = -1;
        }
        delay = false;
        highlight.clear();
        add = 0;
        at = index = 0;
        startPos = -1;
        forceStrict = false;
        down = false;
        place = false;
        placedUp = false;
        blockSlot = -1;
        ground = true;
        blocksPlaced = 0;
        ticks = 0;
        unlticks = 0;
        if (autoSwap.isToggled()) {
            if (lastSlot != -1) {
                mc.thePlayer.inventory.currentItem = lastSlot;
                lastSlot = -1;
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (bypass.isToggled() && SendPacketEvent.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction actionPacket = (C0BPacketEntityAction) SendPacketEvent.getPacket();
            if (actionPacket.getAction() == C0BPacketEntityAction.Action.START_SPRINTING ||
                    actionPacket.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                e.cancelEvent();
            }
        }
    }

    public int getY(BlockPos b) {
        BlockPos blockPos = b;

        int y = blockPos.getY();

        return y;
    }

    public void onUpdate() {
        posa = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
        start = ((mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0));
        if (mc.thePlayer.onGround) {
            lastGroundY = (int) Math.floor(mc.thePlayer.posY);
        }
        BlockPos pos = null;
        if (mc.thePlayer != null && mc.thePlayer.playerLocation != null) {
            BlockPosBelow = mc.thePlayer.playerLocation.add(0, -1, 0);
        } else {
            BlockPosBelow = posa;
        }
        if (mc.thePlayer.onGround) {
            if (mc.thePlayer.playerLocation != null) {
                LastGroundBlockPosBelow = mc.thePlayer.playerLocation.add(0, -1, 0);
            }
        }
    }

    public void onEnable() {
        lastSlot = -1;
        startPos = mc.thePlayer.posY;
        placePitch = 85;
        previousRotation = null;
        placeYaw = 2000;
        IBlockAccess blockAccess = mc.theWorld;
        BlockPos pos = new BlockPos(mc.thePlayer.posX, lastGroundY - 2, mc.thePlayer.posZ);
        if (!BlockUtils.getBlock(pos).isAir(blockAccess, pos)) {
            StartY = lastGroundY - 1;
        }
        if (autoSwap.isToggled()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null && !(heldItem.getItem() instanceof ItemBlock)) {
                lastSlot = mc.thePlayer.inventory.currentItem;
            }

            int bestSlot = -1;
            int maxStackSize = -1;

            for (int i = 0; i < 9; ++i) {
                ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
                if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                    int stackSize = itemStack.stackSize;
                    if (stackSize > maxStackSize) {
                        bestSlot = i;
                        maxStackSize = stackSize;
                    }
                }
            }

            if (bestSlot != -1) {
                mc.thePlayer.inventory.currentItem = bestSlot;
                blockSlot = bestSlot;
            } else {
                blockSlot = -1;
                if (lastSlot != -1) {
                    mc.thePlayer.inventory.currentItem = lastSlot;
                    lastSlot = -1;
                }
                if (mc.thePlayer.motionX != 0 || mc.thePlayer.motionZ != 0) {
                    MoveUtil.stop();
                }
            }
        }
    }


    public boolean canTower() {return totalBlocks() > 0 && mc.currentScreen == null && !MoveUtil.isMoving() && Utils.isnull() && Utils.jumpDown() && mc.thePlayer.hurtTime < 9;}


    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.isnull()) {
            return;
        }

        if (rotation.getInput() > 0) {
            if (rotation.getInput() == 4) {
                float[] floats = getNearestRotation();
                event.setYaw(floats[0]);
                event.setPitch(floats[1]);
            }
            if (((rotation.getInput() == 2 && forceStrict) || rotation.getInput() == 3) && placeYaw != 2000) {
                event.setYaw(placeYaw);
                event.setPitch(placePitch);
            } else {
                event.setYaw(getYaw());
                event.setPitch(85);
            }
        }

        place = true;

        if (canTower()) {
            switch ((int) tower.getInput()) {
                case 0:
                    break;
                case 1:
                    if (BlockUtils.blockNear(1)) {
                        mc.thePlayer.motionY = 0.42F;
                    }
                    break;
                case 2:
                    if (BlockUtils.blockNear(2)) {
                        PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(null));
                        if (mc.thePlayer.posY % 1 <= 0.00153598) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                            mc.thePlayer.motionY = 0.42F;
                        } else if (mc.thePlayer.posY % 1 < 0.1 && offGroundTicks != 0) {
                            mc.thePlayer.motionY = 0;
                            mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                        }
                    }
            }
        }
    }

    @SubscribeEvent
    public void onJump(JumpEvent e) {
        delay = true;
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (fastScaffold.getInput() == 9) {
            event.setMoveFix(RotationHandler.MoveFix.Strict);
        } else {
            event.setMoveFix(moveFix.isToggled() ? RotationHandler.MoveFix.Silent : RotationHandler.MoveFix.None);
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        ticks++;
        unlticks++;
        if (ticks > 40) ticks = 0;
        if (mc.thePlayer.isAirBorne) {
            offGroundTicks++;
            onGroundTicks = 0;
        }
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
            onGroundTicks++;
        }
        if (delay && delayOnJump.isToggled()) {
            delay = false;
            return;
        }

        final ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (!autoSwap.isToggled() || getSlot() == -1 || !(heldItem != null && heldItem.getItem() instanceof ItemBlock)) {
            return;
        }


        if (keepYPosition() && !down) {
            startPos = Math.floor(mc.thePlayer.posY);
            down = true;
        } else if (!keepYPosition() || Math.floor(mc.thePlayer.posY) < startPos) {
            down = false;
            placedUp = false;
        }

        if (fastScaffold.getInput() == 10) {
            if (!(unlticks > 30) && MathUtil.isInAnyOffsetRange(ticks, 2, 6)) {
                Utils.getTimer().timerSpeed = 1.2f;
            } else {
                Utils.resetTimer();
            }
            if (MoveUtil.isMoving()) {
                is++;
                if(ground && mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.42F;
                    MoveUtil.strafec(0.47);
                    ground = false;
                    return;
                }
                mc.thePlayer.setSprinting(true);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            } else {
                ground = true;
                is = 0;
                floatTick = 0;
                return;
            }
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !ground) {
                if (floatTick < -5) {
                    ground = true;
                    is = 0;
                    return;
                }
                if (onGroundTicks % 2 == 0 && floatTick > 0) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0522, mc.thePlayer.posZ);
                }
                MoveUtil.strafec(mc.thePlayer.isPotionActive(Potion.moveSpeed) ? is % 11 == 0 ? 0.146 : 0.296 : 0.20);
            }
        }

        if (fastScaffold.getInput() == 7 && !Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            bypass.enable(); // Enables Bypass (xd)
            mc.thePlayer.setSprinting(true);



            Utils.resetTimer();
            if (!MoveUtil.isMoving()) return;
            if (Utils.isMoving() && mc.thePlayer.onGround) {
                MoveUtil.strafe(0.45);
                MoveUtil.jump(0.44f);
            }
            MoveUtil.strafe(0.32);
        }

        if (fastScaffold.getInput() == 8) {
            bypass.enable(); // Enables Bypass (xd)
            mc.thePlayer.setSprinting(true);

            Utils.resetTimer();
            if (!MoveUtil.isMoving()) return;
            if (Utils.isMoving() && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0;
                MoveUtil.strafe(0.5);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe(((.06 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) + 0.1));
                }
            }
            MoveUtil.strafe(0.33);
        }

        if (fastScaffold.getInput() == 8) {
            if (Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(getYaw())) > 90) {
                mc.thePlayer.setSprinting(false);
            }
            if (!MoveUtil.isMoving()) return;
        }

        if (keepYPosition() && (fastScaffold.getInput() == 3 || fastScaffold.getInput() == 4 || fastScaffold.getInput() == 5) && mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            add = 0;
            if (Math.floor(mc.thePlayer.posY) == Math.floor(startPos) && fastScaffold.getInput() == 5) {
                placedUp = false;
            }
        }
        original = startPos;
        if (fastScaffold.getInput() == 3) {
            if (groundDistance() >= 2 && add == 0) {
                original++;
                add++;
            }
        } else if (fastScaffold.getInput() == 4 || fastScaffold.getInput() == 5) {
            if (groundDistance() > 0 && mc.thePlayer.posY - startPos < 1.5 && mc.thePlayer.fallDistance > 0 && ((!placedUp || Utils.isDiagonal()) || fastScaffold.getInput() == 4)) {
                original = mc.thePlayer.posY;
            }
        }


        if (mc.thePlayer.onGround && Utils.isMoving() && motion.getInput() != 1.0) {
            Utils.setSpeed(Utils.getHorizontalSpeed() * motion.getInput());
        }

        if (!autoSwap.isToggled() || getSlot() == -1 || !(heldItem != null && heldItem.getItem() instanceof ItemBlock)) {
            return;
        }

        if (this.autoSwap.isToggled()) {
            int slot = -1;
            int highestStack = -1;
            for (int i = 0; i < 9; ++i) {
                final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
                if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                    if (itemStack.stackSize > highestStack) {
                        highestStack = itemStack.stackSize;
                        slot = i;
                    }
                }
            }

            if (slot == -1) {
                if (lastSlot != -1) {
                    mc.thePlayer.inventory.currentItem = lastSlot;
                    lastSlot = -1;
                }
                MoveUtil.stop();
                return;
            }

            mc.thePlayer.inventory.currentItem = slot;
        }






        MovingObjectPosition rayCasted = null;
        float searchYaw = 35;
        switch ((int) precision.getInput()) {
            case 4:
                searchYaw = 90;
                break;
            case 3:
                searchYaw = 65;
                break;
            case 2:
                break;
            case 1:
                searchYaw = 20;
                break;
            case 0:
                searchYaw = 6;
                break;
        }

        PlaceData placeData = getBlockData(new BlockPos(mc.thePlayer.posX, keepYPosition() ? original - 1 : mc.thePlayer.posY - 1, mc.thePlayer.posZ));

        if (placeData == null || placeData.blockPos == null || placeData.enumFacing == null) {
            return;
        }

        float[] targetRotation = RotationUtils.getRotations(placeData.blockPos);
        float[] searchPitch = new float[]{78, 12};
        double closestCombinedDistance = Double.MAX_VALUE;
        double offsetWeight = 0.2D;
        for (int i = 0; i < 2; i++) {
            if (i == 1 && rayCasted == null && BlockUtils.overPlaceable(-1)) {
                searchYaw = 180;
                searchPitch = new float[]{65, 25};
            } else if (i == 1) {
                break;
            }
            float[] yawSearchList = generateSearchSequence(searchYaw);
            float[] pitchSearchList = generateSearchSequence(searchPitch[1]);
            for (float checkYaw : yawSearchList) {
                float playerYaw = getYaw();
                float fixedYaw = (float) (playerYaw + checkYaw + getRandom());
                if (!BlockUtils.overPlaceable(-1)) {
                    continue;
                }
                for (float checkPitch : pitchSearchList) {
                    float fixedPitch = RotationUtils.clampTo90((float) (targetRotation[1] + checkPitch + getRandom()));
                    MovingObjectPosition raycast = RotationUtils.rayCast(mc.playerController.getBlockReachDistance(), fixedYaw, fixedPitch);
                    if (raycast != null) {
                        if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            if (raycast.getBlockPos().equals(placeData.blockPos) && raycast.sideHit == placeData.getEnumFacing()) {
                                if (((ItemBlock) heldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, heldItem)) {
                                    double offSetX = raycast.hitVec.xCoord - raycast.getBlockPos().getX();
                                    double offSetY = raycast.hitVec.yCoord - raycast.getBlockPos().getY();
                                    double offSetZ = raycast.hitVec.zCoord - raycast.getBlockPos().getZ();

                                    double distanceToCenter = Math.abs(offSetX - 0.5f) + Math.abs(offSetY - 0.5f) + Math.abs(offSetZ - 0.5f);
                                    double distanceToPreviousRotation = previousRotation != null ? Math.abs(fixedYaw - previousRotation[0]) : 0;
                                    double combinedDistance = offsetWeight * distanceToCenter + distanceToPreviousRotation / 360;

                                    if (rayCasted == null || combinedDistance < closestCombinedDistance) {
                                        closestCombinedDistance = combinedDistance;
                                        rayCasted = raycast;
                                        placeYaw = fixedYaw;
                                        placePitch = fixedPitch;

                                        forceStrict = (forceStrict(checkYaw)) && i == 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (rayCasted != null) {
                break;
            }
        }
        if (rayCasted != null && (place || rotation.getInput() == 0)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            placeBlock = rayCasted;
            place(placeBlock, false);
            int input = (int) multiPlace.getInput();
            for (int i = 0; i < input; i++) {
                place(placeBlock, true);
            }
            place = false;
            if (placeBlock.sideHit == EnumFacing.UP && keepYPosition()) {
                placedUp = true;
            }
            previousBlock = placeData.blockPos.offset(placeData.getEnumFacing());
        }
    }

    private double distanceToLastPitch(final float pitch) {
        return Math.abs(pitch - this.rots[1]);
    }

    private boolean buildForward() {
        final float realYaw = MathHelper.wrapAngleTo180_float(YawPitchHelper.realYaw);
        return (realYaw > 77.5 && realYaw < 102.5) || (realYaw > 167.5 || realYaw < -167.0f) || (realYaw < -77.5 && realYaw > -102.5) || (realYaw > -12.5 && realYaw < 12.5);
    }

    private float[] getNearestRotation() {
        BlockPos blockPos = QuantumAim.getAimBlockPos();
        objectPosition = null;
        final float[] floats = rots;
        final BlockPos b = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ);
        hashMap.clear();
        if (start) {
            final float yaw = QuantumAim.rotateToYaw((float)11.091247, rots[0], YawPitchHelper.realYaw - 180.0f);
            QuantumAim.mouseSens(yaw, 80.34f, rots[0], rots[1]);
            floats[1] = 80.34f;
            floats[0] = yaw;
        }
        else {
            final float yaww = YawPitchHelper.realYaw - 180.0f;
            floats[0] = yaww;
            double x = mc.thePlayer.posX;
            double z = mc.thePlayer.posZ;
            final double add1 = 1.288;
            final double add2 = 0.288;
            if (!buildForward()) {
                x += mc.thePlayer.posX - xyz[0];
                z += mc.thePlayer.posZ - xyz[2];
            }
            xyz = new double[] { mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ };
            if (blockPos == null) {
                blockPos = new BlockPos(b);
            }
            final double maX = blockPos.getX() + add1;
            final double miX = blockPos.getX() - add2;
            final double maZ = blockPos.getZ() + add1;
            final double miZ = blockPos.getZ() - add2;
            if (x > maX || x < miX || z > maZ || z < miZ) {
                final ArrayList<MovingObjectPosition> movingObjectPositions = new ArrayList<MovingObjectPosition>();
                final ArrayList<Float> pitchs = new ArrayList<Float>();
                for (float i = Math.max(rots[1] - 20.0f, -90.0f); i < Math.min(rots[1] + 20.0f, 90.0f); i += 0.05f) {
                    final float[] f = QuantumAim.mouseSens(yaww, i, rots[0], rots[1]);
                    final MovingObjectPosition m2 = QuantumAim.customRayTrace(mc.thePlayer, 4.5, 1.0f, yaww, f[1]);
                    if (m2.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && BlockUtils.isOkBlock(m2.getBlockPos()) && !movingObjectPositions.contains(m2) && m2.getBlockPos().equals(blockPos) && m2.sideHit != EnumFacing.DOWN && m2.sideHit != EnumFacing.UP && m2.getBlockPos().getY() <= b.getY()) {
                        movingObjectPositions.add(m2);
                        hashMap.put(f, m2);
                        pitchs.add(f[1]);
                    }
                }
                movingObjectPositions.sort(Comparator.comparingDouble(m -> mc.thePlayer.getDistanceSq(m.getBlockPos().add(0.5, 0.5, 0.5))));
                MovingObjectPosition mm = null;
                if (movingObjectPositions.size() > 0) {
                    mm = movingObjectPositions.get(0);
                }
                if (mm != null) {
                    floats[0] = yaww;
                    pitchs.sort(Comparator.comparingDouble((ToDoubleFunction<? super Float>)this::distanceToLastPitch));
                    if (!pitchs.isEmpty()) {
                        floats[1] = pitchs.get(0);
                        objectPosition = hashMap.get(floats);
                    }
                    return floats;
                }
            }
            else {
                floats[1] = rots[1];
            }
        }
        return floats;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.isnull() || !showBlockCount.isToggled()) {
            return;
        }
        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                return;
            }
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            int blocks = totalBlocks();
            String color = "§";
            if (blocks <= 5) {
                color += "c";
            } else if (blocks <= 15) {
                color += "6";
            } else if (blocks <= 25) {
                color += "e";
            } else {
                color = "";
            }
            FontRenderer font = FontManager.googleMedium20;
            int width = (int) Math.max(20, 14 + font.getStringWidth(color + blocks));
            if (shader.isToggled()) {
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound((float) ((double) scaledResolution.getScaledWidth() / 2 - ((double) width / 2)), (float) ((double) scaledResolution.getScaledHeight() / 2 + 130), (float) width, 36, 6, Color.black);
                BlurUtils.blurEnd(2, 1F);
            } else {
                RoundedUtils.drawRound((float) ((double) scaledResolution.getScaledWidth() / 2 - ((double) width / 2)), (float) ((double) scaledResolution.getScaledHeight() / 2 + 130), (float) width, 36, 6, new Color (0, 0, 0, 108));
            }
            RenderUtils.renderItemIcon((double) scaledResolution.getScaledWidth() / 2 - 8, (double) scaledResolution.getScaledHeight() / 2 + 132, mc.thePlayer.getHeldItem());
            font.drawString(color + blocks, (double) scaledResolution.getScaledWidth() / 2 - (font.getStringWidth(color + blocks) / 2), (double) scaledResolution.getScaledHeight() / 2 + 130 + 26, -1, false);
        }
    }

    @Override
    public String getInfo() {
        return fastScaffoldModes[(int) fastScaffold.getInput()];
    }

    public float[] generateSearchSequence(float value) {
        int length = (int) value * 2;
        float[] sequence = new float[length + 1];

        int index = 0;
        sequence[index++] = 0;

        for (int i = 1; i <= value; i++) {
            sequence[index++] = i;
            sequence[index++] = -i;
        }

        return sequence;
    }

    public PlaceData getBlockData(BlockPos pos) {
        for (int lastCheck = 0; lastCheck < 2; lastCheck++) {
            for (int i = 0; i < offsets.length; i++) {
                BlockPos newPos = pos.add(offsets[i]);
                Block block = BlockUtils.getBlock(newPos);
                if (newPos.equals(previousBlock)) {
                    return new PlaceData(facings[i], newPos);
                }
                if (lastCheck == 0) {
                    continue;
                }
                if (!block.getMaterial().isReplaceable() && !BlockUtils.isInteractable(block)) {
                    return new PlaceData(facings[i], newPos);
                }
            }
        }
        BlockPos[] additionalOffsets = { // adjust these for perfect placement
                pos.add(-1, 0, 0),
                pos.add(1, 0, 0),
                pos.add(0, 0, 1),
                pos.add(0, 0, -1),
                pos.add(0, -1, 0),
        };
        for (int lastCheck = 0; lastCheck < 2; lastCheck++) {
            for (BlockPos additionalPos : additionalOffsets) {
                for (int i = 0; i < offsets.length; i++) {
                    BlockPos newPos = additionalPos.add(offsets[i]);
                    Block block = BlockUtils.getBlock(newPos);
                    if (newPos.equals(previousBlock)) {
                        return new PlaceData(facings[i], newPos);
                    }
                    if (lastCheck == 0) {
                        continue;
                    }
                    if (!block.getMaterial().isReplaceable() && !BlockUtils.isInteractable(block) || newPos.equals(previousBlock)) {
                        return new PlaceData(facings[i], newPos);
                    }
                }
            }
        }
        BlockPos[] additionalOffsets2 = { // adjust these for perfect placement
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(0, 0, -1),
                new BlockPos(0, -1, 0),
        };
        for (int lastCheck = 0; lastCheck < 2; lastCheck++) {
            for (BlockPos additionalPos2 : additionalOffsets2) {
                for (BlockPos additionalPos : additionalOffsets) {
                    for (int i = 0; i < offsets.length; i++) {
                        BlockPos newPos = additionalPos2.add(additionalPos.add(offsets[i]));
                        Block block = BlockUtils.getBlock(newPos);
                        if (newPos.equals(previousBlock)) {
                            return new PlaceData(facings[i], newPos);
                        }
                        if (lastCheck == 0) {
                            continue;
                        }
                        if (!block.getMaterial().isReplaceable() && !BlockUtils.isInteractable(block) || newPos.equals(previousBlock)) {
                            return new PlaceData(facings[i], newPos);
                        }
                    }
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent mouseEvent) {
        if (mouseEvent.button == 1) {
            rmbDown = mouseEvent.buttonstate;
            if (placeBlock != null && rmbDown) {
                mouseEvent.setCanceled(true);
            }
        }
    }

    public boolean stopFastPlace() {
        return this.isEnabled() && placeBlock != null;
    }

    public double groundDistance() {
        for (int i = 1; i <= 20; i++) {
            if (!mc.thePlayer.onGround && !(BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - (i / 10), mc.thePlayer.posZ)) instanceof BlockAir)) {
                return (i / 10);
            }
        }
        return -1;
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.isnull() || !highlightBlocks.isToggled() || highlight.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BlockPos, Timer>> iterator = highlight.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Timer> entry = iterator.next();
            if (entry.getValue() == null) {
                entry.setValue(new Timer(750));
                entry.getValue().start();
            }
            int alpha = entry.getValue() == null ? 210 : 210 - entry.getValue().getValueInt(0, 210, 1);
            if (alpha == 0) {
                iterator.remove();
                continue;
            }
            RenderUtils.renderBlock(entry.getKey(), Utils.merge(Theme.getGradient((int)theme.getInput(),0), alpha), true, false);
        }
    }

    public boolean sprint() {
        if (this.isEnabled() && fastScaffold.getInput() > 0 && placeBlock != null && (!fastOnRMB.isToggled() || Mouse.isButtonDown(1))) {
            switch ((int) fastScaffold.getInput()) {
                case 1:
                    return true;
                case 2:
                    return Utils.onEdge(mc.thePlayer);
                case 3:
                case 4:
                case 5:
                case 6:
                    return keepYPosition();
            }
        }
        return false;
    }

    private boolean forceStrict(float value) {
        return (inBetween(-170, -105, value) || inBetween(-80, 80, value) || inBetween(98, 170, value)) && !inBetween(-10, 10, value);
    }

    private boolean keepYPosition() {
        int input = (int) fastScaffold.getInput();
        boolean inputValid = IntStream.of(3, 4, 5, 6, 7, 9, 10).anyMatch(i -> i == input);
        boolean jumpValid = !Utils.jumpDown() || input == 6;
        boolean rmbValid = !fastOnRMB.isToggled() || Mouse.isButtonDown(1);

        return this.isEnabled() && Utils.keysDown() && inputValid && jumpValid && rmbValid;
    }



    public boolean safewalk() {
        return this.isEnabled() && safeWalk.isToggled() && (!keepYPosition() || fastScaffold.getInput() == 3 || totalBlocks() == 0);
    }

    public boolean stopRotation() {
        return this.isEnabled() && (rotation.getInput() <= 1 || (rotation.getInput() == 2 && placeBlock != null));
    }

    private boolean inBetween(float min, float max, float value) {
        return value >= min && value <= max;
    }

    public float getYaw() {
        float yaw = 0.0f;
        double moveForward = mc.thePlayer.movementInput.moveForward;
        double moveStrafe = mc.thePlayer.movementInput.moveStrafe;
        if (moveForward == 0.0) {
            if (moveStrafe == 0.0) {
                yaw = 180.0f;
            } else if (moveStrafe > 0.0) {
                yaw = 90.0f;
            } else if (moveStrafe < 0.0) {
                yaw = -90.0f;
            }
        } else if (moveForward > 0.0) {
            if (moveStrafe == 0.0) {
                yaw = 180.0f;
            } else if (moveStrafe > 0.0) {
                yaw = 135.0f;
            } else if (moveStrafe < 0.0) {
                yaw = -135.0f;
            }
        } else if (moveForward < 0.0) {
            if (moveStrafe == 0.0) {
                yaw = 0.0f;
            } else if (moveStrafe > 0.0) {
                yaw = 45.0f;
            } else if (moveStrafe < 0.0) {
                yaw = -45.0f;
            }
        }
        return MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) + yaw;
    }

    private void place(MovingObjectPosition block, boolean extra) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            return;
        }
        if (!extra && mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, block.getBlockPos(), block.sideHit, block.hitVec)) {
            if (silentSwing.isToggled()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
                mc.getItemRenderer().resetEquippedProgress();
            }
            highlight.put(block.getBlockPos().offset(block.sideHit), null);
            previousRotation = new float[]{placeYaw, placePitch};
            if (heldItem.stackSize == 0) {
                blockSlot = -1;
            }
        } else if (extra) {
            float f = (float) (block.hitVec.xCoord - (double) block.getBlockPos().getX());
            float f1 = (float) (block.hitVec.yCoord - (double) block.getBlockPos().getY());
            float f2 = (float) (block.hitVec.zCoord - (double) block.getBlockPos().getZ());
            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(block.getBlockPos(), block.sideHit.getIndex(), heldItem, f, f1, f2));
            if (silentSwing.isToggled()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
                mc.getItemRenderer().resetEquippedProgress();
            }
        }
        blocksPlaced++;
    }

    public int getSlot() {
        if (lastSlot == -1) {
            lastSlot = SlotHandler.getCurrentSlot();
        }

        int slot = SlotHandler.getCurrentSlot();

        if (autoSwap != null && autoSwap.isToggled()) {
            ItemStack heldItem = SlotHandler.getHeldItem();

            if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock) || !Utils.canBePlaced((ItemBlock) heldItem.getItem())) {
                slot = SlotHandler.getCurrentSlot();
            }
        }

        if (slot == -1) {
            if (lastSlot != -1) {
                SlotHandler.setCurrentSlot(lastSlot);
                MoveUtil.stop();
            }
            return -1;
        }

        SlotHandler.setCurrentSlot(slot);
        return slot;
    }


    public int totalBlocks() {
        int totalBlocks = 0;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemBlock && Utils.canBePlaced((ItemBlock) stack.getItem()) && stack.stackSize > 0) {
                totalBlocks += stack.stackSize;
            }
        }
        return totalBlocks;
    }

    private double getRandom() {
        return Utils.randomizeInt(-40, 40) / 100.0;
    }

    static class PlaceData {
        EnumFacing enumFacing;
        BlockPos blockPos;

        PlaceData(EnumFacing enumFacing, BlockPos blockPos) {
            this.enumFacing = enumFacing;
            this.blockPos = blockPos;
        }

        EnumFacing getEnumFacing() {
            return enumFacing;
        }
    }



    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent e) {
        if (fastScaffold.getInput() == 8) {
            if (!MoveUtil.isMoving()) return;
            if (mc.thePlayer.onGround) {
                MoveUtil.strafe(.51);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe(((.06 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) + 0.1));
                }
            } else if (mc.thePlayer.motionY < .77) {
                MoveUtil.strafe(.3);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe(((.01 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) + 0.1));
                }
            }
        }
    }
}