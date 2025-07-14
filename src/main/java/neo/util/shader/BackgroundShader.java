package neo.util.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BackgroundShader {
    private int shaderProgram = 0;
    private final ResourceLocation fragShader;
    Minecraft mc = Minecraft.getMinecraft();
    private float time = 0f;
    private Framebuffer tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private long startTime = System.currentTimeMillis();

    public BackgroundShader(ResourceLocation fragShader) {
        this.fragShader = fragShader;
    }

    public void init() throws IOException {
        if (shaderProgram != 0) {
            GL20.glDeleteProgram(shaderProgram);
            shaderProgram = 0;
        }
        try {
            initShader();
            startTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initShader() throws IOException {
        shaderProgram = createShaderProgram();
    }

    private void clear() {
        if (mc.displayWidth != tempFBO.framebufferWidth || mc.displayHeight != tempFBO.framebufferHeight) {
            tempFBO.deleteFramebuffer();
            tempFBO = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            tempFBO.framebufferClear();
        }
    }


    public void use(float partialTicks) {
        if (!Display.isVisible() || shaderProgram == 0 || !GL20.glIsProgram(shaderProgram)) return;

        time += partialTicks * 0.05f;

        ScaledResolution sr = new ScaledResolution(mc);
        int w = mc.displayWidth;
        int h = mc.displayHeight;

        mc.getFramebuffer().bindFramebuffer(true);

        GL20.glUseProgram(shaderProgram);
        GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "resolution"), w, h);
        GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "time"),
                (System.currentTimeMillis() - startTime) / 1000f);
        {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0.0F, 0.0F);
            GL11.glVertex2d(0, h);
            GL11.glTexCoord2f(1.0F, 0.0F);
            GL11.glVertex2d(w, h);
            GL11.glTexCoord2f(1.0F, 1.0F);
            GL11.glVertex2d(w, 0);
            GL11.glTexCoord2f(0.0F, 1.0F);
            GL11.glVertex2d(0, 0);
            GL11.glEnd();
        }
        GL20.glUseProgram(0);
    }

    public void cleanUp() {
        if (shaderProgram != 0) {
            GL20.glUseProgram(0);
            GL20.glDeleteProgram(shaderProgram);
            shaderProgram = 0;
        }
    }


    private int createShaderProgram() throws IOException {
        String vertexShaderSource =
                "#version 120\n" +
                        "void main() {\n" +
                        "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
                        "}";

        String fragmentShaderSource = loadShaderFromResource(fragShader);
        if (fragmentShaderSource == null) {
            System.err.println("Failed to load fragment shader.");
            return 0;
        }

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glCompileShader(vertexShader);
        if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Vertex shader compile failed:\n" + GL20.glGetShaderInfoLog(vertexShader, GL20.glGetShaderi(vertexShader, GL20.GL_INFO_LOG_LENGTH)));
            return 0;
        }

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);
        GL20.glCompileShader(fragmentShader);
        if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("Fragment shader compile failed:\n" + GL20.glGetShaderInfoLog(fragmentShader, GL20.glGetShaderi(fragmentShader, GL20.GL_INFO_LOG_LENGTH)));
            return 0;
        }

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("Shader program link failed:\n" + GL20.glGetProgramInfoLog(program, GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH)));
            return 0;
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return program;
    }

    private String loadShaderFromResource(ResourceLocation resourceLocation) throws IOException {
        try (InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
