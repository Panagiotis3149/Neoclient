package neo.util.other.java.mixin;


import net.minecraft.world.chunk.Chunk;

public interface WorldExt {
    void markTileEntitiesInChunkForRemoval(Chunk chunk);
}
