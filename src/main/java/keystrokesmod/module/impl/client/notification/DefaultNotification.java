package keystrokesmod.module.impl.client.notification;

import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.font.CenterMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.ColorUtils;
import keystrokesmod.utility.RRectUtils;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DefaultNotification extends SubMode<Notifications> implements INotification {
    private final SliderSetting font;

    public DefaultNotification(String name, @NotNull Notifications parent) {
        super(name, parent);
        this.registerSetting(font = new SliderSetting("Font", new String[]{"Minecraft", "Regular", "Product Sans", "Tenacity", "HelveticaNeue"}, 1));
    }

    private IFont getFont() {
        switch ((int) font.getInput()) {
            case 0:
                return FontManager.getMinecraft();
            default:
            case 1:
                return FontManager.regular20;
            case 2:
                return FontManager.productSans20;
            case 3:
                return FontManager.tenacity20;
            case 4:
                return FontManager.helveticaNeue;
        }
    }

    @Override
    public void render(Notifications.@NotNull Notification notification) {
        if (!Notifications.blur.isToggled()) {
            RRectUtils.drawRound(notification.animationX.getValue(), notification.animationY.getValue(), 120, 25, 3, new Color(0, 0, 0, 128));
        } else if (Notifications.blur.isToggled()) {
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) notification.animationX.getValue(), (float) notification.animationY.getValue(), 120, 25, 3f, true, Color.black);
            BlurUtils.blurEnd(2, 2F);
        }
        FontManager.icon24.drawString(notification.type == Notifications.NotificationTypes.INFO ? "G" : "R", notification.animationX.getValue() + 6.5, notification.animationY.getValue() + 10.5, ColorUtils.getFontColor(2).getRGB(), false);
        String[] messageParts = notification.message.split("§");
        double x = notification.animationX.getValue() + 25;
        double y = notification.animationY.getValue() + 15;
        if (messageParts.length == 1) {
            getFont().drawString(notification.message, x, y - 5, Color.WHITE.getRGB(), false);
        } else {
            for (String part : messageParts) {
                if (part.isEmpty()) continue;
                char colorCode = part.charAt(0);
                String text = part.substring(1);
                Color color = ColorUtils.getColorFromCode("§" + colorCode);
                getFont().drawString(text, x, y - 5, color.getRGB(), false);
                x += getFont().width(text);
            }
        }
    }
}