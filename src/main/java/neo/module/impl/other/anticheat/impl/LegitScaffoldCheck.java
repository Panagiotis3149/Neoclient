package neo.module.impl.other.anticheat.impl;

import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.player.move.PlayerData;
import net.minecraft.entity.player.EntityPlayer;

public class LegitScaffoldCheck implements AnticheatComponent {
    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat parent) {
        if (!parent.legitScaffold.isToggled()) return;
        if (data.sneakTicks >= 3) {
            parent.alert(player, parent.legitScaffold);
        }
    }
}
