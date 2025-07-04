package neo.mixins.impl.entity;

import neo.module.ModuleManager;
import neo.module.impl.render.ItemPhysics;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.util.MathHelper.sin;
import static org.lwjgl.opengl.GL11.*;

@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem extends Render<EntityItem> {
    protected MixinRenderEntityItem(final RenderManager p_i46179_1_) {
        super(p_i46179_1_);
    }

    /**
     * @author Eclipses
     * Modified by @Panagiotis3149 to work with neoclient.
     * @reason for ItemPhysics Module
     * Original simplified code by FDPClient & Modified by Eclipses:
     * https://github.com/SkidderMC/FDPClient/blob/main/src/main/java/net/ccbluex/liquidbounce/injection/forge/mixins/render/MixinRenderEntityItem.java
     *
     * Original code from:
     * https://github.com/CreativeMD/ItemPhysic/blob/1.8.9/src/main/java/com/creativemd/itemphysic/physics/ClientPhysic.java
     */
    @Overwrite
    private int func_177077_a(EntityItem itemIn, double x, double y, double z, float p_177077_8_, IBakedModel ibakedmodel) {
        final ItemPhysics itemPhysics = ModuleManager.itemPhysics;

        ItemStack itemStack = itemIn.getEntityItem();
        Item item = itemStack.getItem();

        if (item == null || itemStack == null) {
            return 0;
        }

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        boolean isGui3d = ibakedmodel.isGui3d();
        int count = getItemCount(itemStack);
        float yOffset = 0.25F;

        float age = (float) itemIn.getAge() + p_177077_8_;
        float hoverStart = itemIn.hoverStart;
        boolean isPhysicsState = itemPhysics.isEnabled();
        boolean isRealistic = false;
        float weight = isPhysicsState ? 0.45f : 0.0f;

        float sinValue = sin((age / 10.0F + hoverStart)) * 0.1F + 0.1F;
        if (isPhysicsState) {
            sinValue = 0.0f;
        }
        float scaleY = ibakedmodel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;

        if (isPhysicsState) {
            translate((float)x, (float)y, (float)z);
        } else {
            translate((float) x, (float) y + sinValue + yOffset * scaleY, (float) z);
        }

        if (isGui3d) {
            translate(0, 0, -0.08);
        } else {
            translate(0, 0, -0.04);
        }

        if (isGui3d || this.renderManager.options != null) {
            float rotationYaw = (age / 20.0F + hoverStart) * (180F / (float) Math.PI);

            rotationYaw *= 1 * (1.0F + Math.min(age / 360.0F, 1.0F));

            if (isPhysicsState) {
                if (itemIn.onGround) {
                    GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                    if (!isRealistic) {
                        GL11.glRotatef(itemIn.rotationYaw, 0.0f, 0.0f, 1.0f);
                    } else {
                        GL11.glRotatef(itemIn.rotationYaw, 0.0f, 1.0f, 0.6f);
                    }
                } else {
                    for (int a = 0; a < 7; ++a) {
                        GL11.glRotatef(rotationYaw, weight, weight, 1.35f);
                    }
                }
            } else {
                rotate(rotationYaw, 0.0F, 1.0F, 0.0F);
            }
        }

        if (!isGui3d) {
            float offsetX = -0.0F * (float) (count - 1) * 0.5F;
            float offsetY = -0.0F * (float) (count - 1) * 0.5F;
            float offsetZ = -0.09375F * (float) (count - 1) * 0.5F;
            translate(offsetX, offsetY, offsetZ);
        }

        glDisable(GL_CULL_FACE);

        color(1.0F, 1.0F, 1.0F, 1.0F);
        return count;
    }

    private int getItemCount(ItemStack stack) {
        int size = stack.stackSize;

        if (size > 48) {
            return 5;
        } else if (size > 32) {
            return 4;
        } else if (size > 16) {
            return 3;
        } else if (size > 1) {
            return 2;
        }

        return 1;
    }
}