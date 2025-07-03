package neo.mixins.impl.render;

import neo.module.ModuleManager;
import neo.module.impl.combat.AntiBot;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> extends Render<T> {
    @Shadow
    protected ModelBase mainModel;

    protected MixinRendererLivingEntity(RenderManager p_i46156_1_, ModelBase p_i46156_2_, float p_i46156_3_) {
        super(p_i46156_1_);
        this.mainModel = p_i46156_2_;
        this.shadowSize = p_i46156_3_;
    }
}
