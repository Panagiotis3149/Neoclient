package neo.gui.altmgr;

import neo.gui.menu.other.CustomButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import neo.util.shader.BackgroundShader;
import java.io.IOException;

public class AddAccount extends GuiScreen {
    private static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/mainmenu.frag");


    private final GuiScreen parent;
    private BackgroundShader shader;

    public AddAccount(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int buttonY = (this.height / 2) - 20;
        int centerX = this.width / 2 - 100;

        this.buttonList.add(new CustomButton(1, centerX,  buttonY + 00, "Cracked Login"));
        this.buttonList.add(new CustomButton(2, centerX,  buttonY + 24, "Credentials Login"));
        this.buttonList.add(new CustomButton(3, centerX,  buttonY + 48, "Microsoft Login"));
        this.buttonList.add(new CustomButton(4, centerX,  buttonY + 72, "Cookie Login"));
        this.buttonList.add(new CustomButton(5, centerX,  buttonY + 96, "Token Login"));

        this.buttonList.add(new CustomButton(0, this.width / 2 - 100, this.height - 30, "Back"));

        shader = new BackgroundShader(FRAG_SHADER);
        try {
            shader.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        shader.use(partialTicks);

        // Draw buttons
        for (GuiButton btn : buttonList) {
            btn.drawButton(this.mc, mouseX, mouseY);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                this.mc.displayGuiScreen(new CrackedLogin(this));
                break;
            case 2:
                this.mc.displayGuiScreen(new CredentialLogin(this));
                break;
            case 3:
                this.mc.displayGuiScreen(new MicrosoftLogin(this));
                break;
            case 4:
                this.mc.displayGuiScreen(new CookieLogin(this));
                break;
            case 5:
                this.mc.displayGuiScreen(new TokenLogin(this));
                break;
            case 0:
                this.mc.displayGuiScreen(parent);
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        shader.cleanUp();
    }
}