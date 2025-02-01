package keystrokesmod.clickgui.menu;

import keystrokesmod.utility.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class MainMenu extends GuiScreen {


    public static final ResourceLocation CUSTOM_LOGO = new ResourceLocation("keystrokesmod", "textures/gui/Logo.png");

    private static final float SCALE_FACTOR = 0.25f;
    private static final int BUTTON_OFFSET_Y = 130;
    private static final int LOGO_Y_OFFSET = -130;

    private static final int BG_WIDTH = 1440;
    private static final int BG_HEIGHT = 810;
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
        shaderProgram = createShaderProgram();
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
            GL20.glUseProgram(shaderProgram);
            GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "resolution"), this.width, this.height);
            GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "time"), time);

            float aspectRatio = (float) BG_WIDTH / BG_HEIGHT;
            float screenAspectRatio = (float) this.width / this.height;

            float scaledWidth = this.width;
            float scaledHeight = this.height;

            if (screenAspectRatio > aspectRatio) {
                scaledWidth = this.height * aspectRatio;
            } else {
                scaledHeight = this.width / aspectRatio;
            }

            float xOffset = (this.width - scaledWidth) / 2.0f;
            float yOffset = (this.height - scaledHeight) / 2.0f;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(xOffset, yOffset);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(xOffset + scaledWidth, yOffset);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(xOffset + scaledWidth, yOffset + scaledHeight);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(xOffset, yOffset + scaledHeight);
            GL11.glEnd();


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

    private int createShaderProgram() {
        String vertexShaderSource =
                "#version 120\n" +
                        "void main() {\n" +
                        "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
                        "}";

        String fragmentShaderSource =
                "#ifdef GL_ES\n" +
                        "precision mediump float;\n" +
                        "#endif\n" +
                        "#extension GL_OES_standard_derivatives : enable\n" +
                        "uniform vec2 resolution;\n" +
                        "uniform float time;\n" +
                        "mat2 m(float a) {\n" +
                        "    float c = cos(a), s = sin(a);\n" +
                        "    return mat2(c, -s, s, c);\n" +
                        "}\n" +
                        "float map(vec3 p) {\n" +
                        "    p.xz *= m(time * 0.4);\n" +
                        "    p.xy *= m(time * 0.1);\n" +
                        "    vec3 q = p * 2.0 + time;\n" +
                        "    return length(p + vec3(sin(time * 0.7))) * log(length(p) + 1.0) + sin(q.x + sin(q.z + sin(q.y))) * 0.5 - 1.0;\n" +
                        "}\n" +
                        "void main() {\n" +
                        "    vec2 a = gl_FragCoord.xy / resolution - vec2(0.5, 0.5);\n" +
                        "    vec3 cl = vec3(1.0);\n" +
                        "    float d = 2.5;\n" +
                        "    for (int i = 0; i <= 5; i++) {\n" +
                        "        vec3 p = vec3(0, 0, 4.0) + normalize(vec3(a, -1.0)) * d;\n" +
                        "        float rz = map(p);\n" +
                        "        float f = clamp((rz - map(p + 0.1)) * 0.5, -0.1, 1.0);\n" +
                        "        vec3 l = vec3(0.1, 0.3, 0.4) + vec3(5.0, 2.5, 3.0) * f;\n" +
                        "        cl = cl * l + smoothstep(2.5, 0.0, rz) * 0.6 * l;\n" +
                        "        d += min(rz, 1.0);\n" +
                        "    }\n" +
                        "    gl_FragColor = vec4(cl, 1.0);\n" +
                        "}";

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
            super(buttonId, x, y, 200, 20, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                        mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;


                if (this.hovered) {
                    hoverAnimation = Math.min(hoverAnimation + 0.1F, 1.0F); // Ease in
                } else {
                    hoverAnimation = Math.max(hoverAnimation - 0.1F, 0.0F); // Ease out
                }


                int hoverOffset = (int) (hoverAnimation * 2);


                RenderUtils.drawRoundedRectangle(this.xPosition, this.yPosition - hoverOffset,
                        this.xPosition + this.width, this.yPosition + this.height - hoverOffset, 12, BUTTON_COLOR);

                RenderUtils.drawOutline(this.xPosition,this.yPosition - hoverOffset,this.xPosition + this.width,this.yPosition + this.height - hoverOffset,2, 0XFF00BCD4);

                this.mouseDragged(mc, mouseX, mouseY);


                int textColor = this.enabled ? 0xFFFFFF : 10526880;


                this.drawCenteredString(mc.fontRendererObj, this.displayString,
                        this.xPosition + this.width / 2,
                        this.yPosition + (this.height - 8) / 2 - hoverOffset, textColor);
            }
        }
    }
}

