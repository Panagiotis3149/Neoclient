package neo.module.impl.combat;

import neo.Neo;
import neo.event.*;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.other.RotationHandler;
import neo.util.other.java.Reflection;
import neo.util.packet.PacketUtils;
import neo.util.player.move.RotationUtils;
import neo.util.world.block.BlockUtils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.entity.Entity;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.util.EnumFacing.DOWN;
/**
 * @deprecated This class is replaced by {@link KillAura}
 */
// Well... Not yet...
// @Deprecated, Will be soon due to KillAura. Date of writing: 13/07/25 17:28
@Deprecated // Deprecated on 13/07/25 18:22. Only like an hour later LOL, This was after adding AutoBlock to KillAura, That will become the new Killaura
public class OldAura extends Module {
    public static EntityLivingBase target;

    private final SliderSetting aps;
    public SliderSetting autoBlockMode;
    private final SliderSetting fov;
    private final SliderSetting attackRange;
    private final SliderSetting swingRange;
    private final SliderSetting blockRange;
    private final SliderSetting rotationMode;
    private final SliderSetting sortMode;
    private final SliderSetting switchDelay;
    private final SliderSetting targets;
    private final ButtonSetting targetInvis;
    private final ButtonSetting disableInInventory;
    private final ButtonSetting disableWhileBlocking;
    private final ButtonSetting disableWhileMining;
    private final ButtonSetting hitThroughBlocks;
    private final ButtonSetting ignoreTeammates;
    public ButtonSetting manualBlock;
    private final SliderSetting moveFixMode;
    private final ButtonSetting prioritizeEnemies;
    private final ButtonSetting requireMouseDown;
    private final ButtonSetting silentSwing;
    private final ButtonSetting weaponOnly;
    private final ButtonSetting badPacketsCheck;
    private final String[] autoBlockModes = new String[]{"Manual", "Vanilla", "Post", "Swap", "Interact A", "Interact B", "Fake", "Partial", "Test"};
    private final String[] rotationModes = new String[]{"None", "Silent", "Lock view"};
    private final String[] sortModes = new String[]{"Health", "Hurttime", "Distance", "Yaw"};
    private List<EntityLivingBase> availableTargets = new ArrayList<>();
    public AtomicBoolean block = new AtomicBoolean();
    private long lastSwitched = System.currentTimeMillis();
    private boolean switchTargets;
    private byte entityIndex;
    public boolean swing;
    // autoclicker vars
    private long i;
    private long j;
    private long k;
    private long l;
    private double m;
    private boolean n;
    private Random rand;
    private final double lastCps = 0;
    private long recalculateTime = 0;
    private final long DCPSRecalculateTickDelay = 200;
    // autoclicker vars end
    private boolean attack;
    private boolean blocking;
    public boolean blinking;
    public boolean lag;
    private boolean swapped;
    public boolean rmbDown;
    private float[] prevRotations;
    private boolean startSmoothing;
    private final ConcurrentLinkedQueue<Packet> blinkedPackets = new ConcurrentLinkedQueue<>();


