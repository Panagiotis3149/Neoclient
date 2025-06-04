package neo.util.font;

import net.minecraft.client.Minecraft;

import static neo.Neo.mc;

public class MinecraftFontRenderer {
    public static MinecraftFontRenderer INSTANCE = new MinecraftFontRenderer();
    public double drawString(String text, double x, double y, int color, boolean dropShadow) {
        return mc.fontRendererObj.drawString(text, (float) x, (float) y, color, dropShadow);
    }

    public double drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    public double width(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    public int getAccurateWidthTest(String text) {
        if (text == null) return 0;

        int width = 0;
        boolean italic = false;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                i++;
                char code = text.charAt(i);

                if (code == 'l' || code == 'L') {
                    italic = true;
                } else if (code == 'r' || code == 'R') {
                    italic = false;
                }
                continue;
            }

            int charWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(c);
            width += charWidth;
            if (italic && charWidth > 0) {
                width++;
            }
        }

        return width;
    }



    public double height() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }
}