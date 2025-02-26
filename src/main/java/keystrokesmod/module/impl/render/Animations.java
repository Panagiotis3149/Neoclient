package keystrokesmod.module.impl.render;

import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.render.ItemRendererAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Animations extends Module {
    private static final ButtonSetting swingWhileDigging = new ButtonSetting("Swing while digging", true);
    private static final ButtonSetting clientSide = new ButtonSetting("Client side (visual 1.7)", true);
    private final SliderSetting blockAnimation = new SliderSetting("Block animation", new String[]{"None", "1.7", "Leaked"}, 1);
    private final SliderSetting x = new SliderSetting("X", 0, -1, 1, 0.05);
    private final SliderSetting y = new SliderSetting("Y", 0, -1, 1, 0.05);
    private final SliderSetting z = new SliderSetting("Z", 0, -1, 1, 0.05);
    private final SliderSetting swingSpeed = new SliderSetting("Swing speed", 0, -200, 50, 5);
    private final SliderSetting swingSpeedWhileBlocking = new SliderSetting("Swing speed while blocking", 0, -200, 50, 5);
    private int swing;

    public Animations() {
        super("Animations", category.render);
        registerSetting(blockAnimation, swingWhileDigging, clientSide, x, y, z, swingSpeed, swingSpeedWhileBlocking);
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (Utils.nullCheck() && swingWhileDigging.isToggled() && clientSide.isToggled() && event.getPacket() instanceof C0APacketAnimation && mc.thePlayer.isUsingItem()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderItem(@NotNull RenderItemEvent event) {
        if (event.getItemToRender().getItem() instanceof ItemMap) return;
        try {
            EnumAction itemAction = event.getEnumAction();
            ItemRendererAccessor itemRenderer = (ItemRendererAccessor) mc.getItemRenderer();
            float animationProgression = event.getAnimationProgression();
            float swingProgress = event.getSwingProgress();
            float convertedProgress = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);

            applyTranslation(x.getInput(), y.getInput(), z.getInput());

            if (event.isUseItem() && itemAction == EnumAction.BLOCK) {
                handleBlockAnimation(itemRenderer, animationProgression, swingProgress, convertedProgress);
                event.setCanceled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBlockAnimation(ItemRendererAccessor itemRenderer, float animationProgression, float swingProgress, float convertedProgress) {
        switch ((int) blockAnimation.getInput()) {
            case 0:
                itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                itemRenderer.blockTransformation();
            break;
            case 1:
                itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                itemRenderer.blockTransformation();
                break;
                case 2:
                itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                float yOffset = -convertedProgress * 2.0F;
                applyTranslation(0.0F, yOffset / 10.0F + 0.1F, 0.0F);
                applyRotation(yOffset);
                break;
                case 3:
                GlStateManager.translate(.0f, -.03f, -.13f);
                itemRenderer.transformFirstPersonItem(animationProgression / 3F, 0.0F);
                GlStateManager.translate(0.0f, 0.1F, 0.0F);
                itemRenderer.blockTransformation();
                GlStateManager.rotate(convertedProgress * 10.0F, 0.0F, 1.0F, 1.5F);
                GlStateManager.rotate(-convertedProgress * 50.0F, 1.0f, 0.9F, 0.0F);
                break;
        }
    }

    private void applyRotation(float yOffset) {
        GlStateManager.rotate(yOffset * 10.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(250, 0.2F, 1.0F, -0.6F);
        GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
        GlStateManager.rotate(-yOffset * 20.0F, 1.0F, 0.5F, 1.0F);
    }

    private void applyTranslation(double x, double y, double z) {
        GlStateManager.translate(x + this.x.getInput(), y + this.y.getInput(), z + this.z.getInput());
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.swingProgressInt == 1) swing = 9;
        else swing = Math.max(0, swing - 1);
    }

    @SubscribeEvent
    public void onSwingAnimation(@NotNull SwingAnimationEvent event) {
        float speedModifier = (float) (mc.thePlayer.isUsingItem() || mc.thePlayer.getItemInUseCount() == 1
                        ? (-swingSpeedWhileBlocking.getInput() / 100) + 1
                        : (-swingSpeed.getInput() / 100) + 1);
        event.setAnimationEnd((int) (event.getAnimationEnd() * speedModifier));
    }
}
