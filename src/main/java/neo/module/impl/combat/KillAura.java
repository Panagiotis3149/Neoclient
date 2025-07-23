package neo.module.impl.combat;

import neo.Neo;
import neo.event.*;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.other.RotationHandler;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import neo.util.aim.QuantumAim;
import neo.util.aim.RandomUtil;
import neo.util.aim.Vec2;
import neo.util.aim.YawPitchHelper;
import neo.util.other.MathUtil;
import neo.util.other.java.Reflection;
import neo.util.packet.PacketUtils;
import neo.util.player.move.RotationAt;
import neo.util.player.move.RotationUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;


public class KillAura extends Module {
    // target
    public static EntityLivingBase target;

    // strings
    private final String[] autoBlockModes = new String[]{"Manual", "Vanilla", "Post", "Swap", "Interact A", "Interact B", "Fake", "Partial", "Test"};
    private final String[] rotationModes = {"None", "Normal", "Legit", "Lock View", "Modulo1", "Test", "Autistic Anticheat"};
    private final String[] clickerModes = {"Basic", "Normal", "Legit"};
    private final String[] sortModes = {"Health", "HurtTime", "Distance", "Yaw"};

    // settings
    // sliders
    private final SliderSetting aps;
    private final SliderSetting clickerMode;
    private final SliderSetting fov;
    private final SliderSetting reach;
    private final SliderSetting sortMode;
    private final SliderSetting switchDelay;
    private final SliderSetting targets;
    private final SliderSetting rotationMode;
    private final SliderSetting moveFixMode;
    public final SliderSetting autoBlockMode;
    // buttons
    private final ButtonSetting targetInvis;
    private final ButtonSetting disableInInventory;
    private final ButtonSetting disableWhileBlocking;
    private final ButtonSetting disableWhileMining;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting requireMouseDown;
    private final ButtonSetting weaponOnly;
    private final ButtonSetting silentSwing;
    public ButtonSetting manualBlock;


    // attacking and shit
    private List<EntityLivingBase> availableTargets = new ArrayList<>();
    private long lastSwitched = System.currentTimeMillis();
    private byte entityIndex;
    public boolean swing;
    private boolean attack;

    // clicker
    private long nextClick = 0L;
    private long midClick = 0L;
    private long condition = 0L;
    private long reset = 0L;
    private double mult = 0;
    private boolean multActive = false;
    private final Random rand = new Random();
    private long nextClickTimeBasic = 0L;

    private final SecureRandom secureRandom = new SecureRandom();
    private long startTime = System.currentTimeMillis();
    private long nextClickTime = 0L;
    private double lastCps = -1;
    private String lastCpsStr = "";

    // rots
    private Vec2 lastRotation = new Vec2(0, 0);
    public float[] rots;
    public float[] lastRots;
    private boolean backRotate;

    // blocking
    private final ConcurrentLinkedQueue<Packet> blinkedPackets = new ConcurrentLinkedQueue<>();
    public AtomicBoolean block = new AtomicBoolean();
    private boolean blocking;
    private boolean lag;
    private boolean swapped;
    public boolean rmbDown;
    public boolean blinking;
    private boolean switchTargets;


