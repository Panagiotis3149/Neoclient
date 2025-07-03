package neo.module.impl.other.anticheat;

import neo.module.impl.other.Anticheat;
import neo.util.player.move.PlayerData;
import net.minecraft.entity.player.EntityPlayer;

public interface AnticheatComponent {
    void check(EntityPlayer player, PlayerData data, Anticheat parent);
}
