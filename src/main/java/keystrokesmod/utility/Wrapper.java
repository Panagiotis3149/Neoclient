package keystrokesmod.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Session;

import java.lang.reflect.Field;

/*
 * @author ReesZRB
 */
public class Wrapper {
    private static Wrapper theWrapper = new Wrapper();
    private static Minecraft mc = Minecraft.getMinecraft();



    public static final Wrapper getWrapper() {
        if(theWrapper == null)theWrapper = new Wrapper();
        return theWrapper;
    }

    public final Minecraft getMinecraft(){
        return mc;
    }

    public final PlayerControllerMP getPlayerController(){
        return mc.playerController;
    }

    public final WorldClient getWorld(){
        return mc.theWorld;
    }

    public final RenderGlobal getRenderGlobal(){
        return mc.renderGlobal;
    }


    public final EffectRenderer getEffectRenderer(){
        return mc.effectRenderer;
    }


    public final GuiScreen getCurrentScreen(){
        return mc.currentScreen;
    }

    public final EntityRenderer getEntityRenderer(){
        return mc.entityRenderer;
    }

    public final GameSettings getGameSettings(){
        return mc.gameSettings;
    }

    public final TextureManager getTextureManager(){
        return mc.getTextureManager();
    }

    public static final void setSession(Session s) {
        try {
            Field sessionField = Minecraft.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            Session session = (Session) sessionField.get(mc);

            sessionField.set(mc, s);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}