    public OldAura() {
        super("Aura", category.combat);
        this.registerSetting(aps = new SliderSetting("CPS", 12.0, 1.0, 20.0, 0.5));
        this.registerSetting(autoBlockMode = new SliderSetting("Autoblock", autoBlockModes, 0));
        this.registerSetting(fov = new SliderSetting("Field of view", 360.0, 30.0, 360.0, 4.0));
        this.registerSetting(attackRange = new SliderSetting("Range (attack)", 3.0, 3.0, 6.0, 0.05));
        this.registerSetting(swingRange = new SliderSetting("Range (swing)", 3.3, 3.0, 8.0, 0.05));
        this.registerSetting(blockRange = new SliderSetting("Range (block)", 6.0, 3.0, 12.0, 0.05));
        this.registerSetting(rotationMode = new SliderSetting("Rotations", rotationModes, 0));
        this.registerSetting(moveFixMode = new SliderSetting("Move fix", RotationHandler.MoveFix.MODES, 0));
        this.registerSetting(sortMode = new SliderSetting("Target sorting modes", sortModes, 3.0));
        this.registerSetting(switchDelay = new SliderSetting("Delay (switch)", 200.0, 50.0, 1000.0, 25.0, "ms"));
        this.registerSetting(targets = new SliderSetting("Targets", 3.0, 1.0, 10.0, 1.0));
        this.registerSetting(targetInvis = new ButtonSetting("Target invis", true));
        this.registerSetting(disableInInventory = new ButtonSetting("Disable in inventory", true));
        this.registerSetting(disableWhileBlocking = new ButtonSetting("Disable while blocking", false));
        this.registerSetting(disableWhileMining = new ButtonSetting("Disable while mining", false));
        this.registerSetting(hitThroughBlocks = new ButtonSetting("Hit through blocks", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(manualBlock = new ButtonSetting("No AB", false));
        this.registerSetting(prioritizeEnemies = new ButtonSetting("Prioritize enemies", false));
        this.registerSetting(requireMouseDown = new ButtonSetting("Require mouse down", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing while blocking", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(badPacketsCheck = new ButtonSetting("BadPacketsCheck", true));
   //     this.registerSetting(mBX = new ButtonSetting("Miniblox", false));
    }

    public void onEnable() {
        this.rand = new Random();
    }

    public void onDisable() {
        reset();

    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRotation(@NotNull RotationEvent event) {
            event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFixMode.getInput()]);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.isntnull()) {
            return;
        }
        if (ev.phase != TickEvent.Phase.START) {
            return;
        }
        if (canAttack()) {
            attack = true;
        }
        if (target != null && rotationMode.getInput() == 2) {
            float[] rotations = RotationUtils.getRotations(target, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            if (rotations == null) return;

            float currentYaw = mc.thePlayer.rotationYaw;
            float currentPitch = mc.thePlayer.rotationPitch;
            float yawDiff = MathHelper.wrapAngleTo180_float(rotations[0] - currentYaw);
            float pitchDiff = rotations[1] - currentPitch;

            float accFactor = (float) (100 / 200.0);

            float yawStep = Math.abs(yawDiff) * accFactor;
            float pitchStep = Math.abs(pitchDiff) * accFactor;

            float newYaw = currentYaw + (yawDiff > 0 ? yawStep : -yawStep);
            float newPitch = currentPitch + (pitchDiff > 0 ? pitchStep : -pitchStep);
            newPitch = MathHelper.clamp_float(newPitch, -90, 90);

            mc.thePlayer.rotationYaw = newYaw;
            mc.thePlayer.rotationPitch = newPitch;
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (!basicCondition() || !settingCondition()) {
            reset();
            return;
        }

        block();

        if (ModuleManager.bedAura != null && ModuleManager.bedAura.isEnabled() && !ModuleManager.bedAura.allowAura.isToggled() && ModuleManager.bedAura.currentBlock != null) {
            resetBlinkState(true);
            return;
        }
        if ((mc.thePlayer.isBlocking() || block.get()) && disableWhileBlocking.isToggled()) {
            resetBlinkState(true);
            return;
        }
        boolean swingWhileBlocking = !silentSwing.isToggled() || !block.get();
        if (swing && attack) {
            if (swingWhileBlocking) {
                mc.thePlayer.swingItem();
            } else {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
        }
        if (block.get() && (autoBlockMode.getInput() == 3 || autoBlockMode.getInput() == 4 || autoBlockMode.getInput() == 5 || autoBlockMode.getInput() == 8)  && Utils.holdingSword()) {
            setBlockState(block.get(), false, false);
            if (ModuleManager.bedAura.stopAutoblock) {
                resetBlinkState(false);
                ModuleManager.bedAura.stopAutoblock = false;
                return;
            }
            if (target != null) {
                switch ((int) autoBlockMode.getInput()) {
                    case 3:
                        if (lag) {
                            blinking = true;
                            if (Neo.badPacketsHandler.playerSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(Neo.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem % 8 + 1));
                                swapped = true;
                            }
                            lag = false;
                        } else {
                            if (Neo.badPacketsHandler.delayAttack) {
                                return;
                            }
                            if (Neo.badPacketsHandler.playerSlot != mc.thePlayer.inventory.currentItem) {
                                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(Neo.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem));
                                swapped = false;
                            }
                            attackAndInteract(target, swingWhileBlocking, false);
                            sendBlock();
                            releasePackets();
                            lag = true;
                        }
                        break;
                    case 4:
                    case 5:
                        if (lag) {
                            blinking = true;
                            unBlock();
                            lag = false;
                        } else {
                            attackAndInteract(target, swingWhileBlocking, autoBlockMode.getInput() == 5); // attack while blinked
                            releasePackets(); // release
                            sendBlock(); // block after releasing unblock
                            lag = true;
                        }
                        break;
                    case 9:
                        if (lag) {
                            blinking = true;
                            unBlock();
                            lag = false;
                        }
                }
                return;
            }
        } else if (blinking || lag) {
            resetBlinkState(true);
        }
        if (target == null) {
            return;
        }
        if (attack) {
            resetBlinkState(true);
            attack = false;
            if (!aimingEntity()) {
                return;
            }
            switchTargets = true;
            Utils.attackEntity(target, swingWhileBlocking, !swingWhileBlocking);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        if (!basicCondition() || !settingCondition()) {
            reset();
            return;
        }
        setTarget(new float[]{e.getYaw(), e.getPitch()});
        if (target != null && rotationMode.getInput() == 1) {

            prevRotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch,
                    mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};

            float[] rotations = new float[0];
            if (target != null) {
                rotations = RotationUtils.getRotations(target, e.getYaw(), e.getPitch());
            }

            if (prevRotations == null || prevRotations.length < 2) {
                throw new IllegalArgumentException("prevRotations must not be null and must have at least 2 elements.");
            }
            float[] controlPoint1 = {prevRotations[0], prevRotations[1]};

            if (rotations == null) {
                throw new IllegalArgumentException("rotations must not be null.");
            }

            float[] controlPoint2 = {(rotations[0] + prevRotations[0]) / 2, (rotations[1] + prevRotations[1]) / 2};

            float t = (float) 0.7;
            float bezierX = (1 - t) * (1 - t) * controlPoint1[0] + 2 * (1 - t) * t * controlPoint2[0] + t * t * rotations[0];
            float bezierY = (1 - t) * (1 - t) * controlPoint1[1] + 2 * (1 - t) * t * controlPoint2[1] + t * t * rotations[1];

                if (!startSmoothing) {
                    prevRotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                    startSmoothing = true;

                float yawDiff = bezierX - prevRotations[0];
                float pitchDiff = bezierY - prevRotations[1];

                double yawStep = Utils.GCD(Math.abs(yawDiff), 1);
                double pitchStep = Utils.GCD(Math.abs(pitchDiff),1);


                float[] speed = new float[]{
                        (float) (yawDiff > 0 ? yawStep : -yawStep),
                        (float) (pitchDiff > 0 ? pitchStep : -pitchStep)
                };

                prevRotations[0] += speed[0];
                prevRotations[1] += speed[1];

                prevRotations[0] = MathHelper.wrapAngleTo180_float(prevRotations[0]);
                prevRotations[1] = MathHelper.wrapAngleTo180_float(prevRotations[1]);

                if (prevRotations[1] > 90) {
                    prevRotations[1] = 90;
                } else if (prevRotations[1] < -90) {
                    prevRotations[1] = -90;
                }

                e.setYaw(prevRotations[0]);
                e.setPitch(prevRotations[1]);
            } else {
                e.setYaw(rotations[0]);
                e.setPitch(rotations[1]);
            }
        }
 else {
            startSmoothing = false;
        }
        if (autoBlockMode.getInput() == 2 && block.get() && Utils.holdingSword()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }

        if (rotationMode.getInput() == 1 && prevRotations != null && prevRotations.length > 0 && target != null) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    prevRotations[0],
                    prevRotations[1],
                    mc.thePlayer.onGround
            ));
        }

    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent e) {
        if (autoBlockMode.getInput() == 2 && block.get() && Utils.holdingSword()) {
            sendBlock();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.isntnull() || !blinking) {
            return;
        }
        Packet packet = SendPacketEvent.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        if (packet instanceof C00PacketKeepAlive) {
            return;
        }
        blinkedPackets.add(SendPacketEvent.getPacket());
        e.setCanceled(true);
    }


