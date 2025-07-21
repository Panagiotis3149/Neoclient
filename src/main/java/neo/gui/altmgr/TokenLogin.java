package neo.gui.altmgr;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import neo.gui.menu.AltMGR;
import neo.gui.menu.other.CustomButton;
import neo.gui.menu.other.CustomTextField;
import neo.gui.menu.other.SmallButton;
import neo.util.account.AccountUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import neo.util.shader.BackgroundShader;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class TokenLogin extends GuiScreen {

    private static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/background.frag");
    private BackgroundShader bgShader;
    private CustomTextField TokenField;
    private final GuiScreen parent;
    String status = "Enter Refresh Token";

    public TokenLogin(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        if (bgShader != null) {
            bgShader.cleanUp();
        }

        bgShader = new BackgroundShader(FRAG_SHADER);
        try {
            bgShader.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FontRenderer font = FontManager.productSansMedium24;

        TokenField = new CustomTextField(width / 2 - 100, height / 2 - 10, 200, 40, font);
        TokenField.setText("");
        TokenField.setFocused(true);

        int centerX = this.width / 2;
        int centerY = this.height / 2;


        this.buttonList.add(new SmallButton(2, centerX - 112, centerY + 45, "Login"));
        this.buttonList.add(new SmallButton(3, centerX + 12, centerY + 45, "Add"));


        this.buttonList.add(new CustomButton(0, centerX - 100, this.height - 30, "Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        bgShader.use(partialTicks);

        TokenField.drawTextBox();
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
            case 2:
                String tkns = TokenField.getText();
                if (!tkns.isEmpty()) {
                    MicrosoftAuthResult authResult = null;

                    try {
                        authResult = AccountUtils.authenticateWithToken(AccountUtils.getTokens(tkns));
                    } catch (Exception e) {
                        if (e.getMessage() != null || e.getMessage() != "") {
                            status = e.getMessage();
                            System.out.println(status);
                        } else {
                            status = "An unknown exception occurred.";
                            System.out.println(status);
                        }
                    }
                    if (authResult == null) {
                        status = "Null AuthResult";
                        System.out.println(status);
                        return;
                    }
                    AccountUtils.loginWithMicrosoftAuthResult(authResult);
                    this.mc.displayGuiScreen(parent);
                } else {
                    status = "Tokens Empty!";
                    System.out.println(status);
                }
                break;
            case 3:
                String tkn = TokenField.getText();
                if (!tkn.isEmpty()) {
                    MicrosoftAuthResult authResult = null;

                    try {
                        authResult = AccountUtils.authenticateWithToken(AccountUtils.getTokens(tkn));
                    } catch (Exception e) {
                        status = e.getMessage();
                    }
                    if (authResult == null) {
                        status = "Null AuthResult";
                        return;
                    }
                    AltMGR.altManager.addAccount(new AltFile.Account("Tokens", authResult.getProfile().getName(),authResult.getProfile().getId(),authResult.getAccessToken(), System.currentTimeMillis(), authResult.getRefreshToken()));
                    AccountUtils.loginWithMicrosoftAuthResult(authResult);
                    this.mc.displayGuiScreen(parent);
                } else {
                    status = "Tokens Empty!";
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
        TokenField.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TokenField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        bgShader.cleanUp();
    }
}
