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
    Steelvoid(new Color(66, 134, 244), new Color(55, 59, 68)), // 10
    Mist(new Color(94, 228, 154), new Color(40, 139, 207)), // 11
    America(new Color(49, 102, 184), new Color(251, 51, 75)), // 12
    Neo(new Color(71, 120, 225), new Color(97, 93, 178)), // 13
    Nord(new Color(112, 193, 158), new Color(186, 243, 202, 255)), // 14
    Coral(new Color(244, 168, 150), new Color(52, 133, 151)), // 15
    // NEW ONES
    Aubergine(new Color(170, 7, 107), new Color(97, 4, 95)), // 1
    Aqua(new Color(185, 250, 255), new Color(79, 199, 200)), // 2
    Banana(new Color(253, 236, 177), new Color(255, 255, 255)), // 3
    Blend(new Color(71, 148, 253), new Color(71, 253, 160)), // 4
    Bubblegum(new Color(243, 145, 216), new Color(152, 165, 243)), // 5
    Candy_cane(new Color(255, 0, 0), new Color(255, 255, 255)), // 6
    Christmas(new Color(255, 64, 64), new Color(255, 255, 255)), // 7
    Digital_horizon(new Color(95, 195, 228), new Color(229, 93, 135)), // 8
    Express(new Color(173, 83, 137), new Color(60, 16, 83)), // 9
    Lime_water(new Color(18, 255, 247), new Color(179, 255, 171)), // 10
    Lush(new Color(168, 224, 99), new Color(86, 171, 47)), // 11
    Halogen(new Color(255, 65, 108), new Color(255, 75, 43)), // 12
    Hyper(new Color(236, 110, 173), new Color(52, 148, 230)), // 13
    Magic(new Color(74, 0, 224), new Color(142, 45, 226)), // 14
    May(new Color(238, 79, 238), new Color(253, 219, 245)), // 15
    Orange_juice(new Color(252, 74, 26), new Color(247, 183, 51)), // 16
    Pastel(new Color(243, 155, 178), new Color(207, 196, 243)), // 17
    Pumpkin(new Color(241, 166, 98), new Color(255, 216, 169)), // 18
    Satin(new Color(215, 60, 67), new Color(140, 23, 39)), // 19
    Snowy_sky(new Color(1, 171, 179), new Color(234, 234, 234)), // 20
    Sundae(new Color(206, 74, 126), new Color(122, 44, 77)), // 21
    Sunkist(new Color(242, 201, 76), new Color(242, 153, 74)), // 22
    Water(new Color(12, 232, 199), new Color(12, 163, 232)), // 23
    Legacy(new Color(0x70CEFF), new Color(0x70CEFF)), // 24
    Winter(new Color(255, 255, 255), new Color(255, 255, 255)), // 25
    Peony(new Color(226, 208, 249), new Color(207, 171, 255)), // 26
    Shadow(new Color(97, 131, 255), new Color(206, 212, 255)), // 27
    Wood(new Color(79, 109, 81), new Color(170, 139, 87)), // 28
    Creida(new Color(0xff4e5270).brighter().brighter(), new Color(0xff4e5270).darker()), // 29
    Creida_two(new Color(0xff9ACAEB), new Color(0xff7FBBE6).darker()), // 30
    Gothic(new Color(31, 30, 30), new Color(196, 190, 190)), // 31
    Sen(new Color(234, 118, 176), new Color(31, 30, 30)), // 32 Sen -- Rue
    Purple(new Color(0x524391), new Color(0x524391).brighter()); // 33

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
            firstGradient = new Color(66, 134, 244);
            secondGradient = new Color(55, 59, 68);
        } else if (selectedTheme == 11) { // Mist
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
        } else if (selectedTheme == 16) { // Aubergine
            firstGradient = new Color(170, 7, 107);
            secondGradient = new Color(97, 4, 95);
        } else if (selectedTheme == 17) { // Aqua
            firstGradient = new Color(185, 250, 255);
            secondGradient = new Color(79, 199, 200);
        } else if (selectedTheme == 18) { // Banana
            firstGradient = new Color(253, 236, 177);
            secondGradient = new Color(255, 255, 255);
        } else if (selectedTheme == 19) { // Blend
            firstGradient = new Color(71, 148, 253);
            secondGradient = new Color(71, 253, 160);
        } else if (selectedTheme == 20) { // Bubblegum
            firstGradient = new Color(243, 145, 216);
            secondGradient = new Color(152, 165, 243);
        } else if (selectedTheme == 21) { // Candy Cane
            firstGradient = new Color(255, 0, 0);
            secondGradient = new Color(255, 255, 255);
        } else if (selectedTheme == 22) { // Christmas
            firstGradient = new Color(255, 64, 64);
            secondGradient = new Color(255, 255, 255);
        } else if (selectedTheme == 23) { // Digital Horizon
            firstGradient = new Color(95, 195, 228);
            secondGradient = new Color(229, 93, 135);
        } else if (selectedTheme == 24) { // Express
            firstGradient = new Color(173, 83, 137);
            secondGradient = new Color(60, 16, 83);
        } else if (selectedTheme == 25) { // Lime Water
            firstGradient = new Color(18, 255, 247);
            secondGradient = new Color(179, 255, 171);
        } else if (selectedTheme == 26) { // Lush
            firstGradient = new Color(168, 224, 99);
            secondGradient = new Color(86, 171, 47);
        } else if (selectedTheme == 27) { // Halogen
            firstGradient = new Color(255, 65, 108);
            secondGradient = new Color(255, 75, 43);
        } else if (selectedTheme == 28) { // Hyper
            firstGradient = new Color(236, 110, 173);
            secondGradient = new Color(52, 148, 230);
        } else if (selectedTheme == 29) { // Magic
            firstGradient = new Color(74, 0, 224);
            secondGradient = new Color(142, 45, 226);
        } else if (selectedTheme == 30) { // May
            firstGradient = new Color(238, 79, 238);
            secondGradient = new Color(253, 219, 245);
        } else if (selectedTheme == 31) { // Orange Juice
            firstGradient = new Color(252, 74, 26);
            secondGradient = new Color(247, 183, 51);
        } else if (selectedTheme == 32) { // Pastel
            firstGradient = new Color(243, 155, 178);
            secondGradient = new Color(207, 196, 243);
        } else if (selectedTheme == 33) { // Pumpkin
            firstGradient = new Color(241, 166, 98);
            secondGradient = new Color(255, 216, 169);
        } else if (selectedTheme == 34) { // Satin
            firstGradient = new Color(215, 60, 67);
            secondGradient = new Color(140, 23, 39);
        } else if (selectedTheme == 35) { // Snowy Sky
            firstGradient = new Color(1, 171, 179);
            secondGradient = new Color(234, 234, 234);
        } else if (selectedTheme == 36) { // Sundae
            firstGradient = new Color(206, 74, 126);
            secondGradient = new Color(122, 44, 77);
        } else if (selectedTheme == 37) { // Sunkist
            firstGradient = new Color(242, 201, 76);
            secondGradient = new Color(242, 153, 74);
        } else if (selectedTheme == 38) { // Water
            firstGradient = new Color(12, 232, 199);
            secondGradient = new Color(12, 163, 232);
        } else if (selectedTheme == 39) { // Legacy
            firstGradient = new Color(0x70CEFF);
            secondGradient = new Color(0x70CEFF).brighter();
        } else if (selectedTheme == 40) { // Winter
            firstGradient = new Color(255, 255, 255);
            secondGradient = new Color(255, 255, 255);
        } else if (selectedTheme == 41) { // Peony
            firstGradient = new Color(226, 208, 249);
            secondGradient = new Color(207, 171, 255);
        } else if (selectedTheme == 42) { // Shadow
            firstGradient = new Color(97, 131, 255);
            secondGradient = new Color(206, 212, 255);
        } else if (selectedTheme == 43) { // Wood
            firstGradient = new Color(79, 109, 81);
            secondGradient = new Color(170, 139, 87);
        } else if (selectedTheme == 44) { // Creida
            firstGradient = new Color(0xff4e5270).brighter().brighter();
            secondGradient = new Color(0xff4e5270).darker();
        } else if (selectedTheme == 45) { // Creida Two
            firstGradient = new Color(0xff9ACAEB);
            secondGradient = new Color(0xff7FBBE6).darker();
        } else if (selectedTheme == 46) { // Gothic
            firstGradient = new Color(31, 30, 30);
            secondGradient = new Color(196, 190, 190);
        } else if (selectedTheme == 47) { // Sen -- Rue
            firstGradient = new Color(234, 118, 176);
            secondGradient = new Color(31, 30, 30);
        } else if (selectedTheme == 48) { // Purple
            firstGradient = new Color(0x524391);
            secondGradient = new Color(0x524391).brighter();
        }

        return new int[]{ firstGradient.getRGB(), secondGradient.getRGB() };
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





    public static String[] themes = new String[]{
            "Rainbow",
            "Cherry",
            "Cotton candy",
            "Flare",
            "Flower",
            "Gold",
            "Grayscale",
            "Royal",
            "Sky",
            "Vine",
            "Steelvoid",
            "Mist",
            "America",
            "Neo",
            "Nord",
            "Coral", // last new
            "Eggplant", // Aubergine on rise
            "Aqua",
            "Banana",
            "Blend",
            "Bubblegum",
            "Candy Cane",
            "Christmas",
            "Digital Horizon",
            "Express",
            "Lime Water",
            "Lush",
            "Halogen",
            "Hyper",
            "Magic",
            "May",
            "Orange Juice",
            "Pastel",
            "Pumpkin",
            "Satin",
            "Snowy Sky",
            "Sundae",
            "Sunkist",
            "Water",
            "Legacy",
            "Winter",
            "Peony",
            "Shadow",
            "Wood",
            "Creida",
            "Creida Two",
            "Gothic",
            "Sen",
            "Purple"
    };

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
