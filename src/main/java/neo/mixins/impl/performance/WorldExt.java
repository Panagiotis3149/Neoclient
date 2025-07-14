package neo.mixins.impl.performance;


import net.minecraft.world.chunk.Chunk;

public interface WorldExt {
    void markTileEntitiesInChunkForRemoval(Chunk chunk);
}
