package neo.module.impl.other.anticheat.impl;

import neo.module.impl.other.Anticheat;
import neo.module.impl.other.anticheat.AnticheatComponent;
import neo.util.world.block.BlockUtils;
import neo.util.player.move.PlayerData;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;

public class ScaffoldCheck implements AnticheatComponent {
    @Override
    public void check(EntityPlayer player, PlayerData data, Anticheat parent) {
        if (!parent.scaffold.isToggled()) return;
        if (!player.isSwingInProgress) return;
        if (player.rotationPitch < 70.0f) return;
        if (player.getHeldItem() == null) return;
        if (!(player.getHeldItem().getItem() instanceof ItemBlock)) return;
        if (data.fastTick < 20) return;
        if (player.ticksExisted - data.lastSneakTick < 30) return;
        if (player.ticksExisted - data.aboveVoidTicks < 20) return;

        boolean overAir = true;
        BlockPos pos = player.getPosition().down(2);
        for (int i = 0; i < 4; i++) {
            if (!(BlockUtils.getBlock(pos) instanceof BlockAir)) {
                overAir = false;
                break;
            }
            pos = pos.down();
        }

        if (overAir) {
            parent.alert(player, parent.scaffold);
        }
    }
}
