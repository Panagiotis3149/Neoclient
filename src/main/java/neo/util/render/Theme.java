package neo.util.render;

import neo.module.impl.client.Settings;
import neo.util.Utils;

import java.awt.*;

import static neo.util.render.RenderUtil.interpolateColorC;

public enum Theme {
    Rainbow(null, null), // 0
    Cherry(new Color(255, 200, 200), new Color(243, 58, 106)), // 1
    Cotton_candy(new Color(99, 249, 255), new Color(255, 104, 204)), // 2
    Flare(new Color(231, 39, 24), new Color(245, 173, 49)), // 3
    Flower(new Color(215, 166, 231), new Color(211, 90, 232)), // 4
    Gold(new Color(255, 215, 0), new Color(240, 159, 0)), // 5
    Grayscale(new Color(240, 240, 240), new Color(110, 110, 110)), // 6
    Royal(new Color(125, 204, 241), new Color(30, 71, 170)), // 7
    Sky(new Color(160, 230, 225), new Color(15, 190, 220)), // 8
    Vine(new Color(17, 192, 45), new Color(201, 234, 198)), // 9
    Steelvoid(new Color(55, 73, 98), new Color(125, 170, 223)), // 10
    Mist(new Color(94, 228, 154), new Color(40, 139, 207)), // 11
    America(new Color(0, 40, 123), new Color(188, 11, 43)), // 12
    Neo(new Color(71, 120, 225), new Color(97, 93, 178)), // 13
    Nord(new Color(112, 193, 158), new Color(186, 243, 202, 255)), // 14
    Coral(new Color(244, 168, 150), new Color(52, 133, 151)); // 15 - RISE SKID


    private final Color firstGradient;
    private final Color secondGradient;
    public static Color[] descriptor = new Color[]{new Color(95, 235, 255), new Color(68, 102, 250)};
    public static Color[] hiddenBind = new Color[]{new Color(245, 33, 33), new Color(229, 21, 98)};

    Theme(Color firstGradient, Color secondGradient) {
        this.firstGradient = firstGradient;
        this.secondGradient = secondGradient;
    }

    public static int getGradient(int index, double delay) {
        if (index > 0) {
            return convert(values()[index].firstGradient, values()[index].secondGradient, (Math.sin(System.currentTimeMillis() / 1.0E8 * Settings.timeMultiplier.getInput() * 400000.0 + delay * Settings.offset.getInput()) + 1.0) * 0.5).getRGB();
        }
        else if (index == 0) {
            return Utils.getChroma(2, (long) delay);
        }
        return -1;
    }

    public static int getGradient(Color firstGradient, Color secondGradient, double delay) {
        return convert(firstGradient, secondGradient, (Math.sin(System.currentTimeMillis() / 1.0E8 * 0.5 * 400000.0 + delay * 0.550000011920929) + 1.0) * 0.5).getRGB();
    }

    public static int[] getColors(int selectedTheme) {
        Color firstGradient = null;
        Color secondGradient = null;

        if (selectedTheme == 0) { // Rainbow
            firstGradient = null;
            secondGradient = null;
        } else if (selectedTheme == 1) { // Cherry
            firstGradient = new Color(255, 200, 200);
            secondGradient = new Color(243, 58, 106);
        } else if (selectedTheme == 2) { // Cotton Candy
            firstGradient = new Color(99, 249, 255);
            secondGradient = new Color(255, 104, 204);
        } else if (selectedTheme == 3) { // Flare
            firstGradient = new Color(231, 39, 24);
            secondGradient = new Color(245, 173, 49);
        } else if (selectedTheme == 4) { // Flower
            firstGradient = new Color(215, 166, 231);
            secondGradient = new Color(211, 90, 232);
        } else if (selectedTheme == 5) { // Gold
            firstGradient = new Color(255, 215, 0);
            secondGradient = new Color(240, 159, 0);
        } else if (selectedTheme == 6) { // Grayscale
            firstGradient = new Color(240, 240, 240);
            secondGradient = new Color(110, 110, 110);
        } else if (selectedTheme == 7) { // Royal
            firstGradient = new Color(125, 204, 241);
            secondGradient = new Color(30, 71, 170);
        } else if (selectedTheme == 8) { // Sky
            firstGradient = new Color(160, 230, 225);
            secondGradient = new Color(15, 190, 220);
        } else if (selectedTheme == 9) { // Vine
            firstGradient = new Color(17, 192, 45);
            secondGradient = new Color(201, 234, 198);
        } else if (selectedTheme == 10) { // Steelvoid
            firstGradient = new Color(47, 64, 84);
            secondGradient = new Color(125, 179, 223);
        } else if (selectedTheme == 11) {
            firstGradient = new Color(94, 228, 154);
            secondGradient = new Color(40, 139, 207);
        } else if (selectedTheme == 12) { // America
        firstGradient = new Color(49, 102, 184);
        secondGradient = new Color(251, 51, 75);
    } else if (selectedTheme == 13) { // Neo
        firstGradient = new Color(71, 120, 225);
        secondGradient = new Color(97, 93, 178);
    } else if (selectedTheme == 14) { // Nord
        firstGradient = new Color(112, 193, 158);
        secondGradient = new Color(186, 243, 202);
    } else if (selectedTheme == 15) { // Coral
            firstGradient = new Color(244, 168, 150);
            secondGradient = new Color(52, 133, 151);
        }


    int firstColor = firstGradient != null ? 0xFF000000 | firstGradient.getRGB() : Utils.getChroma(2, 0);
        int secondColor = secondGradient != null ? 0xFF000000 | secondGradient.getRGB() : Utils.getChroma(2, 0);

        return new int[]{firstColor, secondColor};
    }

