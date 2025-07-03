package neo.module.impl.other.anticheat.impl;

import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.player.move.PlayerData;
import net.minecraft.entity.player.EntityPlayer;

public class NoSlowCheck implements AnticheatComponent {
    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat parent) {
        if (!parent.noSlow.isToggled()) return;
        if (data.noSlowTicks >= 11 && data.speed >= 0.08) {
            parent.alert(player, parent.noSlow);
        }
    }
}
