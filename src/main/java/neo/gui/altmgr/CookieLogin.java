package neo.gui.altmgr;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import neo.gui.menu.AltMGR;
import neo.gui.menu.other.CustomButton;
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
import java.util.function.Consumer;

public class CookieLogin extends GuiScreen {

    private static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/background.frag");
    private BackgroundShader bgShader;
    private final GuiScreen parent;
    String status = "Webview Login";
    private MicrosoftAuthResult account = null;

    Consumer<MicrosoftAuthResult> success = result -> {
        mc.addScheduledTask(() -> {
            status = ("Account: " + result.getProfile().getName());
            account = result;
        });
    };

    Consumer<String> error = errMsg -> {
        mc.addScheduledTask(() -> {
            status = errMsg;
            System.out.println(errMsg);
        });
    };

    public CookieLogin(GuiScreen parent) {
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



        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.add(new SmallButton(1, centerX - 112, centerY + 45, "Login"));
        this.buttonList.add(new SmallButton(2, centerX + 12, centerY + 45, "Add"));
        this.buttonList.add(new CustomButton(3, centerX - (160 / 2), centerY - 10, "Select File"));

        this.buttonList.add(new CustomButton(0, centerX - 100, this.height - 30, "Back"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        bgShader.use(partialTicks);

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
                if (account != null) {
                    AccountUtils.loginWithMicrosoftAuthResult(account);
                    this.mc.displayGuiScreen(parent);
                }
                break;
            case 2:
                if (account != null) {
                    AccountUtils.loginWithMicrosoftAuthResult(account);
                    MinecraftProfile prof = account.getProfile();
                    AltMGR.altManager.addAccount(new AltFile.Account("Cookie", prof.getName(), prof.getId(), account.getAccessToken(), System.currentTimeMillis(), account.getRefreshToken()));
                    this.mc.displayGuiScreen(parent);
                }
                break;
            case 3:
                    AccountUtils.cookieLogin(success, error);
                break;
            case 0:
                this.mc.displayGuiScreen(parent);
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        bgShader.cleanUp();
    }
}
