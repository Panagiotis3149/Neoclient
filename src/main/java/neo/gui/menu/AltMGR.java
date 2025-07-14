package neo.gui.menu;

import neo.NeoCloud;
import neo.Variables;
import neo.gui.altmgr.AccountComponent;
import neo.gui.altmgr.AddAccount;
import neo.gui.altmgr.AltFile;
import neo.gui.altmgr.AltManager;
import neo.gui.menu.other.CustomButton;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.shader.BackgroundShader;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.ArrayList;

public class AltMGR extends GuiScreen {
    private int scrollY = 0;
    private int maxScrollY = 0;
    public final ArrayList<AccountComponent> accounts = new ArrayList<>();
    private static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/mainmenu.frag");
    private final GuiScreen parentScreen;
    private BackgroundShader shader;
    public static AltManager altManager = new AltManager();


    public AltMGR(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;

    }

    public void refreshGui() {
        initGui();
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;

        this.buttonList.add(new CustomButton(1, centerX - 225, this.height - 30, "Add"));
        this.buttonList.add(new CustomButton(2, centerX + 25, this.height - 30, "Back"));

        shader = new BackgroundShader(FRAG_SHADER);
        try {
            shader.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        accounts.clear();

        int startX = 10;
        int startY = 40;
        int x = startX;
        int y = startY;

        int spacingX = 5;
        int spacingY = 10;

        int maxWidth = new ScaledResolution(mc).getScaledWidth();

        for (AltFile.Account acc : altManager.getAccounts()) {
            AccountComponent comp = new AccountComponent(this, acc.name, acc.type, acc.uuid, 0, 0);

            if (x + comp.width > maxWidth - spacingX) {
                x = startX;
                y += comp.height + spacingY;
            }

            comp.x = x;
            comp.y = y;

            accounts.add(comp);

            x += comp.width + spacingX;
        }

        int totalHeight = y + 50;
        int screenHeight = new ScaledResolution(mc).getScaledHeight();
        maxScrollY = Math.max(0, totalHeight - screenHeight + 10);
        scrollY = 0;
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        shader.use(partialTicks);

        if (NeoCloud.onlineVersion != null && !NeoCloud.onlineVersion.isEmpty() && Variables.OUTDATED) {
            FontRenderer font = FontManager.productSans20;
            int x = (int) (((double) this.width / 2) - (font.width("Neo is outdated! Update now: " + NeoCloud.onlineVersion) / 2));
            font.drawString("Neo is outdated! Update now: " + NeoCloud.onlineVersion, x, 10, 0xFFFFFFFF, false);
        }


        for (AccountComponent account : accounts) {
            account.render(mc, mouseX, mouseY - scrollY);
        }

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            scrollY -= Math.round((wheel / 120f) * 10);
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScrollY) scrollY = maxScrollY;
        }
    }


    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            this.mc.displayGuiScreen(new AddAccount(this));
        } else if (button.id == 2) {
            this.mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        shader.cleanUp();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (AccountComponent account : new ArrayList<>(accounts)) {
            account.mouseClicked(mouseX, mouseY - scrollY, mouseButton);
        }
    }

}