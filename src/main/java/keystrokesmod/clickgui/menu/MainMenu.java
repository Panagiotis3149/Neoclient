package keystrokesmod.clickgui.menu;

import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainMenu extends GuiScreen {


    public static final ResourceLocation CUSTOM_LOGO = new ResourceLocation("keystrokesmod", "textures/gui/Logo.png");
    public static final ResourceLocation FRAG_SHADER = new ResourceLocation("keystrokesmod", "shaders/mainmenu.frag");


    private static final float SCALE_FACTOR = 0.25f;
    private static final int BUTTON_OFFSET_Y = 130;

    private static int shaderProgram;
    private float time = 0.0f;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int buttonY = (int) (this.height * SCALE_FACTOR) + BUTTON_OFFSET_Y;
        this.buttonList.add(new CustomButton(1, this.width / 2 - 100, buttonY, I18n.format("menu.singleplayer")));
        this.buttonList.add(new CustomButton(2, this.width / 2 - 100, buttonY + 24, I18n.format("menu.multiplayer")));
        this.buttonList.add(new CustomButton(0, this.width / 2 - 100, buttonY + 48, I18n.format("menu.options")));
        this.buttonList.add(new CustomButton(4, this.width / 2 - 100, buttonY + 72, I18n.format("menu.quit")));
       // TODO: ADD ALT MANAGER - this.buttonList.add(new CustomButton(5, this.width / 2 - 100, buttonY + 96, "Alt Manager"));
        // this.buttonList.add(new CustomButton(5, this.width / 2 - 100, buttonY + 96, "Alt Manager"));
        try {
            shaderProgram = createShaderProgram();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        time += partialTicks * 0.05f;

        renderShaderBackground();


        Minecraft mc = Minecraft.getMinecraft();


        mc.getTextureManager().bindTexture(CUSTOM_LOGO);
        int logoWidth = 128;
        int logoHeight = 128;
        int logoX = (this.width - logoWidth) / 2;
        int logoY = (int) (this.height * SCALE_FACTOR) - 10;
        drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight); // Draw logo

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }

    public void renderShaderBackground() {
        if (shaderProgram != 0) {
            int displayWidth = Minecraft.getMinecraft().displayWidth;
            int displayHeight = Minecraft.getMinecraft().displayHeight;

            // Bind shader and update uniforms using full display size
            GL20.glUseProgram(shaderProgram);
            GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "resolution"), displayWidth, displayHeight);
            GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "time"), time);

            // Save current GL state
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();

            // Set up the projection for full-screen rendering using display dimensions
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(0, displayWidth, displayHeight, 0, -1, 1);

            // Switch back to modelview matrix and reset it
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();

            // Set the viewport to cover the entire display
            GL11.glViewport(0, 0, displayWidth, displayHeight);

            // Draw the full-screen quad
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(0, 0);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(displayWidth, 0);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(displayWidth, displayHeight);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(0, displayHeight);
            GL11.glEnd();

            // Restore GL state so the main menu is unaffected
            GL11.glPopMatrix(); // Modelview
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix(); // Projection
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glPopAttrib();

            GL20.glUseProgram(0);
        }
    }







    private void drawModalRectWithCustomSizedTexture(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u * f, v * f1);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u * f, (v + height) * f1);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f((u + width) * f, (v + height) * f1);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f((u + width) * f, v * f1);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
    }
    private int createShaderProgram() throws IOException {
        String vertexShaderSource =
                "#version 120\n" +
                        "void main() {\n" +
                        "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
                        "}";

        // Load the fragment shader source from the resource file
        String fragmentShaderSource = loadShaderFromResource(FRAG_SHADER);

        if (fragmentShaderSource == null) {
            System.err.println("Failed to load fragment shader.");
            return 0;
        }

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);

        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Vertex shader compilation failed:\n" + GL20.glGetShaderInfoLog(vertexShader, GL20.glGetShaderi(vertexShader, GL20.GL_INFO_LOG_LENGTH)));
            return 0;
        }

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);

        if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Fragment shader compilation failed:\n" + GL20.glGetShaderInfoLog(fragmentShader, GL20.glGetShaderi(fragmentShader, GL20.GL_INFO_LOG_LENGTH)));
            return 0;
        }

        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);

        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Shader program linking failed:\n" + GL20.glGetProgramInfoLog(shaderProgram, GL20.glGetProgrami(shaderProgram, GL20.GL_INFO_LOG_LENGTH)));
            return 0;
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return shaderProgram;
    }

    private String loadShaderFromResource(ResourceLocation resourceLocation) throws IOException {
        try {
            InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder shaderSource = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
            return shaderSource.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        } else if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        } else if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        } else if (button.id == 4) {
            this.mc.shutdown();
        }
       // TODO: ADD ALT MANAGER - else if (button.id == 5) {
        // TODO: ADD ALT MANAGER -   this.mc.displayGuiScreen(new AltManagerScreen(this));
       // TODO: ADD ALT MANAGER - }
        // else if (button.id == 5) {
       // this.mc.displayGuiScreen(new AltManagerScreen(this));
      //  }

    }

    public static class CustomButton extends GuiButton {
        private static final int BUTTON_COLOR = 0x33161616;
        private float hoverAnimation = 0.0F; // For hover effect

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

                BlurUtils.prepareBlur();
                RoundedUtils.drawRound(this.xPosition + 20 , this.yPosition - hoverOffset, this.width, this.height - hoverOffset, 12, true, Color.black);
                BlurUtils.blurEnd(2, 2.5F);

                this.mouseDragged(mc, mouseX, mouseY);


                int textColor;
             //   if (HUD.theme.getInput() == 0) {
                textColor = this.enabled ? 0xFFFFFFFF : 0xFF526880;
        //        } else {
        //            textColor = this.enabled ? Theme.getGradient(Gui.theme.getInput(), 0.01) : 10526880;
       //         }

                FontRenderer font = FontManager.googleSansMedium;
                font.drawString(this.displayString, this.xPosition - 10 + this.width / 2, this.yPosition + (this.height - 8) / 2 - hoverOffset, textColor);
               // this.drawCenteredString(mc.fontRendererObj, this.displayString,
           //             this.xPosition + this.width / 2,
           //             this.yPosition + (this.height - 8) / 2 - hoverOffset, textColor);
            }
        }
    }
}