    public KillAura() {
        super("KillAura", category.combat, 19);
        this.registerSetting(aps = new SliderSetting("CPS", 12.0, 1.0, 20.0, 0.5));
        this.registerSetting(clickerMode = new SliderSetting("Clicker Mode", clickerModes, 1));
        this.registerSetting(fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 4.0));
        this.registerSetting(reach = new SliderSetting("Reach", 3.0, 3.0, 6.0, 0.05));
        this.registerSetting(autoBlockMode = new SliderSetting("Autoblock", autoBlockModes, 0));
        this.registerSetting(manualBlock = new ButtonSetting("Right Click Only", false));
        this.registerSetting(sortMode = new SliderSetting("Sorting", sortModes, 3));
        this.registerSetting(switchDelay = new SliderSetting("Switch Delay", 200.0, 50.0, 1000.0, 25.0, "ms"));
        this.registerSetting(rotationMode = new SliderSetting("Rotation Mode", rotationModes, 1));
        this.registerSetting(targets = new SliderSetting("Targets", 3.0, 1.0, 10.0, 1.0));
        this.registerSetting(moveFixMode = new SliderSetting("Move fix", RotationHandler.MoveFix.MODES, 0));
        this.registerSetting(targetInvis = new ButtonSetting("Target invis", true));
        this.registerSetting(disableInInventory = new ButtonSetting("Disable in inventory", true));
        this.registerSetting(disableWhileBlocking = new ButtonSetting("Disable while blocking", false));
        this.registerSetting(disableWhileMining = new ButtonSetting("Disable while mining", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(requireMouseDown = new ButtonSetting("Require mouse down", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
    }

    // events
    @Override
    public void onEnable() {
        resetTimers();
        if (mc.thePlayer != null) {
            this.rots = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
            this.lastRots = new float[]{mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch};
        }
    }

    @Override
    public void onDisable() {
        reset();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRotation(@NotNull RotationEvent event) {
        event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFixMode.getInput()]);
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (event.getNonStaticPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getNonStaticPacket();

            if (packet.getRotating()) {
                this.lastRotation = new Vec2(packet.getYaw(), packet.getPitch());
            }
        }
    }

    ;

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (!canRun()) {
            reset();
            return;
        }

        findTargets();

        if (target == null) {
            resetBlinkState(true);
            return;
        }

        if (canAttack()) attack = true;

        block();

        if (attack) {
            attack = false;
            swing = true;
            attackTarget(target);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(PreMotionEvent e) {
        if (!OldAura.basicCondition() || !settingCondition()) {
            reset();
            return;
        }
        doRotation(e);

        if (autoBlockMode.getInput() == 2 && block.get() && Utils.holdingSword()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }

        if (isSilent((int) rotationMode.getInput()) && target != null) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    e.getYaw(),
                    e.getPitch(),
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

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (e.button == 0 && e.buttonstate && (target != null || swing)) {
            e.setCanceled(true);
        }
    }

    // funcs and shit
    private boolean canRun() {
        if (!Utils.isnull()) return false;
        if (mc.thePlayer.isDead) return false;
        if (!Mouse.isButtonDown(0) && requireMouseDown.isToggled()) return false;
        if (!Utils.holdingWeapon() && weaponOnly.isToggled()) return false;
        if (isMining() && disableWhileMining.isToggled()) return false;
        if (mc.currentScreen != null && disableInInventory.isToggled()) return false;
        if ((mc.thePlayer.isBlocking() || block.get()) && disableWhileBlocking.isToggled()) return false;
        return true;
    }

    private boolean isMining() {
        return Mouse.isButtonDown(0) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }


    private void doRotation(PreMotionEvent e) {
        if (target == null) return;

        RotationAt rotationAt = RotationAt.values()[RandomUtil.nextInt(0, RotationAt.values().length - 1)];
        Vec2 newRotation;

        if (rotationMode.getInput() != 4 || rotationMode.getInput() != 5) {
            newRotation = RotationUtils.getRotations(target, rotationAt);
        } else {
            newRotation = RotationUtils.getRotations(target, rotationAt, true);  // parkinsons prevention
        }

        float sensitivity = mc.gameSettings.mouseSensitivity;
        float f = sensitivity * 0.6F + 0.2F;
        float f1 = f * f * f * 1.2F;


        float deltaYaw = MathHelper.wrapAngleTo180_float(newRotation.getX() - lastRotation.getX());
        float deltaPitch = newRotation.getY() - lastRotation.getY();


        switch ((int) rotationMode.getInput()) {
            case 0:
                // yes
                break;
            case 1:
                newRotation.setX(newRotation.getX() - (deltaYaw % f1));
                newRotation.setY(newRotation.getY() - (deltaPitch % f1));
                newRotation.setX(newRotation.getX() + RandomUtil.nextFloat(-1, 1));
                newRotation.setY(newRotation.getY() + RandomUtil.nextFloat(-0.5f, 0.5f));
                e.setYaw(newRotation.getX());
                e.setPitch(newRotation.getY());
                break;
            case 2:
                // uh oh...
                final SecureRandom secureRandom = new SecureRandom();
                float dY = RandomUtil.nextFloat((float) (11.091247 - 0.0010000000474974513), 24.17819923F) / 2.0f + secureRandom.nextFloat() + RandomUtil.nextFloat((float) (11.0912478 - 0.0010000000474974513), 24.12378123F) / 2.0f;
                float dP = RandomUtil.nextFloat((float) (12.1283044 - 0.0010000000474974513), 23.123802F) / 2.0f + secureRandom.nextFloat() + RandomUtil.nextFloat((float) (12.1283044 - 0.0010000000474974513), 23.123802F) / 2.0f;
                Vec3 best = QuantumAim.getBestHitVec(target, Utils.getTimer().renderPartialTicks);
                if (target != null) {
                    backRotate = true;
                    final double distance = mc.thePlayer.getDistanceToEntity(target);
                    if (distance < 0.4) {
                        dP = RandomUtil.nextFloat(0.0f, 4.0f) / 2.0f + RandomUtil.nextFloat(0.0f, 4.0f) / 2.0f;
                        dY = RandomUtil.nextFloat(0.0f, 4.0f) / 2.0f + RandomUtil.nextFloat(0.0f, 4.0f) / 2.0f;
                    }
                    if (this.rots == null || this.rots.length < 2) {
                        if (this.lastRots == null || this.lastRots.length < 2) {
                            this.rots = new float[]{0f, 0f};
                        } else {
                            this.rots = this.lastRots;
                        }
                    }
                    final float[] floats = QuantumAim.faceEntityCustom(target, dY, dP, this.rots[0], this.rots[1], "Doubled", true, true, false, (float) 1.9712f, best, false, true, true, true, true);
                    if (floats == null) {
                        this.rots = this.lastRots;
                        if (!ModuleManager.scaffold.isEnabled()) {
                            e.setYaw(this.rots[0]);
                            e.setPitch(this.rots[1]);
                        } else {
                            this.resetRotation();
                        }
                        this.lastRots = this.rots;
                        target = null;
                        return;
                    }
                    this.lastRots = this.rots;
                    this.rots = floats;
                    if (!ModuleManager.scaffold.isEnabled()) {
                        e.setYaw(this.rots[0]);
                        e.setPitch(this.rots[1]);
                    } else {
                        this.resetRotation();
                    }
                } else {
                    if (this.backRotate) {
                        if (this.rots[0] % 360.0f <= YawPitchHelper.realYaw % 360.0f + 20.0f && this.rots[0] % 360.0f > YawPitchHelper.realYaw % 360.0f - 20.0f && this.rots[1] % 360.0f <= YawPitchHelper.realPitch % 360.0f + 20.0f && this.rots[1] % 360.0f > YawPitchHelper.realPitch % 360.0f - 20.0f) {
                            this.backRotate = false;
                            this.resetRotation();
                        } else {
                            final float[] fa = QuantumAim.backRotate(dY, dP, this.rots[0], this.rots[1], YawPitchHelper.realYaw, YawPitchHelper.realPitch);
                            this.lastRots = this.rots;
                            this.rots = fa;
                            if (!ModuleManager.scaffold.isEnabled()) {
                                e.setYaw(this.rots[0]);
                                e.setPitch(this.rots[1]);

                            } else {
                                this.resetRotation();
                            }
                        }
                    } else {
                        this.resetRotation();
                    }
                }
                break;
            case 3:
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
                break;
            case 4:
                newRotation.setX(newRotation.getX() - ((newRotation.getX() % f1) - f));
                newRotation.setY(newRotation.getY() - ((newRotation.getY() % f1) - f));
                e.setYaw(newRotation.getX());
                e.setPitch(newRotation.getY());
                break;
            case 5:
                if (target == null) break;

                int deltaX = Math.round(deltaYaw / f1);
                int deltaY = Math.round(deltaPitch / f1);

                float smoothedF2 = (deltaX * f1) / 3f;
                float smoothedF3 = (deltaY * f1) / 3f;

                newRotation.setX((this.lastRotation.getX() + smoothedF2) + f1);
                newRotation.setY((this.lastRotation.getY() + smoothedF3) + f1);


                float[] rts = RotationUtils.getRotations(target, e.getYaw(), e.getPitch());
                if (rts == null) break;

                float t = 0.45f;

                float controlX = (rts[0] + newRotation.getX()) / 2f;
                float controlY = (rts[1] + newRotation.getY()) / 2f;

                float bzX = (1 - t) * (1 - t) * newRotation.getX() + 2 * (1 - t) * t * controlX + t * t * rts[0];
                float bzY = (1 - t) * (1 - t) * newRotation.getY() + 2 * (1 - t) * t * controlY + t * t * rts[1];

                float yawDif = bzY - newRotation.getX();
                float pitchDif = bzX - newRotation.getY();

                double yS = Utils.GCD(Math.abs(yawDif), 1);
                double pS = Utils.GCD(Math.abs(pitchDif), 1);

                float speedX = (float) (yawDif > 0 ? yS : -yS);
                float speedY = (float) (pitchDif > 0 ? pS : -pS);

                float newY = newRotation.getX() + speedX;
                float newP = newRotation.getY() + speedY;

                newYaw = MathHelper.wrapAngleTo180_float(newY);
                newPitch = MathHelper.clamp_float(newP, -90f, 90f);

                newRotation.setX(newYaw);
                newRotation.setY(newPitch);

                newRotation.setX(newRotation.getX() +
                        (float) (Math.sin(2f * Math.PI * 1f * ((System.currentTimeMillis() % 10000L) / 1000f) + 0f)
                                * (3f * Math.exp(-0.05f * ((System.currentTimeMillis() % 10000L) / 1000f))) + 0f)
                );

                newRotation.setY(newRotation.getY() +
                        (float) (Math.sin(2f * Math.PI * 1.5f * ((System.currentTimeMillis() % 10000L) / 1000f) + 1f)
                                * (1.5f * Math.exp(-0.05f * ((System.currentTimeMillis() % 10000L) / 1000f))) + 0f)
                );

                int deltaX2 = Math.round(newRotation.getX() / f1);
                int deltaY2 = Math.round(newRotation.getY() / f1);

                float smoothedF22 = (deltaX2 * f1) / 3f;
                float smoothedF32 = (deltaY2 * f1) / 3f;

                newRotation.setX((this.lastRotation.getX() + smoothedF22) + f1);
                newRotation.setY((this.lastRotation.getY() + smoothedF32) + f1);

                e.setYaw(newYaw);
                e.setPitch(newPitch);
                break;
            case 6:
                newRotation.setX((float) (newRotation.getX() * Math.random() + RandomUtil.nextFloat(-180f, 180f)));
                newRotation.setY((float) (newRotation.getY() * Math.random() + RandomUtil.nextFloat(-90f, 90f)));
                e.setYaw(newRotation.getX());
                e.setPitch(newRotation.getY());
                break;
        }
    }


    private void findTargets() {
        availableTargets.clear();
        block.set(false);
        swing = false;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (availableTargets.size() >= targets.getInput()) break;
            if (!(entity instanceof EntityLivingBase)) continue;
            if (entity == mc.thePlayer) continue;
            if (entity.isInvisible() && !targetInvis.isToggled()) continue;

            if (entity instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) entity;
                if (Utils.isFriended(p)) continue;
                if (p.deathTime != 0) continue;
                if (Utils.isTeamMate(p) && ignoreTeammates.isToggled()) continue;
                if (AntiBot.isBot(entity)) continue;
            } else continue;

            double dist = mc.thePlayer.getDistanceSqToEntity(entity);


            float n = (float) fov.getInput();
            if (n != 360.0f && !Utils.inFov(n, entity)) continue;

            if (dist <= reach.getInput() * reach.getInput() && autoBlockMode.getInput() > 0 && Utils.holdingSword()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                block.set(true);
            }

            if (dist <= reach.getInput() * reach.getInput()) {
                swing = true;
            }

            if (dist > reach.getInput() * reach.getInput()) continue;

            availableTargets.add((EntityLivingBase) entity);
        }

        if (availableTargets.isEmpty()) {
            target = null;
            return;
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
            target = availableTargets.get(entityIndex);
        } else {
            target = null;
        }
    }


    private boolean canAttack() {
        switch ((int) clickerMode.getInput()) {
            case 0:
                return canAttackBasic();
            case 1:
                return canAttackNormal();
            case 2:
                return canAttackLegit();
            default:
                return false;
        }
    }


    public boolean canAttackLegit() {
        long t = System.currentTimeMillis() - startTime;
        double warmup = Math.min(1, t / 15000.0);
        double exhaustion = t > 15000 ? Math.max(0, 1 - (t - 15000) / 30000.0) : 1;
        double s1 = Math.sin(t * 0.002 * 2 * Math.PI);
        double s2 = Math.sin(t * 0.005 * 2 * Math.PI + Math.PI / 4);
        double s3 = Math.sin(t * 0.01 * 2 * Math.PI + Math.PI / 2);
        double sineWave = 1 + 0.3 * s1 + 0.2 * s2 + 0.1 * s3;
        double randChaos = secureRandom.nextDouble() * secureRandom.nextDouble() / secureRandom.nextDouble();
        double cps = aps.getInput() * warmup * exhaustion * sineWave * randChaos;

        String cpsStr;
        do {
            String noise = String.format("%03d", (int)(Math.random() * 999));
            if (mc.thePlayer.hurtTime > 0 && secureRandom.nextFloat() > secureRandom.nextFloat()) {
                String drop = String.format("-%03d", (int)(Math.random() * 999));
                cpsStr = String.format("%.5f", cps) + drop + noise;
            } else {
                cpsStr = String.format("%.5f", cps) + noise;
            }
        } while (cpsStr.equals(lastCpsStr));
        lastCpsStr = cpsStr;


        double finalCps;
        if (cpsStr.contains("-")) {
            String[] parts = cpsStr.split("-");
            finalCps = Double.parseDouble(parts[0] + parts[1]);
            finalCps -= Math.random() * (Math.random() * 0.02);
        } else {
            finalCps = Double.parseDouble(cpsStr);
        }

        double safeAps = Double.parseDouble(whatv2(aps.getInput()) + what(String.valueOf((secureRandom.nextInt(9_999_999) + 1) / 10_000_000.0)) + what(String.valueOf(secureRandom.nextFloat())));
        double cappedCps = Math.min(MathUtil.avg(finalCps, MathUtil.avg(aps.getInput() + 1, randChaos + (sineWave * Math.max(Math.random() * 10, 1)) + (randChaos / 10))) * 1.781678314217, safeAps);
        Utils.sendRawMessage("CPS DEBUG: " + cappedCps);
        long delay = (long) (1000.0 / cappedCps);
        if (System.currentTimeMillis() >= nextClickTime) {
            nextClickTime = System.currentTimeMillis() + delay;
            return true;
        }
        return false;
    }

    static String what(String s) {
        return s.replaceFirst(".*?\\.", "").replaceAll("\\.", "");
    }

    static String whatv2(double d) {
        return String.valueOf(d).replaceFirst("\\.0$", ".");
    }



    private boolean canAttackBasic() {
        if (System.currentTimeMillis() < nextClickTimeBasic) return false;
        double cps = aps.getInput();
        double min = Math.max(1, cps - 2);
        double max = cps + 1;
        double randomCps = min + (rand.nextDouble() * (max - min));
        long delay = (long) (1000.0 / randomCps);
        delay += rand.nextInt(50);
        nextClickTimeBasic = System.currentTimeMillis() + delay;
        return true;
    }

    private boolean canAttackNormal() {
        if (nextClick > 0L && midClick > 0L) {
            if (System.currentTimeMillis() > nextClick) {
                normalClick();
                return true;
            } else if (System.currentTimeMillis() > midClick) {
                return false;
            }
        } else {
            normalClick();
        }
        return false;
    }


    private void normalClick() {
        double apsValue = aps.getInput();
        double minCps = Math.max(1, apsValue - 2);
        double maxCps = apsValue + 1;

        double randomCps = minCps + (rand.nextDouble() * (maxCps - minCps));
        long targetDelay = (long) (1000.0D / randomCps);

        long currentTime = System.currentTimeMillis();

        if (currentTime > condition) {
            if (!multActive && rand.nextInt(100) >= 85) {
                multActive = true;
                mult = 1.1D;
            } else {
                multActive = false;
            }
            condition = currentTime + 500L + rand.nextInt(1500);
        }

        if (multActive) {
            targetDelay = (long) (targetDelay * mult);
        }

        if (currentTime > reset) {
            if (rand.nextInt(100) >= 80) {
                targetDelay += 20L + rand.nextInt(50);
            }
            reset = currentTime + 500L + rand.nextInt(1500);
        }

        midClick = currentTime + targetDelay / 2L - rand.nextInt(5);
        nextClick = currentTime + targetDelay;
    }

    private void attackTarget(EntityLivingBase t) {
        if (t == null) return;

        Utils.attackEntity(t, !silentSwing.isToggled(), silentSwing.isToggled());
        if (!silentSwing.isToggled()) mc.thePlayer.swingItem();
        else mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
    }

    private void attackTargetBlock(EntityLivingBase target, boolean swingWhileBlocking) {
        if (target == null) return;

        Utils.attackEntity(target, !swing && swingWhileBlocking, !swingWhileBlocking);

        if (!silentSwing.isToggled()) {
            mc.thePlayer.swingItem();
        } else {
            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }

        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
    }


    private void resetTimers() {
        nextClick = 0L;
        midClick = 0L;
        condition = 0L;
        reset = 0L;
        multActive = false;
        nextClickTimeBasic = 0L;
    }

    private void reset() {
        target = null;
        availableTargets.clear();
        block.set(false);
        swing = false;
        rmbDown = false;
        attack = false;
        nextClick = 0L;
        midClick = 0L;
        nextClickTimeBasic = 0L;

        mc.thePlayer.stopUsingItem();
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));

