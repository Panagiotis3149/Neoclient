package neo.util.render;

import neo.util.render.animation.AnimationUtils;
import neo.util.render.animation.Timer;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    public static final List<AnimationUtils> animation = new ArrayList<>();

    static {
        for (int i = 0; i < 18; i++) {
            animation.add(new AnimationUtils(0.0F));
        }
    }
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)§([0-9A-FK-OR])");

    public static int getContrastColor(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        float brightness = (r * 0.299f + g * 0.587f + b * 0.114f) / 255f;
        return brightness > 0.75f ? 0xFF000000 : 0xFFFFFFFF;
    }

    public static int getEasedContrastColor(int argb, Timer timer, int easingType) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        float brightness = (r * 0.299f + g * 0.587f + b * 0.114f) / 255f;

        float eased = timer.getValueFloat(0f, 1f, easingType);
        float threshold = 0.75f;

        return (brightness * eased) > threshold ? 0xFF000000 : 0xFFFFFFFF;
    }


    public static int getContrastColor(Color color) {
        float brightness = (color.getRed() * 0.299f + color.getGreen() * 0.587f + color.getBlue() * 0.114f) / 255f;
        return brightness > 0.75f ? 0xFF000000 : 0xFFFFFFFF;
    }


    public static Color getFontColor(int id, int alpha) {
        Color rawColor = getRawFontColor(id);
        int speed = 12;

        if (id == 1) {
            animation.get(12).setAnimation(rawColor.getRed(), speed);
            animation.get(13).setAnimation(rawColor.getGreen(), speed);
            animation.get(14).setAnimation(rawColor.getBlue(), speed);

            return new Color((int) animation.get(12).getValue(), (int) animation.get(13).getValue(), (int) animation.get(14).getValue(), alpha);
        }

        if (id == 2) {
            animation.get(15).setAnimation(rawColor.getRed(), speed);
            animation.get(16).setAnimation(rawColor.getGreen(), speed);
            animation.get(17).setAnimation(rawColor.getBlue(), speed);

            return new Color((int) animation.get(15).getValue(), (int) animation.get(16).getValue(), (int) animation.get(17).getValue(), alpha);
        }

        return rawColor;
    }

    private static Color getRawFontColor(int id) {
        switch (id) {
            case 1:
                return new Color(255, 255, 255);
            case 2:
                return new Color(173, 173, 173);
            default:
                return new Color(255, 0, 0);
        }
    }

    public static Color getFontColor(int id) {
        return getFontColor(id, 255);
    }

    public static void setColor(int color, double alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, (float) alpha);
    }

    public static void setColor(int color) {
        setColor(color, (color >> 24 & 255) / 255.0F);
    }

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }
    public static Color getColorFromCode(String input) {
        Matcher matcher = COLOR_CODE_PATTERN.matcher(input);
        if (matcher.find()) {
            char code = matcher.group(1).charAt(0);
            switch (code) {
                case '0': return new Color(0, 0, 0);
                case '1': return new Color(0, 0, 170);
                case '2': return new Color(0, 170, 0);
                case '3': return new Color(0, 170, 170);
                case '4': return new Color(170, 0, 0);
                case '5': return new Color(170, 0, 170);
                case '6': return new Color(255, 170, 0);
                case '7': return new Color(170, 170, 170);
                case '8': return new Color(85, 85, 85);
                case '9': return new Color(85, 85, 255);
                case 'a': return new Color(85, 255, 85);
                case 'b': return new Color(85, 255, 255);
                case 'c': return new Color(255, 85, 85);
                case 'd': return new Color(255, 85, 255);
                case 'e': return new Color(255, 255, 85);
                case 'f': return new Color(255, 255, 255);
                default: return new Color(255, 255, 255);
            }
        }
        return new Color(255, 255, 255);
    }


    public static int interpolateInt(final int oldValue, final int newValue, final double interpolationValue) {
        return ((Double) ((double) oldValue + ((double) newValue - (double) oldValue) * (double) (float) interpolationValue)).intValue();
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Color blend(@NotNull Color color1, @NotNull Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0f - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        return new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
    }

}