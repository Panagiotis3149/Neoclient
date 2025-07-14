package neo;


import neo.gui.click.ClickGui;
import neo.gui.menu.MainMenu;
import neo.util.command.*;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.script.ScriptManager;
import neo.util.*;
import neo.util.config.Config;
import neo.util.other.DebugInfoRenderer;
import neo.util.other.GuiDetectionHandler;
import neo.util.other.ParticleDistanceHandler;
import neo.util.other.java.Reflection;
import neo.util.packet.BadPacketsHandler;
import neo.util.player.CPSCalculator;
import neo.util.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.LWJGLException;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Mod(
        modid = "neo",
        name = "Neoclient",
        version = "2.0.0",
        acceptedMinecraftVersions = "1.8.9"
)

public class Neo {
    public static boolean recommendedChanges = false;
    public static boolean debugger = false;
    public static Minecraft mc = Minecraft.getMinecraft();
    private static final ScheduledExecutorService ex = Executors.newScheduledThreadPool(2);
    public static ModuleManager moduleManager;
    public static ClickGui clickGui;
    public static ConfigManager configManager;
    public static ScriptManager scriptManager;
    public static Config currentConfig;
    public static BadPacketsHandler badPacketsHandler;
    public static File NeoDirectory = new File(mc.mcDataDir + File.separator + "neo");

    public Neo() {
        moduleManager = new ModuleManager();
    }

    private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        if (bufferedimage == null) {
            throw new IOException("Failed to load image from InputStream.");
        }

        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

        for (int i : aint) {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    public InputStream getInputStreamAssets(ResourceLocation location) throws IOException {
        IResource resource = mc.getResourceManager().getResource(location);
        return resource.getInputStream();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        Display.setTitle(Variables.clientName + " " + Variables.clientVersion);

        ByteBuffer[] icons = new ByteBuffer[4];
        try (
                InputStream icon16 = getInputStreamAssets(new ResourceLocation("neo", "textures/gui/ico1.png"));
                InputStream icon32 = getInputStreamAssets(new ResourceLocation("neo", "textures/gui/ico2.png"));
                InputStream icon128 = getInputStreamAssets(new ResourceLocation("neo", "textures/gui/ico3.png"));
                InputStream icon256 = getInputStreamAssets(new ResourceLocation("neo", "textures/gui/ico4.png"))
        ) {
            icons[0] = readImageToBuffer(icon16);   // 16x16
            icons[1] = readImageToBuffer(icon32);   // 32x32
            icons[2] = readImageToBuffer(icon128);  // 128x128
            icons[3] = readImageToBuffer(icon256);  // 256x256

            Display.setIcon(icons);
        } catch (IOException err) {
            System.err.println("icon loading ewwor: " + err.getMessage());
            err.printStackTrace();
        }
    }



    @EventHandler
    public void init(FMLInitializationEvent e) throws IOException, LWJGLException {
        Runtime.getRuntime().addShutdownHook(new Thread(ex::shutdown));
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new DebugInfoRenderer());
        FMLCommonHandler.instance().bus().register(new CPSCalculator());
        FMLCommonHandler.instance().bus().register(badPacketsHandler = new BadPacketsHandler());
        FMLCommonHandler.instance().bus().register(new GuiDetectionHandler());
        FMLCommonHandler.instance().bus().register(new ParticleDistanceHandler());

        // COMMANDS
        FMLCommonHandler.instance().bus().register(new BindCommand(moduleManager));
        FMLCommonHandler.instance().bus().register(new ToggleCommand(moduleManager));
        FMLCommonHandler.instance().bus().register(new NameHiderCommand());
        FMLCommonHandler.instance().bus().register(new HelpCommand());
        FMLCommonHandler.instance().bus().register(new AnticheatCommand());
        // END OF COMMANDS

        RPC rpc = new RPC();
        rpc.onUpdate();
        Reflection.getFields();
        Reflection.getMethods();
        moduleManager.register();
        scriptManager = new ScriptManager();
        clickGui = new ClickGui();
        configManager = new ConfigManager();
        configManager.loadConfigs();
        configManager.loadConfig("default");
        Reflection.setKeyBindings();
        scriptManager.loadScripts();
        NeoCloud.checkVersionAsync();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        String flag = System.getProperty("recommendedChanges");
        recommendedChanges = Boolean.parseBoolean(flag);
        System.out.println("Neo PostInit - recommendedChanges: " + recommendedChanges);
        if (recommendedChanges) {
            Utils.patchOptifineSettings();
            mc.gameSettings.fancyGraphics = false;
            mc.gameSettings.ambientOcclusion = 0;
            mc.gameSettings.useNativeTransport = true;
            mc.gameSettings.particleSetting = 2;
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            event.gui = new MainMenu();
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent e) {
        if (e.phase == Phase.END) {
            if (Utils.isnull()) {
                if (Reflection.sendMessage) {
                    Utils.sendMessage("&cewwor, pwease rewaunch");
                    Reflection.sendMessage = false;
                }
                for (Module module : getModuleManager().getModules()) {
                    if (mc.currentScreen == null && module.canBeEnabled()) {
                        module.keybind();
                    } else if (mc.currentScreen instanceof ClickGui) {
                        module.guiUpdate();
                    }

                    if (module.isEnabled()) {
                        module.onUpdate();
                    }
                }
                for (Config config : Neo.configManager.configs) {
                    if (mc.currentScreen == null) {
                        config.getModule().keybind();
                    }
                }
                for (Module module : Neo.scriptManager.scripts.values()) {
                    if (mc.currentScreen == null) {
                        module.keybind();
                    }
                }
            }

        }
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static ScheduledExecutorService getExecutor() {
        return ex;
    }
}