    public static Color convert(Color color, Color color2, double n) {
        double n2 = 1.0 - n;
        return new Color((int) (color.getRed() * n + color2.getRed() * n2), (int) (color.getGreen() * n + color2.getGreen() * n2), (int) (color.getBlue() * n + color2.getBlue() * n2));
    }

    public static double convert2double(Color color) {
        if (color == null) return 0.0;
        int argb = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        return Double.longBitsToDouble(argb & 0xFFFFFFFFL);
    }

    public static int[] getGradients(int index) {
        Theme[] values = values();
        if (values != null && index >= 0 && index < values.length && values[index] != null) {
            Color firstGradient = values[index].firstGradient;
            Color secondGradient = values[index].secondGradient;
            if (firstGradient != null && secondGradient != null) {
                return new int[]{firstGradient.getRGB(), secondGradient.getRGB()};
            } else {
                return new int[]{Utils.getChroma(2, 0), Utils.getChroma(2, 0)};
            }
        }
        return new int[]{0, 0};
    }

    public static String mCCC(String text, Color firstColor, Color secondColor,float speed) {
        StringBuilder gradientText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {

            float progress = (float) i / text.length();
            Color color = interpolateColorC(firstColor, secondColor, progress);

            if (speed > 0) {
                int r = (int) (color.getRed() + (Math.sin((System.currentTimeMillis() + i * speed) / 1000) * 50));
                int g = (int) (color.getGreen() + (Math.cos((System.currentTimeMillis() + i * speed) / 1000) * 50));
                int b = color.getBlue();
                color = new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), b);
            }


            String colorCode = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            gradientText.append("§").append(colorCode).append(text.charAt(i));
        }

        return gradientText.toString();
    }

    public static String mCCC(String text, int firstColorInt, int secondColorInt, float speed) {

    Color firstColor = new Color(firstColorInt);
    Color secondColor = new Color(secondColorInt);

    StringBuilder gradientText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
        float progress = (float) i / text.length();
        Color color = interpolateColorC(firstColor, secondColor, progress);

        if (speed > 0) {
            int r = (int) (color.getRed() + (Math.sin((System.currentTimeMillis() + i * speed) / 1000) * 50));
            int g = (int) (color.getGreen() + (Math.cos((System.currentTimeMillis() + i * speed) / 1000) * 50));
            int b = color.getBlue();
            color = new Color(Math.min(255, Math.max(0, r)), Math.min(255, Math.max(0, g)), b);
        }

        String colorCode = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        gradientText.append("§").append(colorCode).append(text.charAt(i));
    }

        return gradientText.toString();
  }





    public static String[] themes = new String[]{"Rainbow", "Cherry", "Cotton candy", "Flare", "Flower", "Gold", "Grayscale", "Royal", "Sky", "Vine", "Steelvoid", "Mist", "America", "Neo", "Nord", "Coral"};

        public static int getGradient(double index, double delay) {
            if (index > 0) {
                return convert(values()[(int) index].firstGradient, values()[(int) index].secondGradient, (Math.sin(System.currentTimeMillis() / 1.0E8 * Settings.timeMultiplier.getInput() * 400000.0 + delay * Settings.offset.getInput()) + 1.0) * 0.5).getRGB();
            }
            else if (index == 0) {
                return Utils.getChroma(2, (long) delay);
            }
            return -1;
        }
    }
