package neo.mixins.impl.performance;

import com.google.common.collect.Iterators;
import neo.util.other.java.mixin.WorldExt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Iterator;

@Mixin(Chunk.class)
public abstract class MixinChunk implements WorldExt {


    @Shadow
    @Final
    private ExtendedBlockStorage[] storageArrays;

    @Shadow
    @Final
    private World worldObj;


    public IBlockState getOptimizedBlockState(BlockPos pos) {
        int y = pos.getY();
        ExtendedBlockStorage[] storageArray = storageArrays;
        if (y >= 0 && (y >> 4) < storageArray.length) {
            ExtendedBlockStorage storage = storageArray[y >> 4];
            if (storage != null) {
                return storage.get(pos.getX() & 15, y & 15, pos.getZ() & 15);
            }
        }
        return Blocks.air.getDefaultState();
    }

    @ModifyArg(
            method = "setBlockState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;relightBlock(III)V", ordinal = 0),
            index = 1
    )

    @Redirect(method = "onChunkUnload", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;", ordinal = 0))
    private Iterator unloadTileEntity(Collection instance) {
        ((WorldExt) this.worldObj).markTileEntitiesInChunkForRemoval((Chunk)(Object)this);
        return Iterators.emptyIterator();
    }


    @Overwrite
    public IBlockState getBlockState(BlockPos pos) {
        return getOptimizedBlockState(pos);
    }
}