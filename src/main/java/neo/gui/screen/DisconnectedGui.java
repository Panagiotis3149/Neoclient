package neo.gui.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neo.gui.menu.other.CustomButton;
import neo.util.account.AccountUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import neo.util.shader.BackgroundShader;
import net.minecraft.util.ResourceLocation;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;

@SideOnly(Side.CLIENT)
public class DisconnectedGui extends GuiScreen {
    private static final ResourceLocation FRAG_SHADER = new ResourceLocation("neo", "shaders/background.frag");
    private BackgroundShader shader;

    private final String reason;
    private final IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private double someDouble;
    private static final FontRenderer font = FontManager.productSansMedium24;

    public DisconnectedGui(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp) {
        parentScreen = screen;
        reason = I18n.format(reasonLocalizationKey);
        message = chatComp;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        multilineMessage = wrapText(message.getFormattedText(), width - 50);
        someDouble = (multilineMessage.size() * font.height());

        buttonList.add(new CustomButton(0
                , width / 2 - (160 / 2), (int) ((double) height / 2 + someDouble / 2 + font.height()), I18n.format("gui.toMenu")));
        buttonList.add(new CustomButton(1
                , width / 2 - (160 / 2), (int) ((double) height / 2 + someDouble / 2 + font.height()) + 24, "Set Random Name"));


        shader = new BackgroundShader(FRAG_SHADER);
        try {
            shader.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) mc.displayGuiScreen(parentScreen);
        if (button.id == 1) {
            AccountUtils.crackedLogin("Player_" + Math.random() * 99999);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        shader.use(partialTicks);



        double y = (double) height / 2 - someDouble / 2;
        if (multilineMessage != null) {
            for (String s : multilineMessage) {
                font.drawCenteredString(s, (double) width / 2, y, 0xFFFFFF);
                y += font.height();
            }
        }


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        shader.cleanUp();
        super.onGuiClosed();
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] rawLines = text.split("\n");

        for (String raw : rawLines) {
            String[] words = raw.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String attempt = line.length() == 0 ? word : line + " " + word;
                if (font.getStringWidth(attempt) <= maxWidth) {
                    line = new StringBuilder(attempt);
                } else {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                }
            }

            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }

        return lines;
    }

}
