package neo.module.impl.other.anticheat.impl;

import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.player.move.PlayerData;
import net.minecraft.entity.player.EntityPlayer;

public class AutoblockCheck implements AnticheatComponent {
    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat parent) {
        if (!parent.autoBlock.isToggled()) return;
        if (data.autoBlockTicks >= 10) {
            parent.alert(player, parent.autoBlock);
        }
    }
}
