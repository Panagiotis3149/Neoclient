package neo.gui.menu.altmgr;


import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import neo.gui.menu.AltMGR;
import neo.util.account.AccountUtils;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.render.Theme;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.Objects;

public class AccountComponent {
    public String name;
    public String type;
    public String uuid;
    private final AltMGR parentGui;

    public int x, y, width = 130, height = 50;

    private final Runnable onLogin;
    private final Runnable onDelete;

    public AccountComponent(AltMGR parentGui, String name, String type, String uuid, int x, int y) {
        this.parentGui = parentGui;
        this.name = name;
        this.type = type;
        this.uuid = uuid;
        this.x = x;
        this.y = y;

        this.onLogin = new Runnable() {
            public void run() {
                if (Objects.equals(type, "Cracked")) {
                    AccountUtils.crackedLogin(name);
                } else {
                    try {
                        AccountUtils.authenticateWithRefreshToken(AltMGR.altManager.getRefreshTokenByUUID(uuid));
                    } catch (MicrosoftAuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        this.onDelete = () -> {
            if (AltMGR.altManager.deleteAccountByUUID(uuid)) {
                System.out.println("Deleted alt: " + name);
                parentGui.accounts.removeIf(acc -> acc.uuid.equals(uuid)); // remove from UI list
                parentGui.refreshGui(); // refresh UI to update positions etc
            } else {
                System.out.println("Failed to delete alt: " + name);
            }
        };



    }




    public void render(Minecraft mc, int mouseX, int mouseY) {


        RoundedUtils.drawRound(x, y, width, height, 4, 0x4C000000);

        BlurUtils.prepareBlur();
        RoundedUtils.drawRound(x, y, width, height, 4, Color.BLACK);
        BlurUtils.blurEnd(2, 0.65f);

        FontRenderer fn = FontManager.productSansMedium24;
        FontRenderer font = FontManager.productSans16;

        fn.drawString(name, x + 2, y + 2, 0xFFFFFFFF, false);
        font.drawString(type, x + 2, y + 18, 0xFFCCCCCC, false);

        int loginX = x + width - 120;
        int deleteX = x + width - 60;
        int btnY = y + height - 20;
        boolean loginHover = isHovered(loginX, btnY, 40, 14, mouseX, mouseY);
        boolean deleteHover = isHovered(deleteX, btnY, 40, 14, mouseX, mouseY);


        RoundedUtils.drawRound(loginX, btnY, 40, 14, 2, loginHover ? Theme.getGradient(26, 0) : 0xFF66BB6A);
        RoundedUtils.drawRound(deleteX, btnY, 40, 14, 2, deleteHover ? Theme.getGradient(34, 0) : 0xFFA92000);
        font.drawString("Login", loginX + (40 - font.width("Login")) / 2, btnY + (14 - font.height()) / 2,  0xFFFFFFFF, false);
        font.drawString("Delete", deleteX + (40 - font.width("Delete")) / 2, btnY + (14 - font.height()) / 2, 0xFFFFFFFF, false);
    }

    public void renderAt(Minecraft mc, int mouseX, int mouseY, int renderX, int renderY) {
        RoundedUtils.drawRound(renderX, renderY, width, height, 4, 0x4C000000);

        BlurUtils.prepareBlur();
        RoundedUtils.drawRound(renderX, renderY, width, height, 4, Color.BLACK);
        BlurUtils.blurEnd(2, 0.65f);

        FontRenderer fn = FontManager.productSansMedium24;
        FontRenderer font = FontManager.productSans16;

        fn.drawString(name, renderX + 2, renderY + 2, 0xFFFFFFFF, false);
        font.drawString(type, renderX + 2, renderY + 18, 0xFFCCCCCC, false);

        int loginX = renderX + width - 120;
        int deleteX = renderX + width - 60;
        int btnY = renderY + height - 20;
        boolean loginHover = isHovered(loginX, btnY, 40, 14, mouseX, mouseY);
        boolean deleteHover = isHovered(deleteX, btnY, 40, 14, mouseX, mouseY);

        RoundedUtils.drawRound(loginX, btnY, 40, 14, 2, loginHover ? Theme.getGradient(26, 0) : 0xFF66BB6A);
        RoundedUtils.drawRound(deleteX, btnY, 40, 14, 2, deleteHover ? Theme.getGradient(34, 0) : 0xFFA92000);
        font.drawString("Login", loginX + (40 - font.width("Login")) / 2, btnY + (14 - font.height()) / 2,  0xFFFFFFFF, false);
        font.drawString("Delete", deleteX + (40 - font.width("Delete")) / 2, btnY + (14 - font.height()) / 2, 0xFFFFFFFF, false);
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;

        int loginX = x + width - 120;
        int deleteX = x + width - 60;
        int btnY = y + height - 20;

        if (isHovered(loginX, btnY, 40, 14, mouseX, mouseY)) {
            onLogin.run();
        } else if (isHovered(deleteX, btnY, 40, 14, mouseX, mouseY)) {
            onDelete.run();
        }
    }

    private boolean isHovered(int btnX, int btnY, int btnWidth, int btnHeight, int mouseX, int mouseY) {
        return mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight;
    }
}

