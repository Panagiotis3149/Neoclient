package neo.module.impl.other;

import neo.module.Module;
import neo.util.Utils;
import neo.util.player.move.MoveUtil;

public class SpeedTest extends Module {
    long lastUpdate = 0;

    public SpeedTest() {
        super("SpeedTest", category.other, 0);
    }

    @Override
    public void onUpdate() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate >= 1000) {
            lastUpdate = now;
            double percent = Utils.round3((MoveUtil.getSpeed() / MoveUtil.defaultSpeedBase(0.3)) * 100);
            double bps = Utils.round3(Utils.rawBPS(mc.thePlayer));
            Utils.sendMessage("&b" + percent + "% Compared to Vanilla MAX");
            Utils.sendMessage("&bBPS: " + bps);
        }
    }


}