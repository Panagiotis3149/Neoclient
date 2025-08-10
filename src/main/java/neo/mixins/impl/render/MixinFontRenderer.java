package neo.mixins.impl.render;

import neo.module.ModuleManager;
import neo.module.impl.client.ClientTheme;
import neo.module.impl.client.Settings;
import neo.module.impl.other.NameHider;
import neo.util.render.Theme;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    @Shadow private boolean randomStyle;
    @Shadow private boolean boldStyle;
    @Shadow private boolean strikethroughStyle;
    @Shadow private boolean underlineStyle;
    @Shadow private boolean italicStyle;

    @Shadow private int textColor;
    @Shadow private int[] colorCode;

    @Shadow private float alpha;
    @Shadow private float red;
    @Shadow private float blue;
    @Shadow private float green;

    @Shadow private java.util.Random fontRandom;
    @Shadow private boolean unicodeFlag;

    @Shadow private float posX;
    @Shadow private float posY;

    @Shadow
    private void setColor(float red, float green, float blue, float alpha) { }

    @Shadow
    private int getCharWidth(char c) { return 0; }

    @Shadow
    private float func_181559_a(char c, boolean italic) { return 0f; }

    @Shadow private int FONT_HEIGHT;


    @Shadow
    private void doDraw(float f) { }

    @ModifyVariable(method = "renderString", at = @At("HEAD"), require = 1, ordinal = 0, argsOnly = true)
    private String renderString(String string) {
        if (string == null)
            return null;
        if ((ModuleManager.nameHider != null) && ModuleManager.nameHider.isEnabled()) {
            string = NameHider.getFakeName(string);
        }


        return string;
    }

    /**
     * @author Mojang?? i (Panagiotis3149) just added text theming
     * @reason Text Theming
     */
    @Overwrite
    private void renderStringAtPos(String text, boolean shadow) {
        int themeIndex = (int) ClientTheme.theme.getInput();
        double timeMult = Settings.timeMultiplier.getInput();
        float lineHeight = 9f;

        for (int i = 0; i < text.length(); i++) {
            if (text.startsWith("$NEOTHEME$", i)) {
                int end = text.indexOf("$NEOTHEME$", i + 10);
                if (end == -1) {
                    // treat as normal text (fall through)
                } else {
                    String themed = text.substring(i + 10, end);
                    String[] parts = themed.split("\n", -1);
                    for (int p = 0; p < parts.length; p++) {
                        String part = parts[p];
                        for (int j = 0; j < part.length(); j++) {
                            char c = part.charAt(j);
                            double delay = j * timeMult;

                            if (c == 167 && j + 1 < part.length()) {
                                int i1 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(part.charAt(j + 1)));

                                if (i1 >= 0 && i1 <= 15) {
                                    this.randomStyle = false;
                                    this.boldStyle = false;
                                    this.strikethroughStyle = false;
                                    this.underlineStyle = false;
                                    this.italicStyle = false;

                                    int color = this.colorCode[shadow ? i1 + 16 : i1];
                                    this.textColor = color;
                                    this.red = (color >> 16) & 0xFF;
                                    this.blue = (color >> 8) & 0xFF;
                                    this.green = color & 0xFF;
                                    if (!shadow) {
                                        int rgb = Theme.getGradient(themeIndex, delay);
                                        float r = (rgb >> 16 & 0xFF) / 255f;
                                        float g = (rgb >> 8 & 0xFF) / 255f;
                                        float b = (rgb & 0xFF) / 255f;
                                        setColor(r, g, b, this.alpha);
                                    } else {
                                        setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, this.alpha);
                                    }
                                } else if (i1 == 16) this.randomStyle = true;
                                else if (i1 == 17) this.boldStyle = true;
                                else if (i1 == 18) this.strikethroughStyle = true;
                                else if (i1 == 19) this.underlineStyle = true;
                                else if (i1 == 20) this.italicStyle = true;
                                else if (i1 == 21) {
                                    this.randomStyle = false;
                                    this.boldStyle = false;
                                    this.strikethroughStyle = false;
                                    this.underlineStyle = false;
                                    this.italicStyle = false;
                                    setColor(this.red, this.blue, this.green, this.alpha);
                                }

                                j++;
                                continue;
                            }

                            if (!shadow) {
                                int rgb = Theme.getGradient(themeIndex, delay);
                                float r = (rgb >> 16 & 0xFF) / 255f;
                                float g = (rgb >> 8 & 0xFF) / 255f;
                                float b = (rgb & 0xFF) / 255f;
                                setColor(r, g, b, this.alpha);
                            }

                            float f = this.func_181559_a(c, this.italicStyle);
                            if (this.boldStyle) {
                                this.posX += (this.unicodeFlag ? 0.5f : 1f);
                                this.func_181559_a(c, this.italicStyle);
                                this.posX -= (this.unicodeFlag ? 0.5f : 1f);
                                f++;
                            }

                            doDraw(f);
                        }

                        if (p < parts.length - 1) {
                            this.posX = 0f;
                            this.posY += lineHeight;
                        }
                    }

                    i = end + "$NEOTHEME$".length() - 1;
                    continue;
                }
            }

            char c0 = text.charAt(i);
            if (c0 == 167 && i + 1 < text.length()) {
                int i1 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1)));

                if (i1 >= 0 && i1 <= 15) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    int color = this.colorCode[shadow ? i1 + 16 : i1];
                    this.textColor = color;
                    setColor((color >> 16) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, this.alpha);
                } else if (i1 == 16) this.randomStyle = true;
                else if (i1 == 17) this.boldStyle = true;
                else if (i1 == 18) this.strikethroughStyle = true;
                else if (i1 == 19) this.underlineStyle = true;
                else if (i1 == 20) this.italicStyle = true;
                else if (i1 == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    setColor(this.red, this.blue, this.green, this.alpha);
                }

                i++;
                continue;
            }

            float f = this.func_181559_a(c0, this.italicStyle);
            if (this.boldStyle) {
                this.posX += (this.unicodeFlag ? 0.5f : 1f);
                this.func_181559_a(c0, this.italicStyle);
                this.posX -= (this.unicodeFlag ? 0.5f : 1f);
                f++;
            }

            doDraw(f);
        }
    }






    @ModifyVariable(method = "getStringWidth", at = @At("HEAD"), require = 1, ordinal = 0, argsOnly = true)
    private String getStringWidth(String string) {
        if (string == null) return null;
        string = string.replaceAll("(?s)\\$NEOTHEME\\$(.*?)\\$NEOTHEME\\$", "$1");
        if ((ModuleManager.nameHider != null) && ModuleManager.nameHider.isEnabled()) {
            string = NameHider.getFakeName(string);
        }
        string = string.replace("$NEOTHEME$", "");
        return string;
    }

}
