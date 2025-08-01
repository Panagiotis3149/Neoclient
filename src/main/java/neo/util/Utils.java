package neo.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.client.Settings;
import neo.module.impl.combat.AutoClicker;
import neo.module.impl.minigames.DuelsStats;
import neo.module.impl.other.SlotHandler;
import neo.module.setting.impl.SliderSetting;
import neo.util.packet.PacketUtils;
import neo.util.player.CPSCalculator;
import neo.util.player.move.RotationUtils;
import neo.util.render.Theme;
import neo.util.world.block.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

import static neo.Variables.clientName;


public class Utils {
    private static final Random rand = new Random();
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static HashSet<String> friends = new HashSet<>();
    public static HashSet<String> enemies = new HashSet<>();
    public static final Logger log = LogManager.getLogger();

    public static String readStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append('\n');

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static double round3(double val) {
        return Math.round(val * 1000.0) / 1000.0;
    }

    public static double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }


    public static boolean addEnemy(String name) {
        if (enemies.add(name.toLowerCase())) {
            Utils.sendMessage("&7Added &cenemy&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean removeEnemy(String name) {
        if (enemies.remove(name.toLowerCase())) {
            Utils.sendMessage("&Removed &cenemy&7: &b" + name);
            return true;
        }
        return false;
    }

    public static String getServerName() {
        return DuelsStats.nick.isEmpty() ? mc.thePlayer.getName() : DuelsStats.nick;
    }

    public static boolean overVoid(double posX, double posY, double posZ) {
        for (int i = (int) posY; i > -1; i--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static boolean overVoid() {
        double playerPosY = mc.thePlayer.posY;
        for (int y = (int) playerPosY; y >= 0; y--) {
            BlockPos currentPos = new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ);
            Block blockAtPos = mc.theWorld.getBlockState(currentPos).getBlock();
            if (!(blockAtPos instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static List<NetworkPlayerInfo> getTablist() {
        final ArrayList<NetworkPlayerInfo> list = new ArrayList<>(mc.getNetHandler().getPlayerInfoMap());
        removeDuplicates(list);
        list.remove(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()));
        return list;
    }

    public static double getFallDistance(Entity entity) {
        double fallDist = -1;
        Vec3 pos = new Vec3(entity.posX, entity.posY, entity.posZ);
        int y = (int) Math.floor(pos.yCoord);
        if (pos.yCoord % 1 == 0) y--;
        for (int i = y; i > -1; i--) {
            Block block = BlockUtils.getBlock(new BlockPos((int) Math.floor(pos.xCoord), i, (int) Math.floor(pos.zCoord)));
            if (!(block instanceof BlockAir) && !(block instanceof BlockSign)) {
                fallDist = y - i;
                break;
            }
        }
        return fallDist;
    }

    public static void removeDuplicates(final ArrayList list) {
        final HashSet set = new HashSet(list);
        list.clear();
        list.addAll(set);
    }

    public static boolean removeFriend(String name) {
        if (friends.remove(name.toLowerCase())) {
            Utils.sendMessage("&7Removed &afriend&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean addFriend(String name) {
        if (friends.add(name.toLowerCase())) {
            Utils.sendMessage("&7Added &afriend&7: &b" + name);
            enemies.remove(name.toLowerCase());
            return true;
        }
        return false;
    }

    public static neo.script.classes.Vec3 getEyePos(Entity entity) {
        return getEyePos(entity, new neo.script.classes.Vec3(entity));
    }

    public static neo.script.classes.Vec3 getEyePos() {
        return getEyePos(mc.thePlayer);
    }

    public static neo.script.classes.Vec3 getEyePos(@NotNull Entity entity, neo.script.classes.@NotNull Vec3 position) {
        return position.add(new neo.script.classes.Vec3(0, entity.getEyeHeight(), 0));
    }

    public static boolean isWholeNumber(double num) {
        return num == Math.floor(num);
    }

    public static int randomizeInt(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    public static double randomizeDouble(double min, double max) {
        return org.apache.commons.lang3.RandomUtils.nextDouble(min, max);
    }

    public static boolean inFov(float fov, BlockPos blockPos) {
        return inFov(fov, blockPos.getX(), blockPos.getZ());
    }

    public static boolean inFov(float fov, Entity entity) {
        return inFov(fov, entity.posX, entity.posZ);
    }

    public static boolean inFov(float fov, final double n2, final double n3) {
        fov *= 0.5;
        final double wrapAngleTo180_double = MathHelper.wrapAngleTo180_double((mc.thePlayer.rotationYaw - RotationUtils.angle(n2, n3)) % 360.0f);
        if (wrapAngleTo180_double > 0.0) {
            return wrapAngleTo180_double < fov;
        } else return wrapAngleTo180_double > -fov;
    }

    public static void sendMessage(String txt) {
        if (isntnull()) {
            String msg = Theme.wrap(clientName) + " » §r" + txt;
            mc.thePlayer.addChatMessage(new ChatComponentText(formatColor(msg)));
        }
    }

    public static void sendDebugMessage(String message) {
        if (isntnull()) {
            String msg = Theme.wrap(clientName) + " » §r" + message;
            mc.thePlayer.addChatMessage(new ChatComponentText(formatColor(msg)));
        }
    }


    public static void attackEntityV2(Entity target, boolean sendLook, float yaw, float pitch, boolean silentSwing) {
        if (sendLook) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, mc.thePlayer.onGround));
        }

        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

        if (silentSwing) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        } else {
            mc.thePlayer.swingItem();
        }
    }


    public static void attackEntity(Entity e, boolean clientSwing, boolean silentSwing) {
        if (clientSwing) {
            mc.thePlayer.swingItem();
        } else if (silentSwing || (!silentSwing && !clientSwing)) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
        mc.playerController.attackEntity(mc.thePlayer, e);
    }

    public static void sendRawMessage(String txt) {
        if (isntnull()) {
            mc.thePlayer.addChatMessage(new ChatComponentText(formatColor(txt)));
        }
    }

    public static float getCompleteHealth(EntityLivingBase entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static String getHealthStr(EntityLivingBase entity) {
        float completeHealth = getCompleteHealth(entity);
        return getColorForHealth(entity.getHealth() / entity.getMaxHealth(), completeHealth);
    }

    public static int getTool(Block block) {
        float n = 1.0f;
        int n2 = -1;
        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null) {
                final float a = getEfficiency(getStackInSlot, block);
                if (a > n) {
                    n = a;
                    n2 = i;
                }
            }
        }
        return n2;
    }

    public static float getEfficiency(final ItemStack itemStack, final Block block) {
        float getStrVsBlock = itemStack.getStrVsBlock(block);
        if (getStrVsBlock > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0) {
                getStrVsBlock += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        return getStrVsBlock;
    }

    public static boolean isEnemy(EntityPlayer entityPlayer) {
        return !enemies.isEmpty() && enemies.contains(entityPlayer.getName().toLowerCase());
    }

    public static boolean isEnemy(String name) {
        return !enemies.isEmpty() && enemies.contains(name.toLowerCase());
    }

    public static String getColorForHealth(double n, double n2) {
        double health = rnd(n2, 1);
        return ((n < 0.3) ? "§c" : ((n < 0.5) ? "§6" : ((n < 0.7) ? "§e" : "§a"))) + (isWholeNumber(health) ? (int) health + "" : health);
    }

    public static int getColorForHealth(double health) {
        return ((health < 0.3) ? -43691 : ((health < 0.5) ? -22016 : ((health < 0.7) ? -171 : -11141291)));
    }

    public static String formatColor(String txt) {
        return txt.replaceAll("&", "§");
    }

    public static void correctValue(SliderSetting c, SliderSetting d) {
        if (c.getInput() > d.getInput()) {
            double p = c.getInput();
            c.setValue(d.getInput());
            d.setValue(p);
        }
    }

    public static String generateRandomString(final int n) {
        final char[] array = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        final StringBuilder sb = new StringBuilder();
        IntStream.range(0, n).forEach(p2 -> sb.append(array[rand.nextInt(array.length)]));
        return sb.toString();
    }

    public static boolean isFriended(EntityPlayer entityPlayer) {
        return !friends.isEmpty() && friends.contains(entityPlayer.getName().toLowerCase());
    }


    public static boolean onEdge(Entity entity) {
        return mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(entity.motionX / 3.0D, -1.0D, entity.motionZ / 3.0D)).isEmpty();
    }


    public static boolean isDiagonal() {
        float yaw = ((mc.thePlayer.rotationYaw % 360) + 360) % 360 > 180 ? ((mc.thePlayer.rotationYaw % 360) + 360) % 360 - 360 : ((mc.thePlayer.rotationYaw % 360) + 360) % 360;
        return (yaw >= -170 && yaw <= 170) && !(yaw >= -10 && yaw <= 10) && !(yaw >= 80 && yaw <= 100) && !(yaw >= -100 && yaw <= -80) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
    }


    public static boolean isFriended(String name) {
        return !friends.isEmpty() && friends.contains(name.toLowerCase());
    }

    public static double getRandomValue(SliderSetting a, SliderSetting b, Random r) {
        return a.getInput() == b.getInput() ? a.getInput() : a.getInput() + r.nextDouble() * (b.getInput() - a.getInput());
    }

    public static boolean isntnull() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean isHypixel() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("hypixel.net");
    }

    public static net.minecraft.util.Timer getTimer() {
        return ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
    }

    public static float n() {
        return ae(mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe);
    }

    public static String extractFileName(String name) {
        int firstIndex = name.indexOf("_");
        int lastIndex = name.lastIndexOf("_");

        if (firstIndex != -1 && lastIndex != -1 && lastIndex > firstIndex) {
            return name.substring(firstIndex + 1, lastIndex);
        } else {
            return name;
        }
    }

    public static int merge(int n, int n2) {
        return (n & 0xFFFFFF) | n2 << 24;
    }

    public static int merg(int rgb, int alpha) {
        return (rgb & 0xFFFFFF) | ((alpha & 0xFF) << 24);
    }


    public static int clamp(int n) {
        if (n > 255) {
            return 255;
        }
        if (n < 4) {
            return 4;
        }
        return n;
    }

    public static boolean isTeamMate(Entity entity) {
        try {
            Entity teamMate = entity;
            if (mc.thePlayer.isOnSameTeam((EntityLivingBase) entity) || mc.thePlayer.getDisplayName().getUnformattedText().startsWith(teamMate.getDisplayName().getUnformattedText().substring(0, 2))) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static void setSpeed(double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0.0;
            mc.thePlayer.motionX = 0.0;
            return;
        }
        float n3 = n();
        mc.thePlayer.motionX = -Math.sin(n3) * n;
        mc.thePlayer.motionZ = Math.cos(n3) * n;
    }


    private static final List<Packet> blinkedPackets = new ArrayList<>();

    public static void blinkPackets(SendPacketEvent e) {
        Packet packet = SendPacketEvent.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart || packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing || packet instanceof C01PacketEncryptionResponse || packet instanceof C00PacketKeepAlive || packet instanceof C0FPacketConfirmTransaction) {
            return;
        }
        blinkedPackets.add(packet);
    }

    public static void startBlink(Packet packet) {
        if (!packet.getClass().getSimpleName().startsWith("S")) {
            blinkedPackets.add(packet);
        }
    }

    public static void startBlink(Packet... packets) {
        for (Packet packet : packets) {
            if (!packet.getClass().getSimpleName().startsWith("S")) {
                blinkedPackets.add(packet);
            }
        }
    }


    public static void stopBlink() {
        synchronized (blinkedPackets) {
            for (Packet packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(packet);
            }
        }
        blinkedPackets.clear();
    }

    public static void clearBlinked() {
        blinkedPackets.clear();
    }



    public static double GCD(float a, float b) {
        while (b != 0) {
            float temp = b;
            b = a % b;
            a = temp;
        }
        return Math.abs(a);
    }



    public static void resetTimer() {
        try {
            getTimer().timerSpeed = 1.0F;
        } catch (NullPointerException var1) {
        }
    }

    public static boolean inInventory() {
        if (!Utils.isntnull()) {
            return false;
        }
        return (mc.currentScreen != null) && (mc.thePlayer.inventoryContainer != null) && (mc.thePlayer.inventoryContainer instanceof ContainerPlayer) && (mc.currentScreen instanceof GuiInventory);
    }

    public static int getSpeedAmplifier() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return 1 + mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
        }
        return 0;
    }

    public static int getBedwarsStatus() {
        if (!Utils.isntnull()) {
            return -1;
        }
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return -1;
        }
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null || !stripString(objective.getDisplayName()).contains("BED WARS")) {
            return -1;
        }
        for (String line : getSidebarLines()) {
            line = stripString(line);
            String[] parts = line.split("  ");
            if (parts.length > 1) {
                if (parts[1].startsWith("L")) {
                    return 0;
                }
            } else if (line.equals("Waiting...") || line.startsWith("Starting in")) {
                return 1;
            } else if (line.startsWith("R Red:") || line.startsWith("B Blue:")) {
                return 2;
            }
        }
        return -1;
    }

    public static String stripString(final String s) {
        final char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder validated = new StringBuilder();
        for (final char c : nonValidatedString) {
            if (c < '' && c > '') {
                validated.append(c);
            }
        }
        return validated.toString();
    }

    public static List<String> getSidebarLines() {
        final List<String> lines = new ArrayList<>();
        if (mc.theWorld == null) {
            return lines;
        }
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return lines;
        }
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return lines;
        }
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        final List<Score> list = new ArrayList<>();
        for (final Score input : scores) {
            if (input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#")) {
                list.add(input);
            }
        }
        if (list.size() > 15) {
            scores = new ArrayList<>(Lists.newArrayList(Iterables.skip(list, list.size() - 15)));
        } else {
            scores = list;
        }
        int index = 0;
        for (final Score score : scores) {
            ++index;
            final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
            if (index == scores.size()) {
                lines.add(objective.getDisplayName());
            }
        }
        Collections.reverse(lines);
        return lines;
    }

    public static Random getRandom() {
        return rand;
    }

    public static boolean isMoving() {
        return mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F;
    }

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if ( block == null ) {
            return false;
        }
        return !BlockUtils.isInteractable(block) && !(block instanceof BlockDaylightDetector) && !(block instanceof BlockBeacon) && !(block instanceof BlockBanner) && !(block instanceof BlockEndPortalFrame) && !(block instanceof BlockEndPortal) && !(block instanceof BlockLever) && !(block instanceof BlockButton) && !(block instanceof BlockSkull) && !(block instanceof BlockLiquid) && !(block instanceof BlockCactus) && !(block instanceof BlockDoublePlant) && !(block instanceof BlockLilyPad) && !(block instanceof BlockCarpet) && !(block instanceof BlockTripWire) && !(block instanceof BlockTripWireHook) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFlower) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockSign) && !(block instanceof BlockLadder) && !(block instanceof BlockTorch) && !(block instanceof BlockRedstoneTorch) && !(block instanceof BlockFence) && !(block instanceof BlockPane) && !(block instanceof BlockStainedGlassPane) && !(block instanceof BlockGravel) && !(block instanceof BlockClay) && !(block instanceof BlockSand) && !(block instanceof BlockSoulSand) && !(block instanceof BlockRail);
    }


    public static void aim(Entity en, float ps, boolean pc) {
        if (en != null) {
            float[] t = gr(en);
            if (t != null) {
                float y = t[0];
                float p = t[1] + 4.0F + ps;
                if (pc) {
                    mc.getNetHandler().addToSendQueue(new C05PacketPlayerLook(y, p, mc.thePlayer.onGround));
                } else {
                    mc.thePlayer.rotationYaw = y;
                    mc.thePlayer.rotationPitch = p;
                }
            }

        }
    }

    public static float[] gr(Entity q) {
        if (q == null) {
            return null;
        } else {
            double diffX = q.posX - mc.thePlayer.posX;
            double diffY;
            if (q instanceof EntityLivingBase) {
                EntityLivingBase en = (EntityLivingBase) q;
                diffY = en.posY + (double) en.getEyeHeight() * 0.9D - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            } else {
                diffY = (q.getEntityBoundingBox().minY + q.getEntityBoundingBox().maxY) / 2.0D - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            }

            double diffZ = q.posZ - mc.thePlayer.posZ;
            double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
            float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
            return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
        }
    }

    public static double n(Entity en) {
        return ((double) (mc.thePlayer.rotationYaw - getYaw(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static float getYaw(Entity ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        double yaw = Math.atan2(mc.thePlayer.posX, z) * 57.29577951308232;
        return (float) (yaw * -1.0D);
    }

    public static void ss(double s, boolean m) {
        if (!m || isMoving()) {
            mc.thePlayer.motionX = -Math.sin(cookie()) * s;
            mc.thePlayer.motionZ = Math.cos(cookie()) * s;
        }
    }

    public static boolean keysDown() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
    }

    public static boolean jumpDown() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    public static float cookie() {
        float yw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0F) {
            yw += 180.0F;
        }

        float f;
        if (mc.thePlayer.moveForward < 0.0F) {
            f = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            f = 0.5F;
        } else {
            f = 1.0F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            yw -= 90.0F * f;
        }

        if (mc.thePlayer.moveStrafing < 0.0F) {
            yw += 90.0F * f;
        }

        yw *= 0.017453292F;
        return yw;
    }

    public static float ae(float n, float n2, float n3) {
        float n4 = 1.0f;
        if (n2 < 0.0f) {
            n += 180.0f;
            n4 = -0.5f;
        } else if (n2 > 0.0f) {
            n4 = 0.5f;
        }
        if (n3 > 0.0f) {
            n -= 90.0f * n4;
        } else if (n3 < 0.0f) {
            n += 90.0f * n4;
        }
        return n * 0.017453292f;
    }

    public static double getHorizontalSpeed() {
        return getHorizontalSpeed(mc.thePlayer);
    }

    public static double getHorizontalSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
    }



    public static double gbps(Entity en, int d) {
        double x = en.posX - en.prevPosX;
        double z = en.posZ - en.prevPosZ;
        double sp = Math.sqrt(x * x + z * z) * 20.0D;
        return rnd(sp, d);
    }

    public static double rawBPS(Entity en) {
        double x = en.posX - en.prevPosX;
        double z = en.posZ - en.prevPosZ;
        double sp = Math.sqrt(x * x + z * z) * 20.0D;
        return sp;
    }

    public static String removeFormatCodes(String str) {
        return str.replace("§k", "").replace("§l", "").replace("§m", "").replace("§n", "").replace("§o", "").replace("§r", "");
    }

    public static boolean isClicking() {
        if (ModuleManager.autoClicker.isEnabled() && AutoClicker.leftClick.isToggled()) {
            return Mouse.isButtonDown(0);
        }
        else {
            return CPSCalculator.f() > 1 && System.currentTimeMillis() - CPSCalculator.LL < 300L;
        }
    }

    public static long getDifference(long n, long n2) {
        return Math.abs(n2 - n);
    }

    public static void sendModuleMessage(Module module, String s) {
        sendRawMessage("&3" + module.getInfo() + "&7: &r" + s);
    }

    public static EntityLivingBase raytrace(final int n) {
        Entity entity = null;
        MovingObjectPosition rayTrace = mc.thePlayer.rayTrace(n, 1.0f);
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float rotationYaw = mc.thePlayer.rotationYaw;
        final float rotationPitch = mc.thePlayer.rotationPitch;
        final float cos = MathHelper.cos(-rotationYaw * 0.017453292f - 3.1415927f);
        final float sin = MathHelper.sin(-rotationYaw * 0.017453292f - 3.1415927f);
        final float n2 = -MathHelper.cos(-rotationPitch * 0.017453292f);
        final Vec3 vec3 = new Vec3(sin * n2, MathHelper.sin(-rotationPitch * 0.017453292f), cos * n2);
        final Vec3 addVector = getPositionEyes.addVector(vec3.xCoord * (double)n, vec3.yCoord * (double)n, vec3.zCoord * (double)n);
        Vec3 vec4 = null;
        final List getEntitiesWithinAABBExcludingEntity = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(vec3.xCoord * (double)n, vec3.yCoord * (double)n, vec3.zCoord * (double)n).expand(1.0, 1.0, 1.0));
        double n3 = n;
        for (int i = 0; i < getEntitiesWithinAABBExcludingEntity.size(); ++i) {
            final Entity entity2 = (Entity)getEntitiesWithinAABBExcludingEntity.get(i);
            if (entity2.canBeCollidedWith()) {
                final float getCollisionBorderSize = entity2.getCollisionBorderSize();
                final AxisAlignedBB expand = entity2.getEntityBoundingBox().expand(getCollisionBorderSize, getCollisionBorderSize, getCollisionBorderSize);
                final MovingObjectPosition calculateIntercept = expand.calculateIntercept(getPositionEyes, addVector);
                if (expand.isVecInside(getPositionEyes)) {
                    if (0.0 < n3 || n3 == 0.0) {
                        entity = entity2;
                        vec4 = ((calculateIntercept == null) ? getPositionEyes : calculateIntercept.hitVec);
                        n3 = 0.0;
                    }
                }
                else if (calculateIntercept != null) {
                    final double distanceTo = getPositionEyes.distanceTo(calculateIntercept.hitVec);
                    if (distanceTo < n3 || n3 == 0.0) {
                        if (entity2 == mc.getRenderViewEntity().ridingEntity && !entity2.canRiderInteract()) {
                            if (n3 == 0.0) {
                                entity = entity2;
                                vec4 = calculateIntercept.hitVec;
                            }
                        }
                        else {
                            entity = entity2;
                            vec4 = calculateIntercept.hitVec;
                            n3 = distanceTo;
                        }
                    }
                }
            }
        }
        if (entity != null && (n3 < n || rayTrace == null)) {
            rayTrace = new MovingObjectPosition(entity, vec4);
        }
        if (rayTrace != null && rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase) {
            return (EntityLivingBase)rayTrace.entityHit;
        }
        return null;
    }

    public static int getChroma(long speed, long... delay) {
        long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
        return Color.getHSBColor((float) (time % (15000L / speed)) / (15000.0F / (float) speed), 1.0F, 1.0F).getRGB();
    }

    public static String cFL(String input) {
        if (input == null || input.isEmpty()) return input;
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public static double bypass(double number) {
        double multiple = 0.015625;
        double nm = number / multiple;
        long rm = Math.round(nm);
        return rm * multiple;
    }

    public static boolean isBypass(double number) {
        double multiple = 0.015625;
        double nm = number / multiple;
        long rm = Math.round(nm);
        double snapped = rm * multiple;
        return number == snapped;
    }


    public static void patchOptifineSettings() {
        File file = new File(Minecraft.getMinecraft().mcDataDir, "optionsof.txt");
        if (!file.exists()) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            boolean foundGlErrors = false, foundFastMath = false, foundFastRender = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.startsWith("ofShowGlErrors:")) {
                    lines.set(i, "ofShowGlErrors:false");
                    foundGlErrors = true;
                } else if (line.startsWith("ofFastMath:")) {
                    lines.set(i, "ofFastMath:false");
                    foundFastMath = true;
                } else if (line.startsWith("ofFastRender:")) {
                    lines.set(i, "ofFastRender:false");
                    foundFastRender = true;
                }
            }

            if (!foundGlErrors) lines.add("ofShowGlErrors:false");
            if (!foundFastMath) lines.add("ofFastMath:false");
            if (!foundFastRender) lines.add("ofFastRender:false");

            Files.write(file.toPath(), lines);
        } catch (IOException ignored) {
        }
    }


    public static double bypassValue = 0.015625;

    public static double rnd(double n, int d) {
        if (d == 0) {
            return (double) Math.round(n);
        } else {
            double p = Math.pow(10.0D, d);
            return (double) Math.round(n * p) / p;
        }
    }

    public static String stripColor(final String s) {
        if (s.isEmpty()) {
            return s;
        }
        final char[] array = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (final char c : array) {
            if (c < '\u007f' && c > '\u0014') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static List<String> gsl() {
        List<String> lines = new ArrayList();
        if (mc.theWorld == null) {
            return lines;
        } else {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard == null) {
                return lines;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    return lines;
                } else {
                    Collection<Score> scores = scoreboard.getSortedScores(objective);
                    List<Score> list = new ArrayList();
                    Iterator var5 = scores.iterator();

                    Score score;
                    while (var5.hasNext()) {
                        score = (Score) var5.next();
                        if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                            list.add(score);
                        }
                    }

                    if (list.size() > 15) {
                        scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
                    } else {
                        scores = list;
                    }

                    var5 = scores.iterator();

                    while (var5.hasNext()) {
                        score = (Score) var5.next();
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                        lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
                    }

                    return lines;
                }
            }
        }
    }

    public static String uf(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static boolean holdingWeapon() {
        if (mc.thePlayer.getHeldItem() == null) {
            return false;
        }
        Item getItem = mc.thePlayer.getHeldItem().getItem();
        return getItem instanceof ItemSword || (Settings.weaponAxe.isToggled() && getItem instanceof ItemAxe) || (Settings.weaponRod.isToggled() && getItem instanceof ItemFishingRod) || (Settings.weaponStick.isToggled() && getItem == Items.stick);
    }

    public static boolean holdingSword() {
        if (mc.thePlayer.getHeldItem() == null) {
            return false;
        }
        return mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public static double getDamage(final ItemStack itemStack) {
        double getAmount = 0;
        for (final Map.Entry<String, AttributeModifier> entry : itemStack.getAttributeModifiers().entries()) {
            if (entry.getKey().equals("generic.attackDamage")) {
                getAmount = entry.getValue().getAmount();
                break;
            }
        }
        return getAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25;
    }

    static int lastSlot = -1;

    public static int getSlot() {
        if (lastSlot == -1) {
            lastSlot = SlotHandler.getCurrentSlot();
        }

        int slot = 1;
        int attempts = 0;
        final int MAX_ATTEMPTS = 50;

        do {
            slot = SlotHandler.getCurrentSlot();
            attempts++;

            if (slot < 0) {
                System.out.println("Current slot is invalid: " + slot);
                break;
            }

            if (SlotHandler.getHeldItem() == null) {
                System.out.println("Held item is null.");
            } else if (!(SlotHandler.getHeldItem().getItem() instanceof ItemBlock)) {
                System.out.println("Held item is not an instance of ItemBlock.");
            } else if (!Utils.canBePlaced((ItemBlock) SlotHandler.getHeldItem().getItem())) {
                System.out.println("Held item cannot be placed.");
            } else {
                break;
            }

            if (attempts >= MAX_ATTEMPTS) {
                System.out.println("Max attempts reached without finding a valid slot.");
                break;
            }
        } while (true);

        SlotHandler.setCurrentSlot(slot);
        return slot;
    }
}
