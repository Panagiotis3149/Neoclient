package neo.module.impl.combat;

import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.player.Freecam;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.script.classes.Vec3;
import neo.util.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class AntiBot extends Module {
    private static final HashMap<EntityPlayer, Long> entities = new HashMap();
    private static final Set<EntityPlayer> filteredBot = new HashSet<>();
    private final Map<String, EntityPlayer> lastPlayers = new HashMap<>();
    private static ButtonSetting entitySpawnDelay;
    private final SliderSetting delay;
    private static ButtonSetting pitSpawn;
    private static ButtonSetting tablist;
    private static ButtonSetting matrix;

    public AntiBot() {
        super("AntiBot", category.combat, 0);
        this.registerSetting(entitySpawnDelay = new ButtonSetting("Entity spawn delay", false));
        this.registerSetting(delay = new SliderSetting("Delay", 7.0, 0.5, 15.0, 0.5, " second"));
        this.registerSetting(pitSpawn = new ButtonSetting("Pit spawn", false));
        this.registerSetting(tablist = new ButtonSetting("Tab list", false));
        this.registerSetting(matrix = new ButtonSetting("Matrix", false));
    }

    @SubscribeEvent
    public void c(final EntityJoinWorldEvent entityJoinWorldEvent) {
        if (entitySpawnDelay.isToggled() && entityJoinWorldEvent.entity instanceof EntityPlayer && entityJoinWorldEvent.entity != mc.thePlayer) {
            entities.put((EntityPlayer) entityJoinWorldEvent.entity, System.currentTimeMillis());
        }
    }

    public void onUpdate() {
        if (entitySpawnDelay.isToggled() && !entities.isEmpty()) {
            entities.values().removeIf(n -> n < System.currentTimeMillis() - delay.getInput());
        }
        lastPlayers.clear();
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (filteredBot.contains(p)) continue;

            String name = p.getName();
            if (lastPlayers.containsKey(name)) {

                EntityPlayer exists = lastPlayers.get(name);
                Vec3 thePlayer = new Vec3(mc.thePlayer);
                double existsDistance = thePlayer.distanceTo(exists);
                double curDistance = thePlayer.distanceTo(p);

                if (existsDistance > curDistance) {
                    filteredBot.add(p);
                } else {
                    filteredBot.add(exists);
                }
                break;
            }
            lastPlayers.put(name, p);
        }
    }

    public void onDisable() {
        entities.clear();
    }


    public static boolean isBot(Entity entity) {
        if (!ModuleManager.antiBot.isEnabled()) {
            return false;
        }
        if (Freecam.freeEntity != null && Freecam.freeEntity == entity) {
            return true;
        }
        if (!(entity instanceof EntityPlayer)) {
            return true;
        }
        final EntityPlayer entityPlayer = (EntityPlayer) entity;
        if (entitySpawnDelay.isToggled() && !entities.isEmpty() && entities.containsKey(entityPlayer)) {
            return true;
        }
        if (matrix.isToggled() && filteredBot.contains(entityPlayer)) {
            return true;
        }
        if (entityPlayer.isDead) {
            return true;
        }
        if (entityPlayer.getName().isEmpty()) {
            return true;
        }
        if (matrix.isToggled()) {
            String s = entityPlayer.getName();
            if (s.length() >= 4 && s.length() <= 16 && s.equals(s.toLowerCase())) {
                return true;
            }
            if (entityPlayer.getCurrentArmor(3) != null && entityPlayer.getCurrentArmor(3).getItem() == Items.diamond_helmet &&
                    entityPlayer.getCurrentArmor(2) != null && entityPlayer.getCurrentArmor(2).getItem() == Items.diamond_chestplate &&
                    entityPlayer.getCurrentArmor(1) != null && entityPlayer.getCurrentArmor(1).getItem() == Items.iron_leggings &&
                    entityPlayer.getCurrentArmor(0) != null && entityPlayer.getCurrentArmor(0).getItem() == Items.iron_boots) {
                return true;
            }
        }
        if (tablist.isToggled() && !getTablist().contains(entityPlayer.getName())) {
            return true;
        }
        if (entityPlayer.getHealth() != 20.0f && entityPlayer.getName().startsWith("§c")) {
            return true;
        }
        if (pitSpawn.isToggled() && entityPlayer.posY >= 114 && entityPlayer.posY <= 130 && entityPlayer.getDistance(0, 114, 0) <= 25) {
            if (Utils.isHypixel()) {
                List<String> sidebarLines = Utils.getSidebarLines();
                if (!sidebarLines.isEmpty() && Utils.stripColor(sidebarLines.get(0)).contains("THE HYPIXEL PIT")) {
                    return true;
                }
            }
        }
        if (entityPlayer.maxHurtTime == 0) {
            if (entityPlayer.getHealth() == 20.0f) {
                String unformattedText = entityPlayer.getDisplayName().getUnformattedText();
                if (unformattedText.length() == 10 && unformattedText.charAt(0) != '§') {
                    return true;
                }
                if (unformattedText.length() == 12 && entityPlayer.isPlayerSleeping() && unformattedText.charAt(0) == '§') {
                    return true;
                }
                if (unformattedText.length() >= 7 && unformattedText.charAt(2) == '[' && unformattedText.charAt(3) == 'N' && unformattedText.charAt(6) == ']') {
                    return true;
                }
                return entityPlayer.getName().contains(" ");
            } else if (entityPlayer.isInvisible()) {
                String unformattedText = entityPlayer.getDisplayName().getUnformattedText();
                return unformattedText.length() >= 3 && unformattedText.charAt(0) == '§' && unformattedText.charAt(1) == 'c';
            }
        }
        return false;
    }

    private static List<String> getTablist() {
        List<String> tab = new ArrayList<>();
        for (NetworkPlayerInfo networkPlayerInfo : Utils.getTablist()) {
            if (networkPlayerInfo == null) {
                continue;
            }
            tab.add(networkPlayerInfo.getGameProfile().getName());
        }
        return tab;
    }
}
