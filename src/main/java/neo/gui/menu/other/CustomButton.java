package neo.gui.menu.other;

import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

    public class CustomButton extends GuiButton {
        private static final int BUTTON_COLOR = 0x33161616;
        private float hoverAnimation = 0.0F;

        public CustomButton(int buttonId, int x, int y, String buttonText) {
            super(buttonId, x, y, 160, 20, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                        mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;


                if (this.hovered) {
                    hoverAnimation = Math.min(hoverAnimation + 0.1F, 1.0F);
                } else {
                    hoverAnimation = Math.max(hoverAnimation - 0.1F, 0.0F); // Ease out
                }


                int hoverOffset = (int) (hoverAnimation * 2);

                int boxX = this.xPosition;
                int boxY = this.yPosition - hoverOffset;
                int boxW = this.width;
                int boxH = this.height - hoverOffset;

                BlurUtils.prepareBlur();
                RoundedUtils.drawRound(boxX, boxY, boxW, boxH, 12, Color.black);
                BlurUtils.blurEnd(2, 2.5F);

                this.mouseDragged(mc, mouseX, mouseY);


                int textColor;
                textColor = this.enabled ? 0xFFFFFFFF : 0xFF526880;

                FontRenderer font = FontManager.googleSansMedium;
                String txt = this.displayString;
                int txtW = (int) font.getStringWidth(txt);
                int txtH = (int) font.getHeight();

                int txtX = boxX + (boxW - txtW) / 2;
                int txtY = boxY + (boxH - txtH) / 2;

                font.drawString(txt, txtX, txtY, textColor);

            }
        }
    }
