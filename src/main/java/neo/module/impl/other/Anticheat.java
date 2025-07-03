package neo.module.impl.other;

import neo.module.Module;
import neo.module.impl.combat.AntiBot;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.module.impl.other.anticheat.impl.*;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import java.util.List;
import neo.util.player.move.PlayerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

import static neo.Variables.clientName;

public class Anticheat extends Module {
    public final SliderSetting interval;
    public final ButtonSetting enemyAdd;
    public final ButtonSetting autoReport;
    public final ButtonSetting ignoreTeammates;
    public final ButtonSetting atlasSuspect;
    public final ButtonSetting shouldPing;

    public final ButtonSetting autoBlock;
    public final ButtonSetting noFall;
    public final ButtonSetting noSlow;
    public final ButtonSetting scaffold;
    public final ButtonSetting legitScaffold;
    public final ButtonSetting flight;
    public final ButtonSetting speed;

    private final HashMap<UUID, HashMap<ButtonSetting, Long>> flags = new HashMap<>();
    private final HashMap<UUID, PlayerData> players = new HashMap<>();
    private final List<AnticheatComponent> components = new ArrayList<>();

    private long lastAlert;

    public Anticheat() {
        super("Anticheat", category.other);

        this.registerSetting(new DescriptionSetting("Tries to detect cheaters."));
        this.registerSetting(interval = new SliderSetting("Flag interval", 20.0, 0.0, 60.0, 1.0, " second"));
        this.registerSetting(enemyAdd = new ButtonSetting("Add cheaters as enemy", false));
        this.registerSetting(autoReport = new ButtonSetting("Auto report", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
        this.registerSetting(atlasSuspect = new ButtonSetting("Only atlas suspect", false));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(new DescriptionSetting("Detected cheats"));
        this.registerSetting(autoBlock = new ButtonSetting("Autoblock", true));
        this.registerSetting(noFall = new ButtonSetting("NoFall", true));
        this.registerSetting(flight = new ButtonSetting("Fly", true));
        this.registerSetting(speed = new ButtonSetting("Speed", true));
        this.registerSetting(noSlow = new ButtonSetting("NoSlow", true));
        this.registerSetting(scaffold = new ButtonSetting("Scaffold", true));
        this.registerSetting(legitScaffold = new ButtonSetting("Legit scaffold", true));

        components.add(new AutoblockCheck());
        components.add(new LegitScaffoldCheck());
        components.add(new NoSlowCheck());
        components.add(new ScaffoldCheck());
        components.add(new SpeedCheck());
        components.add(new FlightCheck());
        components.add(new NoFallCheck());
    }

    public void alert(EntityPlayer entityPlayer, ButtonSetting mode, String details) {
        if (Utils.isFriended(entityPlayer) || (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer))) return;
        if (atlasSuspect.isToggled() && !entityPlayer.getName().equals("Suspect§r")) return;

        if (enemyAdd.isToggled()) {
            Utils.addEnemy(entityPlayer.getName());
        }

        final long now = System.currentTimeMillis();
        if (interval.getInput() > 0.0) {
            HashMap<ButtonSetting, Long> hashMap = flags.get(entityPlayer.getUniqueID());
            if (hashMap == null) hashMap = new HashMap<>();
            else {
                Long last = hashMap.get(mode);
                if (last != null && Utils.getDifference(last, now) <= interval.getInput() * 1000.0) return;
            }
            hashMap.put(mode, now);
            flags.put(entityPlayer.getUniqueID(), hashMap);
        }

        ChatComponentText msg = new ChatComponentText(Utils.formatColor("&7[&b" + clientName + "&7]&r " + entityPlayer.getDisplayName().getUnformattedText() + " &7detected for &d" + mode.getName() + " - " + details));
        ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report " + entityPlayer.getName()));
        msg.appendSibling(new ChatComponentText(Utils.formatColor(" §7[§cReport§7]")).setChatStyle(style));
        mc.thePlayer.addChatMessage(msg);

        if (shouldPing.isToggled() && Utils.getDifference(lastAlert, now) >= 1500L) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
            lastAlert = now;
        }

        if (autoReport.isToggled() && !Utils.isFriended(entityPlayer)) {
            mc.thePlayer.sendChatMessage("/report " + Utils.stripColor(entityPlayer.getGameProfile().getName()));
        }
    }

    public void alert(EntityPlayer entityPlayer, ButtonSetting mode) {
        if (Utils.isFriended(entityPlayer) || (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer))) return;
        if (atlasSuspect.isToggled() && !entityPlayer.getName().equals("Suspect§r")) return;

        if (enemyAdd.isToggled()) {
            Utils.addEnemy(entityPlayer.getName());
        }

        final long now = System.currentTimeMillis();
        if (interval.getInput() > 0.0) {
            HashMap<ButtonSetting, Long> hashMap = flags.get(entityPlayer.getUniqueID());
            if (hashMap == null) hashMap = new HashMap<>();
            else {
                Long last = hashMap.get(mode);
                if (last != null && Utils.getDifference(last, now) <= interval.getInput() * 1000.0) return;
            }
            hashMap.put(mode, now);
            flags.put(entityPlayer.getUniqueID(), hashMap);
        }

        ChatComponentText msg = new ChatComponentText(Utils.formatColor("&7[&b" + clientName + "&7]&r " + entityPlayer.getDisplayName().getUnformattedText() + " &7detected for &d" + mode.getName()));
        ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report " + entityPlayer.getName()));
        msg.appendSibling(new ChatComponentText(Utils.formatColor(" §7[§cReport§7]")).setChatStyle(style));
        mc.thePlayer.addChatMessage(msg);

        if (shouldPing.isToggled() && Utils.getDifference(lastAlert, now) >= 1500L) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
            lastAlert = now;
        }

        if (autoReport.isToggled() && !Utils.isFriended(entityPlayer)) {
            mc.thePlayer.sendChatMessage("/report " + Utils.stripColor(entityPlayer.getGameProfile().getName()));
        }
    }

    public void onUpdate() {
        if (mc.isSingleplayer()) return;

        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer == null || entityPlayer == mc.thePlayer || AntiBot.isBot(entityPlayer)) continue;

            PlayerData data = players.getOrDefault(entityPlayer.getUniqueID(), new PlayerData());
            data.update(entityPlayer);
            for (AnticheatComponent check : components) {
                check.check(entityPlayer, data, this);
            }
            data.updateServerPos(entityPlayer);
            data.updateSneak(entityPlayer);
            players.put(entityPlayer.getUniqueID(), data);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            players.clear();
            flags.clear();
        }
    }

    public void onDisable() {
        players.clear();
        flags.clear();
        lastAlert = 0L;
    }
}
