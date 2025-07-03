package neo.module.impl.other.anticheat.impl;

import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.Utils;
import neo.util.player.move.PlayerData;
import net.minecraft.entity.player.EntityPlayer;

public class NoFallCheck implements AnticheatComponent {
    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat parent) {
        if (!parent.noFall.isToggled()) return;
        if (player.capabilities.isFlying) return;

        double serverPosX = player.serverPosX / 32.0;
        double serverPosY = player.serverPosY / 32.0;
        double serverPosZ = player.serverPosZ / 32.0;

        double deltaX = Math.abs(data.serverPosX - serverPosX);
        double deltaY = data.serverPosY - serverPosY;
        double deltaZ = Math.abs(data.serverPosZ - serverPosZ);

        if (deltaY >= 5 && deltaX <= 10 && deltaZ <= 10 && deltaY <= 40) {
            if (!Utils.overVoid(serverPosX, serverPosY, serverPosZ) && Utils.getFallDistance(player) > 3) {
                parent.alert(player, parent.noFall);
            }
        }
    }
}
