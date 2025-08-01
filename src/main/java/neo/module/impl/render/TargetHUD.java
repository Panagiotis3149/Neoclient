package neo.module.impl.render;

import neo.module.Module;
import neo.module.impl.combat.KillAura;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.*;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.font.impl.MinecraftFontRenderer;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.render.animation.Animation;
import neo.util.render.animation.Easing;
import neo.util.render.animation.Timer;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static neo.util.render.Theme.getColors;

public class TargetHUD extends Module {
    public static SliderSetting theme;
    private final ButtonSetting showStatus;
    private final ButtonSetting healthColor;
    private Timer fadeTimer;
    private Timer healthBarTimer = null;
    private EntityLivingBase target;
    private long lastAliveMS;
    private double lastHealth;
    private float lastHealthBar;
    public EntityLivingBase renderEntity;
    private final String[] modes = new String[]{"Fate", "Raven", "Exhibition", "Myau", "Pulsive", "Neo", "NeoUnique"};
    private final SliderSetting mode;
    FontRenderer font = FontManager.productSansMedium;
    public static int current$minX;
    public static int current$maxX;
    public static int current$minY;
    public static int current$maxY;
    private final Animation healthBarAnimation = new Animation(Easing.LINEAR, 250);
    private EntityLivingBase lastTarget;
    private Object aura;


    public TargetHUD() {
        super("TargetHUD", category.render);
        registerSetting(new DescriptionSetting("Who am i targeting? (Killaura Only)"));
        registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        registerSetting(mode = new SliderSetting("Mode", modes, 0));
        registerSetting(showStatus = new ButtonSetting("Show win or loss", true));
        registerSetting(healthColor = new ButtonSetting("Traditional health color", true));
    }

