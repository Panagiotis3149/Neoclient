package neo.mixins.impl.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.gui.GuiDisconnected;

@Mixin(GuiDisconnected.class)
public interface GuiDisconnectedAccessor {
    @Accessor("parentScreen")
    GuiScreen getParentScreen();

    @Accessor("reason")
    String getReason();

    @Accessor("message")
    IChatComponent getMessage();
}
