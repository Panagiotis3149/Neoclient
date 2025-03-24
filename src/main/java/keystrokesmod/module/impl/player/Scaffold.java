package keystrokesmod.module.impl.player;

import keystrokesmod.event.*;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.*;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
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
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;

public class Scaffold extends Module {
    private int ticks = 0;
    private SliderSetting motion;
    private SliderSetting rotation;
    private SliderSetting fastScaffold;
    private SliderSetting precision;
    private SliderSetting multiPlace;
    private ButtonSetting autoSwap;
    private ButtonSetting fastOnRMB;
    private ButtonSetting highlightBlocks;
    public ButtonSetting safeWalk;
    private ButtonSetting showBlockCount;
    private ButtonSetting delayOnJump;
    private ButtonSetting silentSwing;
    private ButtonSetting bypass;
    public ButtonSetting tower;
    private MovingObjectPosition placeBlock;
    private final ButtonSetting moveFix;
    private int lastSlot;
    private String[] rotationModes = new String[]{"None", "Simple", "Strict", "Precise"};
    private String[] fastScaffoldModes = new String[]{"Disabled", "Sprint", "Edge", "Jump A", "Jump B", "Jump C", "KeepY", "Verus", "VerusFast", "Legit (T)"};
    private String[] precisionModes = new String[]{"Very low", "Low", "Moderate", "High", "Very high"};
    private String[] multiPlaceModes = new String[]{"Disabled", "1 extra", "2 extra"};
    public float placeYaw;
    public float placePitch;
    public int at;
    public int index;
    public boolean rmbDown;
    private double startPos = -1;
    private Map<BlockPos, Timer> highlight = new HashMap<>();
    private boolean forceStrict;
    private boolean down;
    private boolean delay;
    private boolean place;
    private int add;
    private boolean placedUp;
    private float previousRotation[];
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
    private EnumFacing[] facings = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP};
    private BlockPos[] offsets = {new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(0, -1, 0)};
    private keystrokesmod.event.ReceivePacketEvent ReceivePacketEvent;
    private SliderSetting theme;

    public Scaffold() {
        super("Scaffold", category.player);
        this.registerSetting(motion = new SliderSetting("Motion", 1.0, 0.5, 1.2, 0.01, "x"));
        this.registerSetting(rotation = new SliderSetting("Rotation", rotationModes, 1));
        this.registerSetting(fastScaffold = new SliderSetting("Fast scaffold", fastScaffoldModes, 0));
        this.registerSetting(precision = new SliderSetting("Precision", precisionModes, 4));
        this.registerSetting(multiPlace = new SliderSetting("Multi-place", multiPlaceModes, 0));
        this.registerSetting(theme = new SliderSetting("Theme (For Highlight)", Theme.themes, 0));
        this.registerSetting(autoSwap = new ButtonSetting("Auto Swap", false)); // Fixed (05/02/25)
        this.registerSetting(delayOnJump = new ButtonSetting("Delay on jump", true));
        this.registerSetting(fastOnRMB = new ButtonSetting("Fast on RMB", false));
        this.registerSetting(highlightBlocks = new ButtonSetting("Highlight blocks", true));
        this.registerSetting(safeWalk = new ButtonSetting("Safewalk", true));
        this.registerSetting(showBlockCount = new ButtonSetting("Show block count", true));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(tower = new ButtonSetting("Tower", false));
        this.registerSetting(bypass = new ButtonSetting("Cancel Sprint Packet", false));
        this.registerSetting(moveFix = new ButtonSetting("MoveFix", false));

    }

    public void onDisable() {
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
        blocksPlaced = 0;
        if (autoSwap.isToggled()) {
            if (lastSlot != -1) {
                mc.thePlayer.inventory.currentItem = lastSlot;
                lastSlot = -1;
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (bypass.isToggled() && e.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction actionPacket = (C0BPacketEntityAction) e.getPacket();
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
        if (fastScaffold.getInput() == 7) {
            Utils.verusTestSelfDamage();
        }

        if (autoSwap.isToggled()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null && !(heldItem.getItem() instanceof ItemBlock)) {
                lastSlot = mc.thePlayer.inventory.currentItem;
            }

            int bestSlot = -1;
            int maxStackSize = -1;

            // Iterate over the main inventory to find the best block slot
            for (int i = 0; i < 9; ++i) {
                ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
                if (itemStack != null && itemStack.getItem() instanceof ItemBlock) {
                    int stackSize = itemStack.stackSize;
                    // Only update if the stack size is greater than the current maxStackSize
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
                blockSlot = -1; // No valid block found
            }
        }


    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (rotation.getInput() > 0) {
            if (((rotation.getInput() == 2 && forceStrict) || rotation.getInput() == 3) && placeYaw != 2000) {
                event.setYaw(placeYaw);
                event.setPitch(placePitch);
            } else {
                event.setYaw(getYaw());
                event.setPitch(85);
            }
        }
        place = true;
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
        if (ticks > 40) ticks = 0;
        float yaw = RotationUtils.renderYaw;
        if (mc.thePlayer.isAirBorne) {
            offGroundTicks++;
        }
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
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


        if (fastScaffold.getInput() == 7 && !Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            bypass.enable(); // Enables Bypass (xd)
            mc.thePlayer.setSprinting(true);
            Utils.verusSSDisablerTest(ReceivePacketEvent, true);


            Utils.resetTimer();
            if (!MoveUtil.isMoving()) return;
            if (Utils.isMoving() && mc.thePlayer.onGround) {
                MoveUtil.strafe5(0.45);
                MoveUtil.jump(0.44f);
            }
            MoveUtil.strafe5(0.32);
        }

        if (fastScaffold.getInput() == 8) {
            bypass.enable(); // Enables Bypass (xd)
            mc.thePlayer.setSprinting(true);
            Utils.verusSSDisablerTest(ReceivePacketEvent, true);


            Utils.resetTimer();
            if (!MoveUtil.isMoving()) return;
            if (Utils.isMoving() && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0;
                MoveUtil.strafe5(0.5);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe5(((.06 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) + 0.1));
                }
            }
            MoveUtil.strafe5(0.33);
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
        double original = startPos;
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
                    // Update slot only if stackSize is higher than the current highest
                    if (itemStack.stackSize > highestStack) {
                        highestStack = itemStack.stackSize;
                        slot = i;
                    }
                }
            }

            // Handle case where no block is found
            if (slot == -1) {
                // Either log an error, notify the player, or handle the empty state gracefully
                return;
            }

            // Proceed with selected slot
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
        float searchPitch[] = new float[]{78, 12};
        double closestCombinedDistance = Double.MAX_VALUE;
        double offsetWeight = 0.2D;
        for (int i = 0; i < 2; i++) {
            if (i == 1 && rayCasted == null && Utils.overPlaceable(-1)) {
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
                if (!Utils.overPlaceable(-1)) {
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

                                        if ((forceStrict(checkYaw)) && i == 1) {
                                            forceStrict = true;
                                        } else {
                                            forceStrict = false;
                                        }
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

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck() || !showBlockCount.isToggled()) {
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
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) ((double) scaledResolution.getScaledWidth() / 2 - ((double) width / 2)), (float) ((double) scaledResolution.getScaledHeight() / 2 + 130), (float) width, 36, 6, true, Color.black);
            BlurUtils.blurEnd(2, 1F);
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
        if (!Utils.nullCheck() || !highlightBlocks.isToggled() || highlight.isEmpty()) {
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
        return this.isEnabled() && Utils.keysDown() && (fastScaffold.getInput() == 4 || fastScaffold.getInput() == 3 || fastScaffold.getInput() == 5 || fastScaffold.getInput() == 6 || fastScaffold.getInput() == 7 || fastScaffold.getInput() == 9) && (!Utils.jumpDown() || fastScaffold.getInput() == 6) && (!fastOnRMB.isToggled() || Mouse.isButtonDown(1));
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
        assert lastSlot >= -1 : "lastSlot should be -1 or greater";

        if (lastSlot == -1) {
            lastSlot = SlotHandler.getCurrentSlot();
        }

        int slot = SlotHandler.getCurrentSlot();

        assert autoSwap != null : "autoSwap should not be null";

        if (autoSwap.isToggled()) {
            ItemStack heldItem = SlotHandler.getHeldItem();

            assert heldItem != null : "heldItem should not be null when autoSwap is toggled";

            if (!(heldItem.getItem() instanceof ItemBlock)) {
                slot = SlotHandler.getCurrentSlot();
            } else {
                ItemBlock itemBlock = (ItemBlock) heldItem.getItem();
                assert Utils.canBePlaced(itemBlock) : "ItemBlock cannot be placed";
                if (!Utils.canBePlaced(itemBlock)) {
                    slot = SlotHandler.getCurrentSlot();
                }
            }
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
    public void onStrafe(PrePlayerInputEvent e) {
        if (fastScaffold.getInput() == 8) {
            if (mc.thePlayer.onGround) {
                MoveUtil.strafe5(.51);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe5(((.06 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) + 0.1));
                }
            } else if (mc.thePlayer.motionY < .77) {
                MoveUtil.strafe5(.3);
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe5(((.01 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) + 0.1));
                }
            }
        }
    }
}