        resetBlinkState(true);
        swapped = false;

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


    private void resetRotation() {
        (this.rots = this.lastRots)[0] = YawPitchHelper.realYaw;
        this.rots[1] = YawPitchHelper.realPitch;
        this.lastRots[0] = YawPitchHelper.realLastYaw;
        this.lastRots[1] = YawPitchHelper.realLastPitch;
    }

    private boolean isSilent(int mode) {
        ;
        boolean inputValid = IntStream.of(1, 2, 4, 5, 6).anyMatch(i -> i == mode);


        return this.isEnabled() && Utils.keysDown() && inputValid;
    }

    // BLOCKING TIME.
    private void block() {
        if (!block.get() && !blocking) {
            resetBlinkState(true);
            return;
        }

        if (manualBlock.isToggled() && !rmbDown) {
            block.set(false);
            resetBlinkState(true);
            return;
        }

        if (!Utils.holdingSword()) {
            block.set(false);
            resetBlinkState(true);
            return;
        }

        switch ((int) autoBlockMode.getInput()) {
            case 0:
                setBlockState(false, false, true);
                break;
            case 1:
                setBlockState(block.get(), true, true);
                break;
            case 2:
                setBlockState(block.get(), false, true);
                break;
            case 3:
            case 4:
            case 5:
            case 8:
                if (target != null) {
                    if (lag) {
                        blinking = true;
                        unBlock();
                        lag = false;
                    } else {
                        attackTargetBlock(target, false);
                        sendBlock();
                        releasePackets();
                        lag = true;
                    }
                }
                setBlockState(block.get(), false, false);
                break;
            case 6:
                setBlockState(block.get(), false, false);
                break;
            case 7:
                boolean down = (target == null || target.hurtTime >= 5) && block.get();
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), down);
                Reflection.setButton(1, down);
                blocking = down;
                break;
            default:
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

    private void sendBlock() {
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }

    private void unBlock() {
        if (!Utils.holdingSword()) return;
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
    }

    public void resetBlinkState(boolean unblock) {
        releasePackets();
        blocking = false;
        if (Neo.badPacketsHandler.playerSlot != mc.thePlayer.inventory.currentItem && swapped) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            Neo.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem;
            swapped = false;
        }
        if (lag && unblock) unBlock();
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

    // END BLOCKING TIME.
}
