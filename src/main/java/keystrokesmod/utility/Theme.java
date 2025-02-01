package keystrokesmod.utility;

import keystrokesmod.module.impl.client.Settings;

import java.awt.*;

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
    Mist(new Color(94, 228, 154), new Color(40, 139, 207)); // 11

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
                return new int[]{Utils.getChroma(2, (long) 0), Utils.getChroma(2, (long) 0)};
            }
        }
        return new int[]{0, 0};
    }

    public static String[] themes = new String[]{"Rainbow", "Cherry", "Cotton candy", "Flare", "Flower", "Gold", "Grayscale", "Royal", "Sky", "Vine", "Steelvoid", "Mist"};

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
