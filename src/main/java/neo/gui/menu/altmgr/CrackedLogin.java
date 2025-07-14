package neo.gui.menu.altmgr;

import neo.gui.menu.AltMGR;
import neo.gui.menu.other.CustomButton;
import neo.gui.menu.other.CustomTextField;
import neo.gui.menu.other.SmallButton;
import neo.util.account.AccountUtils;
import neo.util.account.UsernameGenerator;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import neo.util.shader.BackgroundShader;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class CrackedLogin extends GuiScreen {

    private static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/mainmenu.frag");
    private BackgroundShader bgShader;
    private CustomTextField usernameField;
    private final GuiScreen parent;
    String status = "Enter Username";

    public CrackedLogin(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        if (bgShader != null) {
            bgShader.cleanUp(); // nuke old shader program and GL state
        }

        bgShader = new BackgroundShader(FRAG_SHADER);
        try {
            bgShader.init(); // fresh compile every time the GUI opens
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

            FontRenderer font = FontManager.productSansMedium24;

            usernameField = new CustomTextField(width / 2 - 100, height / 2 - 10, 200, 40, font);
            usernameField.setMaxLength(16);
            usernameField.setText("Player" + (int) (Math.random() * 9999));
            usernameField.setFocused(true);

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.buttonList.add(new CustomButton(1, centerX + 110, centerY + 3, "Generate Random"));

            this.buttonList.add(new SmallButton(2, centerX - 112, centerY + 45, "Login")); // left button
            this.buttonList.add(new SmallButton(3, centerX + 12, centerY + 45, "Add"));    // right button


            this.buttonList.add(new CustomButton(0, centerX - 100, this.height - 30, "Back"));
        }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        bgShader.use(partialTicks);

        usernameField.drawTextBox();
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer font = FontManager.productSansMedium24;
        font.drawString(status, ((double) sr.getScaledWidth() / 2) - (font.width(status) / 2), ((double) sr.getScaledHeight() / 2) - 50, 0xFFFFFFFF);

        for (GuiButton btn : buttonList) {
            btn.drawButton(this.mc, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                usernameField.setText(UsernameGenerator.generateRandomUsername());
                break;
            case 2:
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    AccountUtils.crackedLogin(username);
                    this.mc.displayGuiScreen(parent);
                } else {
                    status = "Username Empty!";
                }
                break;
            case 3:
                String user = usernameField.getText().trim();
                if (!user.isEmpty()) {
                    AltMGR.altManager.addAccount(new AltFile.Account("Cracked", user, AccountUtils.getUUID(user), "accessToken", System.currentTimeMillis()));
                    AccountUtils.crackedLogin(user);
                    this.mc.displayGuiScreen(parent);
                } else {
                    status = "Username Empty!";
                }
                break;
            case 0:
                this.mc.displayGuiScreen(parent);
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        try {
            super.keyTyped(typedChar, keyCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        usernameField.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        usernameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        bgShader.cleanUp();
    }
}
