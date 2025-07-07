package neo.module.impl.client;

import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.render.animation.AnimationUtils;
import neo.util.render.animation.CoolDown;
import neo.util.render.animation.Timer;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Notifications extends Module {
    public static final List<Notification> notifs = new ArrayList<>();

    public static ButtonSetting chatNoti;
    public static ButtonSetting moduleToggled;
    public static SliderSetting theme;
    private final ButtonSetting sg;

    public Notifications() {
        super("Notifications", category.client);
        this.registerSetting(chatNoti = new ButtonSetting("Show in chat", false));
        this.registerSetting(moduleToggled = new ButtonSetting("Module toggled", true));
        this.registerSetting(sg = new ButtonSetting("Glow", false));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));

    }

    public static void sendNotification(NotificationTypes notificationType, String message) {
        sendNotification(notificationType, message, 3000);
    }

    public static void sendNotification(NotificationTypes notificationType, String message, long duration) {
        if (!ModuleManager.notifications.isEnabled()) return;

        if (!chatNoti.isToggled()) {
            ScaledResolution sr = new ScaledResolution(mc);
            CoolDown coolDown = new CoolDown(duration);
            coolDown.start();
            AnimationUtils animationX = new AnimationUtils(sr.getScaledWidth());
            animationX.setAnimation(sr.getScaledWidth(), 16);
            notifs.add(new Notification(notificationType,
                    message, coolDown,
                    animationX,
                    new AnimationUtils(sr.getScaledHeight() - (notifs.size() * 30))
            ));
        } else {
            Utils.sendMessage(
                    notificationType == NotificationTypes.INFO
                            ? message
                            : "&7[&1LI&7-" + (notificationType == NotificationTypes.WARN ? "&e" : "&4") + notificationType.toString() + "&7]&r " + message
            );
        }
    }

    @Override
    public void onEnable() {
        notifs.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        for (int index = 0; index < notifs.size(); index++) {
            Notification noti = notifs.get(index);
            noti.animationY.setAnimation(sr.getScaledHeight() - ((index + 1) * 30), 15);
            renderNotification(noti);
            if (noti.duration.hasFinished()) {
                notifs.remove(index);
                index--;
            } else if (noti.duration.getTimeLeft() < 500) {
                noti.animationX.setAnimation(sr.getScaledWidth(), 16);
            } else {
                noti.animationX.setAnimation(sr.getScaledWidth() - 125, 16);
            }
        }
    }

    private void renderNotification(Notification notification) {
        FontRenderer font = FontManager.productSans20;
        float xPos = (float) notification.animationX.getValue();
        float yPos = (float) notification.animationY.getValue();

        RoundedUtils.drawRound(xPos, yPos, 120, 20, 4, RenderUtils.toARGBInt(new Color(0, 0, 0, 128)));

        if (notification.timer.last == 0) {
            notification.timer.start();
        }

        float progress = notification.timer.getValueFloat(0f, 120f, 4);
        if (progress > 120f) progress = 120f;

        if (sg.isToggled()) BlurUtils.prepareBloom();
        RoundedUtils.drawRound(xPos, yPos, progress, 20, 4,
                Theme.getGradient((int) theme.getInput(), 0));
        if (sg.isToggled()) BlurUtils.bloomEnd(2, 2F);

        if (sg.isToggled()) RoundedUtils.drawRound(xPos, yPos, progress, 20, 4,
                Theme.getGradient((int) theme.getInput(), 0));

        double textX = xPos + 11; // (120 / 2.0) - (font.width(notification.message) / 2.0);
        double textY = yPos + 11;

        font.drawString(notification.message, textX, textY - 5, Color.WHITE.getRGB(), false);
    }

    public enum NotificationTypes {
        INFO,
        WARN,
        ERROR
    }

    public static class Notification {
        public final NotificationTypes type;
        public final String message;
        public final CoolDown duration;
        public final AnimationUtils animationX;
        public final AnimationUtils animationY;
        public final Timer timer = new Timer(3000);

        public Notification(NotificationTypes type, String message, CoolDown duration, AnimationUtils animationX, AnimationUtils animationY) {
            this.type = type;
            this.message = message;
            this.duration = duration;
            this.animationX = animationX;
            this.animationY = animationY;
        }
    }
}
