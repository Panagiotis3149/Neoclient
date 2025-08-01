package neo.mixins.impl.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("isInWeb")
    boolean isInWeb();

    @Accessor("fire")
    int getFire();
}