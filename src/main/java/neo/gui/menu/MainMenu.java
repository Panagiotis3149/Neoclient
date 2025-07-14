package neo.gui.menu;

import neo.NeoCloud;
import neo.Variables;
import neo.gui.menu.other.CustomButton;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.shader.BackgroundShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;


public class MainMenu extends GuiScreen {

    public static final ResourceLocation CUSTOM_LOGO = new ResourceLocation("neo", "textures/gui/Logo.png");
    public static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/mainmenu.frag");

    private static final float SCALE_FACTOR = 0.25f;
    private static final int BUTTON_OFFSET_Y = 130;

    private BackgroundShader shader;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int buttonY = (int) (this.height * SCALE_FACTOR) + BUTTON_OFFSET_Y;

        this.buttonList.add(new CustomButton(1, this.width / 2 - 100 + 2, buttonY, I18n.format("menu.singleplayer")));
        this.buttonList.add(new CustomButton(2, this.width / 2 - 100 + 2, buttonY + 24, I18n.format("menu.multiplayer")));
        this.buttonList.add(new CustomButton(5, this.width / 2 - 100 + 2, buttonY + 48, "Alt Manager"));
        this.buttonList.add(new CustomButton(0, this.width / 2 - 100 + 2, buttonY + 72, I18n.format("menu.options")));
        this.buttonList.add(new CustomButton(4, this.width / 2 - 100 + 2, buttonY + 96, I18n.format("menu.quit")));

        shader = new BackgroundShader(FRAG_SHADER);
        try {
            shader.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        shader.use(partialTicks);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);

        ScaledResolution sr = new ScaledResolution(mc);
        GL11.glViewport(0, 0, sr.getScaledWidth() * sr.getScaleFactor(), sr.getScaledHeight() * sr.getScaleFactor());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(CUSTOM_LOGO);

        int logoWidth = 312;
        int logoHeight = 312;
        int logoX = (this.width - logoWidth) / 2 - 10;
        int logoY = (int) (this.height * SCALE_FACTOR) - 115;
        drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }

        if (NeoCloud.onlineVersion != null && !NeoCloud.onlineVersion.isEmpty() && Variables.OUTDATED) {
            FontRenderer font = FontManager.productSans20;
            int x = (int) (this.width / 2 - font.width("Neo is outdated! Online version: " + NeoCloud.onlinePretty + ", update now!") / 2);
            font.drawString("Neo is outdated! Online version: " + NeoCloud.onlinePretty + ", update now!", x, 22, 0xFFFFFFFF, false);
        }
    }


    @Override
    public void onGuiClosed() {
        shader.cleanUp();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case 2:
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case 4:
                this.mc.shutdown();
                break;
            case 5:
                this.mc.displayGuiScreen(new AltMGR(this));
                break;
        }
    }
}