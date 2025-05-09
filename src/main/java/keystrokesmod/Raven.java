package keystrokesmod;


import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.clickgui.menu.MainMenu;
import keystrokesmod.feature.command.ToggleCommand;
import keystrokesmod.keystroke.KeySrokeRenderer;
import keystrokesmod.keystroke.KeyStrokeConfigGui;
import keystrokesmod.keystroke.keystrokeCommand;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.script.ScriptManager;
import keystrokesmod.utility.*;
import keystrokesmod.utility.profile.Profile;
import keystrokesmod.utility.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.LWJGLException;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.opengl.Display;
import scala.tools.nsc.transform.patmat.ScalaLogic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Mod(
        modid = "keystrokesmod",
        name = "Neoclient",
        version = "1.1",
        acceptedMinecraftVersions = "1.8.9"
)

public class Raven {
    public static boolean debugger = false;
    public static Minecraft mc = Minecraft.getMinecraft();
    private static KeySrokeRenderer keySrokeRenderer;
    private static boolean isKeyStrokeConfigGuiToggled;
    private static final ScheduledExecutorService ex = Executors.newScheduledThreadPool(2);
    public static ModuleManager moduleManager;
    public static ClickGui clickGui;
    public static MainMenu mainMenu;
    public static ProfileManager profileManager;
    public static ScriptManager scriptManager;
    public static Profile currentProfile;
    public static BadPacketsHandler badPacketsHandler;

    public Raven() {
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

        ByteBuffer[] icons = new ByteBuffer[2];
        try (InputStream inputstream = getInputStreamAssets(new ResourceLocation("keystrokesmod", "textures/gui/ico.png"));
             InputStream inputstream1 = getInputStreamAssets(new ResourceLocation("keystrokesmod", "textures/gui/ico2.png"))) {

            // Read both images into ByteBuffers
            icons[0] = readImageToBuffer(inputstream);
            icons[1] = readImageToBuffer(inputstream1);

            // Set the display icon
            Display.setIcon(icons);
        } catch (IOException exc) {
            System.err.println("Error loading icons: " + exc.getMessage());
            exc.printStackTrace(); // Print stack trace for debugging
        }
    }


    @EventHandler
    public void init(FMLInitializationEvent e) throws IOException, LWJGLException {
        Runtime.getRuntime().addShutdownHook(new Thread(ex::shutdown));
        ClientCommandHandler.instance.registerCommand(new keystrokeCommand());
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new DebugInfoRenderer());
        FMLCommonHandler.instance().bus().register(new CPSCalculator());
        FMLCommonHandler.instance().bus().register(new KeySrokeRenderer());
        FMLCommonHandler.instance().bus().register(new Ping());
        FMLCommonHandler.instance().bus().register(badPacketsHandler = new BadPacketsHandler());

        new ToggleCommand(moduleManager);

        RPC rpc = new RPC();
        rpc.onUpdate();
        // HWIDChecker.main();
        Reflection.getFields();
        Reflection.getMethods();
        moduleManager.register();
        scriptManager = new ScriptManager();
        keySrokeRenderer = new KeySrokeRenderer();
        clickGui = new ClickGui();
        profileManager = new ProfileManager();
        profileManager.loadProfiles();
        profileManager.loadProfile("default");
        Reflection.setKeyBindings();
        scriptManager.loadScripts();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("PostInitializationEvent");
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
            if (Utils.nullCheck()) {
                if (Reflection.sendMessage) {
                    Utils.sendMessage("&cThere was an error, Please relaunch the client.");
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
                for (Profile profile : Raven.profileManager.profiles) {
                    if (mc.currentScreen == null) {
                        profile.getModule().keybind();
                    }
                }
                for (Module module : Raven.scriptManager.scripts.values()) {
                    if (mc.currentScreen == null) {
                        module.keybind();
                    }
                }
            }

            if (isKeyStrokeConfigGuiToggled) {
                isKeyStrokeConfigGuiToggled = false;
                mc.displayGuiScreen(new KeyStrokeConfigGui());
            }
        }
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static ScheduledExecutorService getExecutor() {
        return ex;
    }

    public static KeySrokeRenderer getKeyStrokeRenderer() {
        return keySrokeRenderer;
    }

    public static void toggleKeyStrokeConfigGui() {
        isKeyStrokeConfigGuiToggled = true;
    }
}
