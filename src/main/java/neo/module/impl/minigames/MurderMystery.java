package neo.module.impl.minigames;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.render.RenderUtils;
import neo.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MurderMystery extends Module {
    private final ButtonSetting alert;
    private final ButtonSetting highlightMurderer;
    private final ButtonSetting highlightBow;
    private final ButtonSetting highlightInnocent;
    private final List<EntityPlayer> murderers = new ArrayList();
    private final List<EntityPlayer> hasBow = new ArrayList();
    private final String c1 = "MURDER";
    private final String c2 = "MYSTERY";
    private final String c3 = "Role:";
    private final String c4 = "&7[&cALERT&7]";
    private final String c5 = "note.pling";
    private final String c6 = "Is the murderer!";
    private final String c7 = "has a bow!";
    private boolean override;

    public MurderMystery() {
        super("MurderMystery", category.minigames);
        this.registerSetting(alert = new ButtonSetting("Alert murderer", true));
        this.registerSetting(highlightMurderer = new ButtonSetting("Highlight murderer", true));
        this.registerSetting(highlightBow = new ButtonSetting("Highlight bow", true));
        this.registerSetting(highlightInnocent = new ButtonSetting("Highlight innocent", true));
    }

    public void onDisable() {
        this.clear();
    }

    @SubscribeEvent
    public void o(RenderWorldLastEvent e) {
        if (Utils.isntnull()) {
            if (!this.isMurderMystery()) {
                this.clear();
            } else {
                override = false;
                for (EntityPlayer en : mc.theWorld.playerEntities) {
                    if (en != mc.thePlayer && !en.isInvisible()) {
                        if (en.getHeldItem() != null && en.getHeldItem().hasDisplayName()) {
                            Item i = en.getHeldItem().getItem();
                            if (i instanceof ItemSword || i instanceof ItemAxe || en.getHeldItem().getDisplayName().contains("aKnife")) {
                                if (!murderers.contains(en)) {
                                    murderers.add(en);
                                    if (alert.isToggled()) {
                                        mc.thePlayer.playSound(this.c5, 1.0F, 1.0F);
                                        Utils.sendMessage(this.c4 + " &e" + en.getName() + " &3" + this.c6);
                                    }
                                } else if (i instanceof ItemBow && highlightMurderer.isToggled() && !hasBow.contains(en)) {
                                    hasBow.add(en);
                                }
                            }
                        }
                        override = true;
                        int rgb = Color.green.getRGB();
                        if (murderers.contains(en) && highlightMurderer.isToggled()) {
                            rgb = Color.red.getRGB();
                        }
                        else if (hasBow.contains(en) && highlightBow.isToggled()) {
                            rgb = Color.orange.getRGB();
                        }
                        else if (!highlightInnocent.isToggled()) {
                            continue;
                        }
                        RenderUtils.renderEntity(en, 2, 0.0D, 0.0D, rgb, false);
                    }
                }
            }
        }
    }

    private boolean isMurderMystery() {
        if (Utils.isHypixel()) {
            if (mc.thePlayer.getWorldScoreboard() == null || mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1) == null) {
                return false;
            }

            String d = mc.thePlayer.getWorldScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
            if (!d.contains(this.c1) && !d.contains(this.c2)) {
                return false;
            }

            Iterator var2 = Utils.gsl().iterator();

            while (var2.hasNext()) {
                String l = (String) var2.next();
                String s = Utils.stripColor(l);
                if (s.contains(this.c3)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return murderers.isEmpty() && hasBow.isEmpty() && !override;
    }

    private void clear() {
        override = false;
        murderers.clear();
        hasBow.clear();
    }
}
