package neo.gui.menu.other;

import neo.util.font.impl.FontRenderer;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.*;

public class CustomTextField extends Gui {
    public final int x, y, width, height;
    private String text = "";
    private int maxLength = Integer.MAX_VALUE;

    private int cursorPosition = 0;
    private int selectionEnd = 0;

    private boolean focused = false;
    private int cursorCounter = 0;

    private FontRenderer font;

    private int lineScrollOffset = 0;

    public CustomTextField(int x, int y, int width, int height, FontRenderer font) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
    }

    public void setMaxLength(int max) {
        this.maxLength = max;
        if (text.length() > max) text = text.substring(0, max);
        if (cursorPosition > max) cursorPosition = max;
        if (selectionEnd > max) selectionEnd = max;
    }

    public void setText(String txt) {
        text = txt.length() > maxLength ? txt.substring(0, maxLength) : txt;
        cursorPosition = Math.min(text.length(), cursorPosition);
        selectionEnd = cursorPosition;
        updateScrollOffset();
    }

    public String getText() {
        return text;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            Keyboard.enableRepeatEvents(true);
            cursorCounter = 0;
        } else {
            Keyboard.enableRepeatEvents(false);
        }
    }


    public boolean isFocused() {
        return focused;
    }

    public void updateCursorCounter() {
        cursorCounter++;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (!focused) return;

        if (keyCode == Keyboard.KEY_BACK) {
            if (selectionEnd != cursorPosition) {
                deleteSelection();
            } else if (cursorPosition > 0) {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
                selectionEnd = cursorPosition;
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            if (selectionEnd != cursorPosition) {
                deleteSelection();
            } else if (cursorPosition < text.length()) {
                text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                selectionEnd = cursorPosition;
            }
        } else if (keyCode == Keyboard.KEY_LEFT) {
            if (cursorPosition > 0) cursorPosition--;
            if (!GuiScreen.isShiftKeyDown()) selectionEnd = cursorPosition;
            updateScrollOffset();
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            if (cursorPosition < text.length()) cursorPosition++;
            if (!GuiScreen.isShiftKeyDown()) selectionEnd = cursorPosition;
            updateScrollOffset();
        } else if (GuiScreen.isCtrlKeyDown()) {
            // ctrl + c (copy)
            if (keyCode == Keyboard.KEY_C) {
                copyToClipboard(getSelectedText());
            }
            // ctrl + v (paste)
            else if (keyCode == Keyboard.KEY_V) {
                String clipboard = getClipboardString();
                if (clipboard != null && clipboard.length() > 0) {
                    writeText(clipboard);
                    updateScrollOffset();
                }
            }
            // ctrl + x (cut)
            else if (keyCode == Keyboard.KEY_X) {
                copyToClipboard(getSelectedText());
                deleteSelection();
                updateScrollOffset();
            }
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            writeText(Character.toString(typedChar));
            updateScrollOffset();
        }
    }

    private void writeText(String input) {
        if (selectionEnd != cursorPosition) {
            deleteSelection();
        }
        int insertPos = cursorPosition;
        String filtered = ChatAllowedCharacters.filterAllowedCharacters(input);
        int allowedLength = maxLength - text.length();
        if (filtered.length() > allowedLength) {
            filtered = filtered.substring(0, allowedLength);
        }
        text = text.substring(0, insertPos) + filtered + text.substring(insertPos);
        cursorPosition += filtered.length();
        selectionEnd = cursorPosition;
    }

    private void deleteSelection() {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        text = text.substring(0, start) + text.substring(end);
        cursorPosition = start;
        selectionEnd = start;
    }

    private String getSelectedText() {
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;

        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            if (!focused) setFocused(true);
            int relX = mouseX - x;
            String visibleText = text.substring(lineScrollOffset);
            int cursorPosInVisible = font.trimStringToWidth(visibleText, relX, false).length();
            cursorPosition = Math.min(lineScrollOffset + cursorPosInVisible, text.length());
            selectionEnd = cursorPosition;
            updateScrollOffset();
        } else {
            setFocused(false);  // clear focus if clicked outside
        }
    }

    public void drawTextBox() {
        RoundedUtils.drawRound(x - 1, y - 1, width + 1, height + 1, 2, 0x55222222);
        BlurUtils.prepareBlur();
        RoundedUtils.drawRound(x, y, width, height, 2, Color.BLACK);
        BlurUtils.blurEnd(2, 1f);

        int textY = (int) (y + (height - font.height()) / 2);

        String visibleText = text.substring(lineScrollOffset);

        if (cursorPosition != selectionEnd) {
            int selStart = Math.min(cursorPosition, selectionEnd);
            int selEnd = Math.max(cursorPosition, selectionEnd);

            if (selEnd < lineScrollOffset) {
            } else if (selStart > text.length()) {
            } else {
                int visSelStart = Math.max(selStart, lineScrollOffset) - lineScrollOffset;
                int visSelEnd = Math.min(selEnd, text.length()) - lineScrollOffset;

                int selXStart = (int) (x + 4 + font.getStringWidth(visibleText.substring(0, visSelStart)));
                int selXEnd = (int) (x + 4 + font.getStringWidth(visibleText.substring(0, visSelEnd)));
                drawRect(selXStart, y - 20, selXEnd, y + height, 0x8833B5E5);
            }
        }

        font.drawString(visibleText, x + 4, textY, 0xFFFFFFFF);

        cursorCounter++;
        if (focused && (cursorCounter / 6) % 2 == 0) {
            int cursorRelPos = cursorPosition - lineScrollOffset;
            if (cursorRelPos >= 0 && cursorRelPos <= visibleText.length()) {
                int cursorX = (int) (x + font.getStringWidth(visibleText.substring(0, cursorRelPos)));
                drawRect(cursorX, y + 2, cursorX + 1, (int) (y + font.height() + 2), 0xFFFFFFFF);
            }
        }
    }

    private void updateScrollOffset() {
        int cursorX = (int) font.getStringWidth(text.substring(lineScrollOffset, cursorPosition));
        while (cursorX > width - 6) {
            lineScrollOffset++;
            cursorX = (int) font.getStringWidth(text.substring(lineScrollOffset, cursorPosition));
        }
        while (lineScrollOffset > 0 && font.getStringWidth(text.substring(lineScrollOffset - 1, cursorPosition)) <= width - 6) {
            lineScrollOffset--;
        }
    }

    private static void copyToClipboard(String str) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
        } catch (Exception ignored) {}
    }

    private static String getClipboardString() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return null;
        }
    }


}
