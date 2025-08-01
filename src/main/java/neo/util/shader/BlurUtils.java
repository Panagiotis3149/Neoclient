package neo.util.shader;

import neo.util.render.RenderUtils;
import net.minecraft.client.shader.Framebuffer;


public class BlurUtils {
        private static Framebuffer stencilFrameBufferBlur = new Framebuffer(1, 1, false);
        private static Framebuffer stencilFrameBufferBloom = new Framebuffer(1, 1, false);

        public static void prepareBlur() {
            stencilFrameBufferBlur = RenderUtils.createFrameBuffer(stencilFrameBufferBlur);
            stencilFrameBufferBlur.framebufferClear();
            stencilFrameBufferBlur.bindFramebuffer(false);
        }


        public static void blurEnd(int passes, float radius) {
            stencilFrameBufferBlur.unbindFramebuffer();
            KawaseBlur.renderBlur(stencilFrameBufferBlur.framebufferTexture, passes, radius);
        }

        /**
         * Prepare to capture scene for bloom effect.
         */
        public static void prepareBloom() {
            stencilFrameBufferBloom = RenderUtils.createFrameBuffer(stencilFrameBufferBloom);
            stencilFrameBufferBloom.framebufferClear();
            stencilFrameBufferBloom.bindFramebuffer(false);
        }

        /**
         * Apply bloom effect on captured texture.
         * @param passes number of bloom iterations
         * @param radius bloom radius
         */
        public static void bloomEnd(int passes, float radius) {
            stencilFrameBufferBloom.unbindFramebuffer();
            KawaseBloom.renderBloom(stencilFrameBufferBloom.framebufferTexture, passes, radius);
        }

}
