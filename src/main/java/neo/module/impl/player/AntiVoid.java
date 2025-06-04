package neo.module.impl.player;


import neo.event.PreMotionEvent;
import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.world.block.BlockUtils;
import neo.util.Utils;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AntiVoid extends Module {
    private final SliderSetting mode;
    public static String[] modes = new String[]{"Basic", "Position"};
    public final List<Packet<?>> blinkedPackets = new ArrayList();
    public BlockPos lastSafePos;
    public boolean blink, b1;


    public AntiVoid() {
        super("AntiVoid", category.player, 0);
        registerSetting(mode = new SliderSetting("Mode", modes, 0));
    }



    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        switch ((int) mode.getInput()) {
            case 0:
                BlockPos bp = new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY - 1.0, mc.thePlayer.posZ);
                if ((double)mc.thePlayer.fallDistance > 5 && Utils.overVoid() && lastSafePos != null) {
                    mc.thePlayer.setPosition(lastSafePos.getX(), lastSafePos.getY(), lastSafePos.getZ());
                }
                if (Utils.overVoid()) {
                    if (mc.thePlayer.onGround) {

                    }
                } else {
                    if (mc.thePlayer.onGround) {
                        lastSafePos = mc.thePlayer.getPosition();
                    }
                }
            break;
            case 1:
                if (mc.thePlayer.fallDistance > 5 && !BlockUtils.isBlockUnder()) {
                    e.setPosY(e.getPosY() + mc.thePlayer.fallDistance);
                }
                break;
        }

    }

}

