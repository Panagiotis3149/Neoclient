package neo.module.impl.player;

import neo.event.PreMotionEvent;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.other.java.Reflection;
import neo.util.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall extends Module {
    public SliderSetting mode;
    private final SliderSetting minFallDistance;
    private final ButtonSetting disableAdventure;
    private final ButtonSetting ignoreVoid;
    private final String[] modes = new String[]{"Spoof", "Extra", "NoGround"};

    public NoFall() {
        super("NoFall", category.player);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(minFallDistance = new SliderSetting("Minimum fall distance", 3, 0, 10, 0.1));
        this.registerSetting(disableAdventure = new ButtonSetting("Disable adventure", false));
        this.registerSetting(ignoreVoid = new ButtonSetting("Ignore void", true));
    }

    public void onDisable() {
        Utils.resetTimer();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent e) {
        Utils.resetTimer();
        if (disableAdventure.isToggled() && mc.playerController.getCurrentGameType().isAdventure()) {
            return;
        }
        if (ignoreVoid.isToggled() && isVoid()) {
            return;
        }
        if (((double) mc.thePlayer.fallDistance > minFallDistance.getInput() || minFallDistance.getInput() == 0) || mode.getInput() == 2) {
            switch ((int) mode.getInput()) {
                case 0:
                    e.setOnGround(true);
                    break;
                case 1:
                    float fallDistance = 0;
                    try {
                        fallDistance = Reflection.fallDistance.getFloat(mc.thePlayer);
                    }
                    catch (Exception exception) {
                        Utils.sendMessage("&cFailed to get fall distance.");
                    }
                    if (fallDistance > minFallDistance.getInput()) {
                        Utils.getTimer().timerSpeed = (float) 0.5;
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                        try {
                            Reflection.fallDistance.setFloat(mc.thePlayer, 0);
                        }
                        catch (Exception exception) {
                            Utils.sendMessage("&cFailed to set fall distance to 0.");
                        }
                    }
                    break;
                case 2:
                    e.setOnGround(false);
                    break;
            }
        }
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    private boolean isVoid() {
        return Utils.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }
}