    @SubscribeEvent
    public void onMouse(final MouseEvent mouseEvent) {
        if (mouseEvent.button == 0 && mouseEvent.buttonstate) {
            if (target != null || swing) {
                mouseEvent.setCanceled(true);
            }
        } else if (mouseEvent.button == 1) {
            rmbDown = mouseEvent.buttonstate;
            if (autoBlockMode.getInput() >= 1 && Utils.holdingSword() && block.get()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                if (target == null && mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entityHit != null && AntiBot.isBot(mc.objectMouseOver.entityHit)) {
                        return;
                    }
                    final BlockPos getBlockPos = mc.objectMouseOver.getBlockPos();
                    if (getBlockPos != null && (BlockUtils.check(getBlockPos, Blocks.chest) || BlockUtils.check(getBlockPos, Blocks.ender_chest))) {
                        return;
                    }
                }
                mouseEvent.setCanceled(true);
            }

        }
    }

    @Override
    public String getInfo() {
        return rotationModes[(int) rotationMode.getInput()];
    }

    private boolean aimingEntity() {
        if (rotationMode.getInput() > 0) {
            Object[] raycast = Reach.getEntity(attackRange.getInput(), 0, rotationMode.getInput() == 1 ? prevRotations : null);
            return raycast != null && raycast[0] == target;
        }
        return true;
    }

    private void reset() {
        target = null;
        availableTargets.clear();
        block.set(false);
        startSmoothing = false;
        swing = false;
        rmbDown = false;
        attack = false;
        this.i = 0L;
        this.j = 0L;
        block();
        resetBlinkState(true);
        swapped = false;
    }

    private void block() {
        if (!block.get() && !blocking) {
            return;
        }
        if (manualBlock.isToggled() && !rmbDown) {
            block.set(false);
        }
        if (!Utils.holdingSword()) {
            block.set(false);
        }
        switch ((int) autoBlockMode.getInput()) {
            case 0:
                setBlockState(false, false, true);
                break;
            case 1: // vanilla
                setBlockState(block.get(), true, true);
                break;
            case 2: // post
                setBlockState(block.get(), false, true);
                break;
            case 3: // interact
            case 4:
            case 5:
                setBlockState(block.get(), false, false);
                break;
            case 6: // fake
                setBlockState(block.get(), false, false);
                break;
            case 7: // partial
                boolean down = (target == null || target.hurtTime >= 5) && block.get();
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), down);
                Reflection.setButton(1, down);
                blocking = down;
            case 8: // test
                setBlockState(block.get(), false, false);
                break;
        }
    }

    private void setBlockState(boolean state, boolean sendBlock, boolean sendUnBlock) {
        if (Utils.holdingSword()) {
            if (sendBlock && !blocking && state && Utils.holdingSword() && !Neo.badPacketsHandler.C07) {
                sendBlock();
            } else if (sendUnBlock && blocking && !state) {
                unBlock();
            }
        }
        blocking = Reflection.setBlocking(state);
    }

    private void setTarget(float[] rotations) {
        availableTargets.clear();
        block.set(false);
        swing = false;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (availableTargets.size() > targets.getInput()) {
                continue;
            }
            if (entity == null) {
                continue;
            }
            if (entity == mc.thePlayer) {
                continue;
            }
            if (!(entity instanceof EntityLivingBase)) {
                continue;
            }
            if (entity instanceof EntityPlayer) {
                if (Utils.isFriended((EntityPlayer) entity)) {
                    continue;
                }
                if (((EntityPlayer) entity).deathTime != 0) {
                    continue;
                }
                if (AntiBot.isBot(entity) || (Utils.isTeamMate(entity) && ignoreTeammates.isToggled())) {
                    continue;
                }
            } else {
                continue;
            }
            if (entity.isInvisible() && !targetInvis.isToggled()) {
                continue;
            }
            if (!hitThroughBlocks.isToggled() && behindBlocks(rotations)) {
                continue;
            }
            final float n = (float) fov.getInput();
            if (n != 360.0f && !Utils.inFov(n, entity)) {
                continue;
            }
            double distance = mc.thePlayer.getDistanceSqToEntity(entity); // need a more accurate distance check
            if (distance <= blockRange.getInput() * blockRange.getInput() && autoBlockMode.getInput() > 0 && Utils.holdingSword()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                block.set(true);
            }
            if (distance <= swingRange.getInput() * swingRange.getInput()) {
                swing = true;
            }
            if (distance > attackRange.getInput() * attackRange.getInput()) {
                continue;
            }
            availableTargets.add((EntityLivingBase) entity);
        }
        if (Math.abs(System.currentTimeMillis() - lastSwitched) > switchDelay.getInput() && switchTargets) {
            switchTargets = false;
            if (entityIndex < availableTargets.size() - 1) {
                entityIndex++;
            } else {
                entityIndex = 0;
            }
            lastSwitched = System.currentTimeMillis();
        }
        if (!availableTargets.isEmpty()) {
            List<EntityLivingBase> enemies = new ArrayList<>();
            if (prioritizeEnemies.isToggled()) {
                for (EntityLivingBase entity : availableTargets) {
                    if (Utils.isEnemy((EntityPlayer) entity)) {
                        enemies.add(entity);
                    }
                }
                if (!enemies.isEmpty()) {
                    availableTargets = enemies;
                }
            }
            Comparator<EntityLivingBase> comparator = null;
            switch ((int) sortMode.getInput()) {
                case 0:
                    comparator = Comparator.comparingDouble(entityPlayer -> (double) entityPlayer.getHealth());
                    break;
                case 1:
                    comparator = Comparator.comparingDouble(entityPlayer2 -> (double) entityPlayer2.hurtTime);
                    break;
                case 2:
                    comparator = Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceSqToEntity(entity));
                    break;
                case 3:
                    comparator = Comparator.comparingDouble(entity2 -> RotationUtils.distanceFromYaw(entity2, false));
                    break;
            }
            Collections.sort(availableTargets, comparator);
            if (entityIndex > availableTargets.size() - 1) {
                entityIndex = 0;
            }
            target = availableTargets.get(entityIndex);
        } else {
            target = null;
        }
    }

    public static boolean basicCondition() {
        if (!Utils.isntnull()) {
            return false;
        }
        return !mc.thePlayer.isDead;
    }

    private boolean settingCondition() {
        if (!Mouse.isButtonDown(0) && requireMouseDown.isToggled()) {
            return false;
        } else if (!Utils.holdingWeapon() && weaponOnly.isToggled()) {
            return false;
        } else if (isMining() && disableWhileMining.isToggled()) {
            return false;
        } else return mc.currentScreen == null || !disableInInventory.isToggled();
    }

    private void attackAndInteract(EntityLivingBase target, boolean swingWhileBlocking, boolean predict) {
        if (target != null && attack) {
            attack = false;
            if (!aimingEntity()) {
                return;
            }
            if (predict && target.hurtResistantTime > 16) {
                return;
            }
            switchTargets = true;
            if (target != null) {
                if (rotationMode.getInput() == 1) {
                    // Broken as of: 11/20/2024
                    // Fixed: No. Not yet.
                    Utils.attackEntityV2(target, false, prevRotations[0], prevRotations[1], silentSwing.isToggled());
                } else {
                    Utils.attackEntity(target, !swing && swingWhileBlocking, !swingWhileBlocking);
                }
                mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
            }
        } else if (ModuleManager.antiFireball != null && ModuleManager.antiFireball.isEnabled() && ModuleManager.antiFireball.fireball != null && ModuleManager.antiFireball.attack) {
            Utils.attackEntity(ModuleManager.antiFireball.fireball, !ModuleManager.antiFireball.silentSwing.isToggled(), ModuleManager.antiFireball.silentSwing.isToggled());
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(ModuleManager.antiFireball.fireball, C02PacketUseEntity.Action.INTERACT));
        }
    }

    private boolean isMining() {
        return Mouse.isButtonDown(0) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private boolean canAttack() {
        if (this.j > 0L && this.i > 0L) {
            if (System.currentTimeMillis() > this.j) {
                this.cookie();
                return true;
            } else if (System.currentTimeMillis() > this.i) {
                return false;
            }
        } else {
            this.cookie();
        }
        return false;
    }

    private long outlierStartTime;
    private boolean isOutlierActive;
    private long outlierDuration;
    private double previousCps;
    private final List<Double> lastCpsValues = new ArrayList<>();
    private static final int HISTORY_SIZE = 32;

    public void cookie() {
        double apsValue = aps.getInput();
        double minCps = Math.max(1, apsValue - 2);
        double maxCps = apsValue + 1;

        double randomCps = minCps + (Math.random() * (maxCps - minCps));
        long targetDelay = (long) (1000.0D / randomCps);


        if (System.currentTimeMillis() > this.recalculateTime) {
            while (lastCpsValues.contains(randomCps) || Math.abs(randomCps - previousCps) < 0.01) {
                randomCps = minCps + (Math.random() * (maxCps - minCps));
            }

            if (lastCpsValues.size() >= HISTORY_SIZE) {
                lastCpsValues.remove(0);
            }
            lastCpsValues.add(randomCps);
            previousCps = randomCps;
            this.recalculateTime = System.currentTimeMillis() + DCPSRecalculateTickDelay;
        } else {
            randomCps = previousCps;
        }

        long variability = (long) (Math.random() * 50);
        targetDelay += variability;

        if (System.currentTimeMillis() > this.k) {
            if (!this.n && this.rand.nextInt(100) >= 85) {
                this.n = true;
                this.m = 1.1D;
            } else {
                this.n = false;
            }
            this.k = System.currentTimeMillis() + 500L + this.rand.nextInt(1500);
        }

        if (this.n) {
            targetDelay = (long) ((double) targetDelay * this.m);
        }

        if (System.currentTimeMillis() > this.l) {
            if (this.rand.nextInt(100) >= 80) {
                targetDelay += 20L + this.rand.nextInt(50);
            }
            this.l = System.currentTimeMillis() + 500L + this.rand.nextInt(1500);
        }

        if (!isOutlierActive && this.rand.nextInt(100) < 5) {
            outlierStartTime = System.currentTimeMillis();
            outlierDuration = 2000 + this.rand.nextInt(500);
            targetDelay = (long) (targetDelay * 1.05);
            isOutlierActive = true;
        }

        if (isOutlierActive && System.currentTimeMillis() > outlierStartTime + outlierDuration) {
            isOutlierActive = false;
        }

        long currentDelay = this.j - System.currentTimeMillis();
        long d = Math.max(targetDelay, currentDelay);

        this.j = System.currentTimeMillis() + d;
        this.i = System.currentTimeMillis() + d / 2L - this.rand.nextInt(5);
    }

    private void unBlock() {
        if (!Utils.holdingSword()) {
            return;
        }
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, DOWN));
    }

    public void resetBlinkState(boolean unblock) {
        releasePackets();
        blocking = false;
        if (Neo.badPacketsHandler.playerSlot != mc.thePlayer.inventory.currentItem && swapped) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            Neo.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem;
            swapped = false;
        }
        if (lag && unblock) {
            unBlock();
        }
        lag = false;
    }

    private void releasePackets() {
        try {
            synchronized (blinkedPackets) {
                for (Packet packet : blinkedPackets) {
                    if (packet instanceof C09PacketHeldItemChange) {
                        Neo.badPacketsHandler.playerSlot = ((C09PacketHeldItemChange) packet).getSlotId();
                    }
                    PacketUtils.sendPacketNoEvent(packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendModuleMessage(this, "&cThere was an ewwor releasing blinked packets");
        }
        blinkedPackets.clear();
        blinking = false;
    }

    private boolean behindBlocks(float[] rotations) {
        switch ((int) rotationMode.getInput()) {
            case 0:
            case 2:
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos p = mc.objectMouseOver.getBlockPos();
                    if (p != null && mc.theWorld.getBlockState(p).getBlock() != Blocks.air) {
                        return true;
                    }
                }
                break;
            case 1:
                    return RotationUtils.rayCast(attackRange.getInput(), prevRotations != null ? prevRotations[0] : mc.thePlayer.rotationYaw, prevRotations != null ? prevRotations[1] : mc.thePlayer.rotationPitch) != null;
        }
        return false;
    }

    private void sendBlock() {
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }
}