    public void onDisable() {
        reset();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.isntnull()) {
            reset();
            return;
        }
        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                reset();
                return;
            }
            if (KillAura.target != null) {
                target = KillAura.target;
                lastAliveMS = System.currentTimeMillis();
                fadeTimer = null;
            } else if (target != null) {
                if (System.currentTimeMillis() - lastAliveMS >= 200 && fadeTimer == null) {
                    (fadeTimer = new Timer(400)).start();
                }
            }
            else {
                return;
            }
            String playerInfo = target.getDisplayName().getFormattedText();
            double health = target.getHealth() / target.getMaxHealth();
            if (target.isDead) {
                health = 0;
            }
            if (health != lastHealth) {
                (healthBarTimer = new Timer(350)).start();
            }
            lastHealth = health;
            playerInfo += " " + Utils.getHealthStr(target);
            select(fadeTimer, playerInfo, health, target.getDisplayName().getFormattedText());
        }
    }



    public void select(Timer cd, String string, double health, String name) {
        if ((int) mode.getInput() == 0) {
            int colorh = health >= 0.66f ? 0xFF66BB6A : (health >= 0.33f ? 0xFFDD7621 : 0xFFC93434);
            String nameText = string + "HP";
            if (nameText.length() > 9) {
                font = FontManager.productSans16;
            } else if (nameText.length() > 6) {
                font = FontManager.productSans20;
            } else {
                font = FontManager.productSansMedium;
            }
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int padding = 4;
            final int playerHeadSize = 32;
            final int rectWidth = (int) (40 * 1.4 + font.getStringWidth(nameText));
            final int rectHeight = (int) (32 * 1.1);
            final int baseX = scaledResolution.getScaledWidth() / 2 + 70;
            final int baseY = scaledResolution.getScaledHeight() / 2 + 6;
            final int rectLeft = baseX;
            final int rectTop = baseY;
            final int rectRight = baseX + rectWidth;
            final int rectBottom = baseY + rectHeight;
            final int healthBarHeight = 3;
            float healthBarWidth = (float) ((float) (rectRight - rectLeft) * health);
            final int alphaValue = (cd == null) ? 255 : (255 - cd.getValueInt(0, 255, 1));
            if (alphaValue > 0) {
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound(baseX, baseY, rectWidth, rectHeight, 0.0f, Color.black);
                float inputToRange = (float) (3 * (76 + 35) / 100);
                BlurUtils.blurEnd(2, 2.5F);
                RenderUtils.drawOutline(rectLeft, rectTop, rectRight, rectBottom, 2, 0xFF78E689);
                RenderUtils.drawRect(rectLeft, rectBottom - healthBarHeight, rectLeft + (int) healthBarWidth, rectBottom, colorh);
                EntityLivingBase target = KillAura.target;
                if (target != null) {
                    RenderUtils.drawPlayerHead(rectLeft, rectTop, playerHeadSize, target);
                }
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                font.drawString(nameText, baseX + playerHeadSize + padding, rectTop + padding + 3, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(alphaValue + 15) << 24, false);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        } else if ((int) mode.getInput() == 1) {
            if (showStatus.isToggled()) {
                string = string + " " + ((health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
            }
            final int n2 = 8;
            final int n3 = mc.fontRendererObj.getStringWidth(string) + n2;
            final ScaledResolution sR = new ScaledResolution(mc);
            final int n4 = sR.getScaledWidth() / 2 - n3 / 2 + 70;
            final int n5 = sR.getScaledHeight() / 2 + 15 + 30;
            final int n6 = n4 - n2;
            final int n7 = n5 - n2;
            final int n8 = n4 + n3;
            final int n9 = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;
            final int n10 = (cd == null) ? 255 : (255 - cd.getValueInt(0, 255, 1));
            if (n10 > 0) {
                final int n11 = (n10 > 110) ? 110 : n10;
                final int n12 = (n10 > 210) ? 210 : n10;
                final int[] array = Theme.getGradients((int) theme.getInput());
                RenderUtils.drawRoundedGradientOutlinedRectangle((float) n6, (float) n7, (float) n8, (float) (n9 + 13), 10.0f, Utils.merge(Color.black.getRGB(), n11), Utils.merge(array[0], n10), Utils.merge(array[1], n10));
                final int n13 = n6 + 6;
                final int n14 = n8 - 6;
                final int n15 = n9;
                RenderUtils.drawRoundedRectangle((float) n13, (float) n15, (float) n14, (float) (n15 + 5), 4.0f, Utils.merge(Color.black.getRGB(), n11));
                int k = Utils.merge(array[0], n12);
                int n16 = Utils.merge(array[1], n12);
                float healthBar = (float) (int) (n14 + (n13 - n14) * (1 - health));
                if (healthBar != lastHealthBar && lastHealthBar - n13 >= 3 && healthBarTimer != null) {
                    float diff = lastHealthBar - healthBar;
                    if (diff > 0) {
                        lastHealthBar = lastHealthBar - healthBarTimer.getValueFloat(0, diff, 1);
                    } else {
                        lastHealthBar = healthBarTimer.getValueFloat(lastHealthBar, healthBar, 1);
                    }
                } else {
                    lastHealthBar = healthBar;
                }
                if (healthColor.isToggled()) {
                    k = n16 = Utils.merge(Utils.getColorForHealth(health), n12);
                }
                if (lastHealthBar > n14) {
                    lastHealthBar = n14;
                }
                RenderUtils.drawRoundedGradientRect((float) n13, (float) n15, lastHealthBar, (float) (n15 + 5), 4.0f, k, k, k, n16);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                mc.fontRendererObj.drawString(string, (float) n4, (float) n5, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10 + 15) << 24, true);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        } else if ((int) mode.getInput() == 2) {
            final ScaledResolution sRR = new ScaledResolution(mc);
            FontRenderer fonta = FontManager.productSansMedium;
            final int n2a = 8;
            final int n3a = mc.fontRendererObj.getStringWidth(target.getDisplayName().getFormattedText()) + n2a;
            final int n4a = sRR.getScaledWidth() / 2 - n3a / 2 + 70;
            final int n5a = sRR.getScaledHeight() / 2 + 15 + 30;
            current$minX = n4a - n2a;
            current$minY = n5a - n2a;
            current$maxX = n4a + n3a;
            current$maxY = n5a + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2a;
            double x = TargetHUD.current$minX;
            double y = TargetHUD.current$minY;
            RenderUtils.drawRect(x, y, x + 140, y + 50, new Color(0, 0, 0).getRGB());
            RenderUtils.drawRect(x + 0.5, y + 0.5, x + 0.5 + 139, y + 0.5 + 49, new Color(60, 60, 60).getRGB());
            RenderUtils.drawRect(x + 1.5, y + 1.5, x + 1.5 + 137, y + 1.5 + 47, new Color(0, 0, 0).getRGB());
            RenderUtils.drawRect(x + 2, y + 2, x + 2 + 136, y + 2 + 46, new Color(25, 25, 24).getRGB());
            fonta.drawString(name, (int) (x + 40), (int) (y + 6), Color.WHITE.getRGB());
            if (mc.thePlayer != null) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.7, 0.7, 0.7);
                fonta.drawString("HP: " + Math.round(target.getHealth()) + " | Dist: " + Math.round(mc.thePlayer.getDistanceToEntity(target)), (int) ((x + 40) * (1 / 0.7)), (int) ((y + 17) * (1 / 0.7)), Color.WHITE.getRGB());
                GlStateManager.popMatrix();
            }
            double healtha = Math.min(Math.round(target.getHealth()), target.getMaxHealth());
            float healthColor = Utils.getCompleteHealth(target);
            double x2 = x + 40;
            RenderUtils.drawRect(x2, y + 25, x + 100 - 9, y + 25 + 5, (int) healthColor);
            RenderUtils.drawRect(x2, y + 25, x + (100 - 9) * (health / target.getMaxHealth()), y + 25 + 6, (int) healthColor);
            RenderUtils.drawRect(x2, y + 25, x + 91, y + 25 + 1, Color.BLACK.getRGB());
            RenderUtils.drawRect(x2, y + 30, x + 91, y + 30 + 1, Color.BLACK.getRGB());
            for (int i = 0; i < 10; i++) {
                RenderUtils.drawRect(x2 + 10 * i, y + 25, x2 + 10 * i + 1, y + 25 + 6, Color.BLACK.getRGB());
            }
            RenderUtils.renderItemIcon(x2, y + 31, target.getHeldItem());
            RenderUtils.renderItemIcon(x2 + 15, y + 31, target.getEquipmentInSlot(4));
            RenderUtils.renderItemIcon(x2 + 30, y + 31, target.getEquipmentInSlot(3));
            RenderUtils.renderItemIcon(x2 + 45, y + 31, target.getEquipmentInSlot(2));
            RenderUtils.renderItemIcon(x2 + 60, y + 31, target.getEquipmentInSlot(1));
            if (mc.thePlayer != null) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.4, 0.4, 0.4);
                GlStateManager.translate((x + 20) * (1 / 0.4), (y + 44) * (1 / 0.4), 40f * (1 / 0.4));
                drawModel(target.rotationYaw, target.rotationPitch, target);
                GlStateManager.popMatrix();
            }
        } else if ((int) mode.getInput() == 3) {
            MinecraftFontRenderer fontb = MinecraftFontRenderer.INSTANCE;
            String TargetName = target.getDisplayName().getFormattedText();
            String TargetHealth = String.format("%.1f", target.getHealth()) + "§c❤ ";
            if (showStatus.isToggled() && mc.thePlayer != null) {
                String status = (health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? " §aW" : " §cL";
                TargetName = TargetName + status;
            }
            final ScaledResolution sRRR = new ScaledResolution(mc);
            final int n2ex = 8;
            final int n3ex = mc.fontRendererObj.getStringWidth(TargetName) + n2ex + 20;
            final int n4ex = sRRR.getScaledWidth() / 2 - n3ex / 2 + 70;
            final int n5ex = sRRR.getScaledHeight() / 2 + 15 + 30;
            current$minX = n4ex - n2ex;
            current$minY = n5ex - n2ex;
            current$maxX = n4ex + n3ex;
            current$maxY = n5ex + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2ex;
            final int n10ex = 255;
            final int n11 = Math.min(n10ex, 110);
            final int n12 = Math.min(n10ex, 210);
            RenderUtils.drawRect(current$minX, current$minY, current$maxX, current$maxY + 7, Utils.merge(Color.black.getRGB(), Math.min(n10ex, 60)));
            final int n13 = current$minX + 6 + 27;
            final int n14 = current$maxX - 2;
            final int n15 = (int) (current$maxY + 0.45);
            RenderUtils.drawRect(n13, n15, n14, n15 + 4, Utils.merge(Color.black.getRGB(), n11));
            float healthBar = (float) (int) (n14 + (n13 - n14) * (1.0 - ((health < 0.01) ? 0 : health)));
            if (healthBar - n13 < 1) {
                healthBar = n13;
            }
            if (target != lastTarget) {
                healthBarAnimation.setValue(healthBar);
                lastTarget = target;
            }
            float displayHealthBar;
            healthBarAnimation.run(healthBar);
            displayHealthBar = (float) healthBarAnimation.getValue();
            RenderUtils.drawRect(n13, n15, displayHealthBar, n15 + 4, Utils.merge(Theme.getGradients((int) theme.getInput())[0], n12));
            if (healthColor.isToggled()) {
                int healthTextColor = Utils.getColorForHealth(health);
                RenderUtils.drawRect(n13, n15, displayHealthBar, n15 + 4, healthTextColor);
            }
            int playerHeadSizea = 32;
            int playerHeadX = 500;
            int playerHeadY = 500;
            RenderUtils.drawPlayerHead(playerHeadX, playerHeadY, playerHeadSizea, target);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            fontb.drawString(TargetName, (float) (n4ex + 25), (float) n5ex - 4, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10ex + 15) << 24, true);
            fontb.drawString(TargetHealth, (float) (n4ex + 25), (float) n5ex + 6, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10ex + 15) << 24, true);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } else if ((int) mode.getInput() == 4) {
            final ScaledResolution sCRES = new ScaledResolution(mc);
            FontRenderer fontr = FontManager.google;
            FontRenderer fontm = FontManager.googleMedium;
            double x5 = (double) sCRES.getScaledWidth() / 2 + 70;
            double y5 = (double) sCRES.getScaledHeight() / 2 + 6;
            String TargetName2 = target.getDisplayName().getFormattedText();
            String TargetHealth2 = "HP" + (int) target.getHealth();
            int width = Math.max(80, (int) fontm.getStringWidth(TargetName2) + 40);
            int height = 40;
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) x5, (float) y5, width, height, 4.0f, RenderUtils.toColor(RenderUtils.toArgb(RenderUtils.toRgbColor(Theme.getGradient(theme.getInput(), 0)), 255)));
            BlurUtils.blurEnd(2, 1.75F);
            RenderUtils.drawRoundedRectangle((float) x5, (float) y5, (float) x5 + width, (float) y5 + height, 4, 0x66161616);
            int playerHeadSize2 = 23;
            int playerHeadX2 = (int) (x5 + 4);
            int playerHeadY2 = (int) (y5 + 4);
            RenderUtils.drawPlayerHead(playerHeadX2, playerHeadY2, playerHeadSize2, target);
            float healthBarW = (width + 20) * (target.getHealth() / target.getMaxHealth());
            float healthBarH = 6;
            int hbx = (int) (x5 + 4);
            int hby = (int) (y5 + height - 10);
            health = RenderUtils.animate((float) health, hbx + healthBarW, 0.05f);
            int[] colors = getColors((int) theme.getInput());
            int hColor1 = colors[0];
            int hColor2 = colors[1];
            RenderUtils.drawRoundedGradientRect(hbx, hby, (float) health, hby + healthBarH, 4, hColor1, hColor2, hColor2, hColor1);
            int nposx = (int) (x5 + 31);
            int nposy = (int) (y5 + 6);
            int hposy = (int) (y5 + 21);
            fontm.drawString(TargetName2, nposx, nposy, -1, false);
            fontr.drawString(TargetHealth2, nposx, hposy, 0xFFCCCCCC, false);
        } else if ((int) mode.getInput() == 5) {
            final ScaledResolution sCRES = new ScaledResolution(mc);
            FontRenderer fontr = FontManager.productSansLight22;
            FontRenderer fontm = FontManager.googleSansMedium;
            double x5 = (double) sCRES.getScaledWidth() / 2 + 70;
            double y5 = (double) sCRES.getScaledHeight() / 2 + 6;
            String TargetName2 = target.getDisplayName().getFormattedText();
            String TargetHealth2 = String.valueOf((int)target.getHealth());
            int width = Math.max(80, (int) fontm.getStringWidth(TargetName2) + 40);
            int height = 40;
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) x5, (float) y5, width, height, 4.0f, Color.black);
            BlurUtils.blurEnd(2, 1.75F);
            RenderUtils.drawRoundedRectangle((float) x5, (float) y5, (float) x5 + width, (float) y5 + height, 4, 0x66161616);
            int playerHeadSize2 = 23;
            int playerHeadX2 = (int) (x5 + 4);
            int playerHeadY2 = (int) (y5 + 4);
            RenderUtils.drawPlayerHead(playerHeadX2, playerHeadY2, playerHeadSize2, target);
            float healthBarW = (width + 21) * (target.getHealth() / target.getMaxHealth());
            float healthBarH = 6;
            int hbx = (int) (x5 + 4);
            int hby = (int) (y5 + height - 10);
            health = RenderUtils.animate((float) health, hbx + healthBarW, 0.05f);
            int[] colors = getColors((int) theme.getInput());
            int hColor1 = colors[0];
            int hColor2 = colors[1];
            RenderUtils.drawRoundedGradientRect(hbx, hby, (float) health, hby + healthBarH, 4, hColor1, hColor2, hColor2, hColor1);
            RenderUtils.drawRoundedRectangle(hbx, hby, healthBarW, hby + healthBarH, 4, 0x22202024);
            int nposx = (int) (x5 + 31);
            int nposy = (int) (y5 + 6);
            int hposy = (int) (y5 + 16);
            fontm.drawString(TargetName2, nposx, nposy, 0xFFCCCCCC, false);
            fontr.drawString(TargetHealth2, nposx, hposy, -1, false);
        } else if ((int) mode.getInput() == 6) {
            final ScaledResolution sCRES = new ScaledResolution(mc);
            FontRenderer fontr = FontManager.productSansLight22;
            FontRenderer fontm = FontManager.googleSansMedium;
            double x5 = -2;
            double y5 = (double) sCRES.getScaledHeight() / 2 - 40;
            String TargetName2 = target.getDisplayName().getFormattedText();
            String TargetHealth2 = "H: " + (int) target.getHealth();
            int width = Math.max(60, (int) fontm.getStringWidth(TargetName2) + 20);
            int height = 80;
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) x5, (float) y5, width, height, 4.0f, Color.black);
            BlurUtils.blurEnd(2, 1.75F);
            RenderUtils.drawRoundedRectangle((float) x5, (float) y5, (float) x5 + width, (float) y5 + height, 4, 0x66161616);
            int playerHeadSize2 = 32;
            int playerHeadX2 = (int) (x5 + 4);
            int playerHeadY2 = (int) (y5 + height - 36);
            RenderUtils.drawPlayerHead(playerHeadX2, playerHeadY2, playerHeadSize2, target);
            float healthBarW = (width - 2) * (target.getHealth() / target.getMaxHealth());
            float healthBarH = 6;
            int hbx = (int) (x5 + 4);
            int hby = (int) (y5 + 5);
            health = RenderUtils.animate((float) health, hbx + healthBarW, 0.05f);
            int[] colors = getColors((int) theme.getInput());
            int hColor1 = colors[0];
            int hColor2 = colors[1];
            RenderUtils.drawRoundedGradientRect(hbx, hby, (float) health, hby + healthBarH, 4, hColor1, hColor2, hColor2, hColor1);
            int nposx = (int) (x5 + 5);
            int nposy = (int) (y5 + 18);
            int hposy = (int) (y5 + 32);
            fontm.drawString(TargetName2, nposx, nposy,0xFFCCCCCC , false);
            fontr.drawString(TargetHealth2, nposx, hposy, Utils.getColorForHealth(target.getHealth()), false);
            String distText = "D: " + (int) target.getDistanceToEntity(mc.thePlayer);
            int estDTSTUX = (int) (fontr.getStringWidth(distText) + 5);
            if (width >= estDTSTUX) {
                fontr.drawString(distText, nposx + fontr.getStringWidth(TargetHealth2) + 5, hposy, 0xFFCCCCCC, false);
            }
        }
    }

    private void reset() {
        fadeTimer = null;
        target = null;
        healthBarTimer = null;
        renderEntity = null;
    }


    public static void drawModel(final float yaw, final float pitch, final @NotNull EntityLivingBase entityLivingBase) { // Method to draw full model (double skidded)
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = entityLivingBase.renderYawOffset;
        final float rotationYaw = entityLivingBase.rotationYaw;
        final float rotationPitch = entityLivingBase.rotationPitch;
        final float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        final float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - 0.4f;
        entityLivingBase.rotationYaw = yaw - 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }
}