package neo.module.impl.minigames;

import neo.module.Module;
import neo.module.impl.combat.AntiBot;
import neo.module.setting.impl.ButtonSetting;
import neo.util.world.block.BlockUtils;
import neo.util.render.RenderUtils;
import neo.util.Utils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BedWars extends Module {
    public static ButtonSetting whitelistOwnBed;
    private final ButtonSetting diamondArmor;
    private final ButtonSetting enderPearl;
    private final ButtonSetting obsidian;
    private final ButtonSetting shouldPing;
    private BlockPos spawnPos;
    private boolean check;
    public static boolean outsideSpawn = true;
    private final List<String> armoredPlayer = new ArrayList<>();
    private final Map<String, String> lastHeldMap = new ConcurrentHashMap<>();
    private final Set<BlockPos> obsidianPos = new HashSet<>();
    private final int obsidianColor = new Color(0, 0,0).getRGB();

    public BedWars() {
        super("BedWars", category.minigames);
        this.registerSetting(whitelistOwnBed = new ButtonSetting("Whitelist own bed", true));
        this.registerSetting(diamondArmor = new ButtonSetting("Diamond armor", true));
        this.registerSetting(enderPearl = new ButtonSetting("Ender pearl", true));
        this.registerSetting(obsidian = new ButtonSetting("Obsidian", true));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
    }

    public void onEnable() {
        armoredPlayer.clear();
        lastHeldMap.clear();
        obsidianPos.clear();
        check = false;
        outsideSpawn = true;
    }

    public void onDisable() {
        outsideSpawn = true;
    }

    @SubscribeEvent
    public void onBlock(BlockEvent.PlaceEvent e) {
        if (!Utils.isntnull() || !obsidian.isToggled()) {
            return;
        }
        if (!(e.state.getBlock() instanceof BlockObsidian)) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (BlockUtils.getBlock(e.pos.offset(facing)) instanceof BlockBed) {
                obsidianPos.add(e.pos);
                Utils.sendMessage(e.player.getDisplayName().getFormattedText() + " &7placed &dObsidian");
                break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (Utils.isntnull()) {
            if (this.obsidianPos.isEmpty()) {
                return;
            }
            try {
                Iterator<BlockPos> iterator = this.obsidianPos.iterator();
                while (iterator.hasNext()) {
                    BlockPos blockPos = iterator.next();
                    if (!(mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockObsidian)) {
                        iterator.remove();
                        continue;
                    }
                    RenderUtils.renderBlock(blockPos, obsidianColor, false, true);
                }
            }
            catch (Exception exception) {}
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        if (!Utils.isntnull() || e.entity == null) {
            return;
        }
        if (e.entity == mc.thePlayer) {
            armoredPlayer.clear();
            lastHeldMap.clear();
        }
    }

    public void onUpdate() {
        if (Utils.getBedwarsStatus() == 2) {
            if (diamondArmor.isToggled() || enderPearl.isToggled() || obsidian.isToggled()) {
                for (EntityPlayer p : mc.theWorld.playerEntities) {
                    if (p == null) {
                        continue;
                    }
                    if (p == mc.thePlayer) {
                        continue;
                    }
                    if (AntiBot.isBot(p)) {
                        continue;
                    }
                    String name = p.getName();
                    ItemStack item = p.getHeldItem();
                    if (diamondArmor.isToggled()) {
                        ItemStack leggings = p.inventory.armorInventory[1];
                        if (!armoredPlayer.contains(name) && p.inventory != null && leggings != null && leggings.getItem() != null && leggings.getItem() == Items.diamond_leggings) {
                            armoredPlayer.add(name);
                            Utils.sendMessage("&eAlert: &r" + p.getDisplayName().getFormattedText() + " &7has purchased &bDiamond Armor");
                            ping();
                        }
                    }
                    if (item != null && !lastHeldMap.containsKey(name)) {
                        String itemType = getItemType(item);
                        if (itemType != null) {
                            lastHeldMap.put(name, itemType);
                            double distance = Math.round(mc.thePlayer.getDistanceToEntity(p));
                            handleAlert(itemType, p.getDisplayName().getFormattedText(), Utils.isWholeNumber(distance) ? (int) distance + "" : String.valueOf(distance));
                        }
                    } else if (lastHeldMap.containsKey(name)) {
                        String itemType = lastHeldMap.get(name);
                        if (!itemType.equals(getItemType(item))) {
                            lastHeldMap.remove(name);
                        }
                    }
                }
            }
            if (whitelistOwnBed.isToggled()) {
                if (check) {
                    spawnPos = mc.thePlayer.getPosition();
                    check = false;
                }
                if (spawnPos == null) {
                    outsideSpawn = true;
                }
                else {
                    outsideSpawn = mc.thePlayer.getDistanceSq(spawnPos) > 800;
                }
            }
            else {
                outsideSpawn = true;
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent c) {
        if (!Utils.isntnull()) {
            return;
        }
        String strippedMessage = Utils.stripColor(c.message.getUnformattedText());
        if (strippedMessage.startsWith(" ") && strippedMessage.contains("Protect your bed and destroy the enemy beds.")) {
            check = true;
        }
    }

    private String getItemType(ItemStack item) {
        if (item == null || item.getItem() == null) {
            return null;
        }
        String unlocalizedName = item.getItem().getUnlocalizedName();
        if (item.getItem() instanceof ItemEnderPearl && enderPearl.isToggled()) {
            return "&7an §3Ender Pearl";
        } else if (unlocalizedName.contains("tile.obsidian") && obsidian.isToggled()) {
            return "§dObsidian";
        }
        return null;
    }

    private void handleAlert(String itemType, String name, String info) {
        String alert = "&eAlert: &r" + name + " &7is holding " + itemType + " &7(" + "§d" + info + "m" + "&7)";
        Utils.sendMessage(alert);
        ping();
    }

    private void ping() {
        if (shouldPing.isToggled()) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
        }
    }
}