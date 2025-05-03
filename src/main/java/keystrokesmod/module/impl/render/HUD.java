package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class HUD extends Module {
    public static SliderSetting theme;
    public static ButtonSetting dropShadow;
    public static ButtonSetting alphabeticalSort;
    public static SliderSetting fonts;
    public static SliderSetting bloomType;
    public static SliderSetting outliness;
    public static SliderSetting blurss;
    public static SliderSetting bgss;
    public static SliderSetting cbgss;
   // public static SliderSetting round;
    public static ButtonSetting alignRight;
    private static ButtonSetting lowercase;
    public static ButtonSetting showInfo;
 //   public static ButtonSetting colorout;
    public static int hudX = 5;
    public static int hudY = 70;
    private boolean isAlphabeticalSort;
    private boolean canShowInfo;
    public String[] cbgs = new String[]{"None", "Light", "Normal", "Heavy"};
    public String[] outlines = new String[]{"None", "Sidebar", "NewSidebar"}; // , "AllOutline"};
    public String[] blurs = new String[]{"None", "Light", "Normal", "Heavy"};
    public String[] bgs = new String[]{"None", "Transparent", "Normal", "Opaque"};
    public String[] blooms = new String[]{"None", "Shadow", "Glow"};
    public String[] fontss = new String[]{"Minecraft", "Helvetica Neue", "Product Sans", "Google", "Apple UI", "Greycliff CF", "Product Sans Light", "Poppins Bold"};

    public HUD() {
        super("HUD", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(fonts = new SliderSetting("Font", fontss, 0));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(new ButtonSetting("Edit position", () -> mc.displayGuiScreen(new EditScreen())));
        this.registerSetting(alignRight = new ButtonSetting("Align right", true));
        this.registerSetting(alphabeticalSort = new ButtonSetting("Alphabetical sort", false));
        this.registerSetting(dropShadow = new ButtonSetting("Drop shadow", false));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false));
        this.registerSetting(showInfo = new ButtonSetting("Show module info", false));
      //  this.registerSetting(colorout = new ButtonSetting("Color Outline", false));
        this.registerSetting(bloomType = new SliderSetting("Bloom Type", blooms, 0));
        this.registerSetting(blurss = new SliderSetting("Blur Type", blurs, 0));
        this.registerSetting(bgss = new SliderSetting("Background Type", bgs, 0));
        this.registerSetting(cbgss = new SliderSetting("Color Type", cbgs, 0));
        this.registerSetting(outliness = new SliderSetting("Sidebar Type", outlines, 0));
      //  this.registerSetting(round = new SliderSetting("Roundedness", 0, 0.0, 8, 1));
    }


    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) return;
        if (isAlphabeticalSort != alphabeticalSort.isToggled()) {
            isAlphabeticalSort = alphabeticalSort.isToggled();
            ModuleManager.sort();
        }
        if (canShowInfo != showInfo.isToggled()) {
            canShowInfo = showInfo.isToggled();
            ModuleManager.sort();
        }
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) return;

        int n = hudY;
        double n2 = 0.0;


        for (Module module : ModuleManager.organizedModules) {
            if (module.isEnabled() && module != this) {
                if (module.isHidden() || module == ModuleManager.commandLine) continue;
                String moduleName = module.getName() ;
                String moduleInfo = " §7" + module.getInfo()  ;

                if (fonts.getInput() == 0) {
                    MinecraftFontRenderer font = MinecraftFontRenderer.INSTANCE;


                    {
                        String text = showInfo.isToggled() ? moduleName + moduleInfo : moduleName;
                        if (lowercase.isToggled()) {
                            text = moduleName.toLowerCase();
                        }
                        if (!lowercase.isToggled()) {
                            text = moduleName;
                        }
                        int e = Theme.getGradient((int) theme.getInput(), n2);
                        if (theme.getInput() == 0) {
                            n2 -= 120;
                        } else {
                            n2 -= 12;
                        }
                        double n3 = hudX;
                        double width = font.width(text) + 1;
                        if (alignRight.isToggled()) {
                            n3 -= width + 2;
                        }
                        if (bloomType.getInput() == 1) {
                            BlurUtils.prepareBlur();
                            BlurUtils.prepareBloom();
                            RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                            BlurUtils.bloomEnd(2, 2F);
                            BlurUtils.blurEnd(2, 0.75F);
                        }
                        if (bloomType.getInput() == 2) {
  //                          BlurUtils.prepareBlur();
                            BlurUtils.prepareBloom();
                            RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Theme.getGradient((int) theme.getInput(), 0.0));
                            BlurUtils.bloomEnd(4, 2F);
 //                           BlurUtils.blurEnd(2, 0.75F);
                        }
                        switch ((int) blurss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                BlurUtils.prepareBlur();
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                BlurUtils.blurEnd(2, 0.25F);
                                break;
                            case 2:
                                BlurUtils.prepareBlur();
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                BlurUtils.blurEnd(2, 0.75F);
                                break;
                            case 3:
                                BlurUtils.prepareBlur();
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                BlurUtils.blurEnd(2, 1.5F);
                                break;
                        }
                        switch ((int) bgss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, new Color(0, 0, 0, 64).getRGB());
                                break;
                            case 2:
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, new Color(0, 0, 0, 128).getRGB());
                                break;
                            case 3:
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, new Color(32, 32, 36, 255).getRGB());
                                break;
                        }
                        switch ((int) cbgss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 32));
                                break;
                            case 2:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 92));
                                break;
                            case 3:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 255));
                                break;
                        }
                        switch ((int) outliness.getInput()) {
                            case 0:
                                break;
                            case 1:
                                RenderUtils.drawRect(alignRight.isToggled() ? n3 + width : n3 + 1, n - 1, alignRight.isToggled() ? n3 + width + 2 : n3 - 1, n + (float) Math.floor(font.height() + 1), Theme.getGradient((int) theme.getInput(), 0.0));
                                break;
                            case 2:
                                RoundedUtils.drawRound((float) (alignRight.isToggled() ? n3 + (width + 3) : n3 - 2), (float) n, (float) ((alignRight.isToggled() ? n3 + width - 1 : n3 - 1) - (alignRight.isToggled() ? n3 + width - 2 : n3 - 2)), (float) Math.floor(font.height() + 1), 1f, false, Theme.getGradient((int) theme.getInput(), 0.0));
                                break;


                            //        case 3:
                            //            RenderUtils.drawOutline((float) (n3 - 1), n - 1, (float) ((n3 - 1) + width + 3), (float) Math.floor(font.height() + 1) + (n - 1), 2, new Color(32, 32, 36, 255).getRGB());
                            //            break;
                        }
                        font.drawString(text, n3, n, e, dropShadow.isToggled());
                        if (fonts.getInput() != 5) {
                            n += (int) (font.height() + 2);
                        } else {
                            n += (int) (font.height() + 3);
                        }
                      }
                    } else {
                        keystrokesmod.utility.font.impl.FontRenderer font = FontManager.helveticaNeue;
                        switch ((int) fonts.getInput()) {
                            case 2:
                                font = FontManager.productSans20;
                                break;
                            case 3:
                                font = FontManager.googleMedium20;
                                break;
                            case 4:
                                font = FontManager.sfRegular20;
                                break;
                            case 5:
                                font = FontManager.greyCliffCF20;
                                break;
                            case 6:
                                font = FontManager.productSansLight22;
                                break;
                            case 7:
                                font = FontManager.poppinsBold20;
                                break;
                            default:
                                break;
                        }
                        {
                            String text = showInfo.isToggled() ? moduleName + moduleInfo : moduleName;
                            if (lowercase.isToggled()) {
                                text = moduleName.toLowerCase();
                            }
                            if (!lowercase.isToggled()) {
                                text = moduleName;
                            }
                            int e = Theme.getGradient((int) theme.getInput(), n2);
                            if (theme.getInput() == 0) {
                                n2 -= 120;
                            } else {
                                n2 -= 12;
                            }
                            double n3 = hudX;
                            double width = font.width(text) + 1;
                            if (alignRight.isToggled()) {
                                n3 -= width + 2;
                            }
                            if (bloomType.getInput() == 1) {
     //                           BlurUtils.prepareBlur();
                                BlurUtils.prepareBloom();
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                BlurUtils.bloomEnd(2, 2F);
   //                             BlurUtils.blurEnd(2, 0.75F);
                            }
                            if (bloomType.getInput() == 2) {
   //                             BlurUtils.prepareBlur();
                                BlurUtils.prepareBloom();
                                RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Theme.getGradient((int) theme.getInput(), 0.0));
                                BlurUtils.bloomEnd(4, 2F);
   //                             BlurUtils.blurEnd(2, 0.75F);
                            }
                            switch ((int) blurss.getInput()) {
                                case 0:
                                    break;
                                case 1:
                                    BlurUtils.prepareBlur();
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                    BlurUtils.blurEnd(2, 0.25F);
                                    break;
                                case 2:
                                    BlurUtils.prepareBlur();
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                    BlurUtils.blurEnd(2, 0.75F);
                                    break;
                                case 3:
                                    BlurUtils.prepareBlur();
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, true, Color.black);
                                    BlurUtils.blurEnd(2, 1.5F);
                                    break;
                            }
                            switch ((int) bgss.getInput()) {
                                case 0:
                                    break;
                                case 1:
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, new Color(0, 0, 0, 64).getRGB());
                                    break;
                                case 2:
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, new Color(0, 0, 0, 128).getRGB());
                                    break;
                                case 3:
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, new Color(32, 32, 36, 255).getRGB());
                                    break;
                            }
                            switch ((int) cbgss.getInput()) {
                                case 0:
                                    break;
                                case 1:
                                    e = RenderUtils.toArgb(Color.WHITE, 255);
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 32));
                                    break;
                                case 2:
                                    e = RenderUtils.toArgb(Color.WHITE, 255);
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 92));
                                    break;
                                case 3:
                                    e = RenderUtils.toArgb(Color.WHITE, 255);
                                    RoundedUtils.drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), (float) Math.floor(font.height() + 1), 0, false, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 255));
                                    break;
                            }
                            switch ((int) outliness.getInput()) {
                                case 0:
                                    break;
                                case 1:
                                    RenderUtils.drawRect(alignRight.isToggled() ? n3 + width : n3 + 1, n - 1, alignRight.isToggled() ? n3 + width + 2 : n3 - 1, n + (float) Math.floor(font.height() + 1), Theme.getGradient((int) theme.getInput(), 0.0));
                                    break;
                                case 2:
                                    RoundedUtils.drawRound((float) (alignRight.isToggled() ? n3 + (width + 3) : n3 - 2), (float) n, (float) ((alignRight.isToggled() ? n3 + width - 1 : n3 - 1) - (alignRight.isToggled() ? n3 + width - 2 : n3 - 2)), (float) Math.floor(font.height() + 1), 1f, false, Theme.getGradient((int) theme.getInput(), 0.0));
                                    break;
                                //        case 3:
                                //            RenderUtils.drawOutline((float) (n3 - 1), n - 1, (float) ((n3 - 1) + width + 3), (float) Math.floor(font.height() + 1) + (n - 1), 2, new Color(32, 32, 36, 255).getRGB());
                                //            break;
                            }
                            font.drawString(text, n3, n, e, dropShadow.isToggled());
                            if (fonts.getInput() != 5) {
                                n += (int) (font.height() + 2);
                            } else {
                                n += (int) (font.height() + 2);
                            }
                        }
                    }
                }
            }
        }

    static class EditScreen extends GuiScreen {
        final String example = "ModuleExample";
        GuiButtonExt resetPosition;
        boolean dragging = false;
        int dragStartX = 0;
        int dragStartY = 0;
        int initialHudX = 0;
        int initialHudY = 0;

        public void initGui() {
            super.initGui();
            this.buttonList.add(this.resetPosition = new GuiButtonExt(1, this.width - 90, 5, 85, 20, "Reset position"));
            this.initialHudX = HUD.hudX;
            this.initialHudY = HUD.hudY;
            Keyboard.enableRepeatEvents(true);
        }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, -1308622848);

        drawModulesOrExample(mc.fontRendererObj);

        HUD.hudX = this.initialHudX;
        HUD.hudY = this.initialHudY;
        ScaledResolution res = new ScaledResolution(this.mc);
        int infoX = res.getScaledWidth() / 2 - 84;
        int infoY = res.getScaledHeight() / 2 - 20;
        RenderUtils.dct("Edit the HUD position by dragging.", '-', infoX, infoY, 2L, 0L, true, this.mc.fontRendererObj);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawModulesOrExample(FontRenderer fr) {
        int x = this.initialHudX;
        int y = this.initialHudY;
        String[] parts = example.split("-");
        for (String part : parts) {
            fr.drawString(part, x, y, Color.white.getRGB());
            y += 12;
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && isHovered(this.initialHudX, this.initialHudY, mouseX, mouseY)) {
            this.dragging = true;
            this.dragStartX = mouseX;
            this.dragStartY = mouseY;
        }
    }

    private boolean isHovered(int hudX, int hudY, int mouseX, int mouseY) {
        int hudWidth = 50;
        int hudHeight = 32;
        return mouseX >= hudX && mouseX <= hudX + hudWidth && mouseY >= hudY && mouseY <= hudY + hudHeight;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.dragging = false;
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.dragging) {

            int deltaX = Mouse.getEventX() * this.width / mc.displayWidth - this.dragStartX;
            int deltaY = this.height - Mouse.getEventY() * this.height / mc.displayHeight - 1 - this.dragStartY;

            this.initialHudX += deltaX;
            this.initialHudY += deltaY;


            this.dragStartX += deltaX;
            this.dragStartY += deltaY;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

 }
}