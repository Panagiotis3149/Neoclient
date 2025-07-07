package neo.util.render;

import neo.util.render.animation.DecelerateAnimation;
import neo.util.render.animation.Direction;
import neo.util.render.animation.Anim;
import net.minecraft.client.renderer.GlStateManager;

public class ClickCircle {

    private final Anim scaleAnim = new DecelerateAnimation(150, 1, Direction.FORWARDS);
    private final Anim fadeAnim = new DecelerateAnimation(300, 1, Direction.FORWARDS);
    private final int x, y;

    public ClickCircle(int x, int y) {
        this.x = x;
        this.y = y;
        scaleAnim.setDirection(Direction.FORWARDS);
        fadeAnim.setDirection(Direction.BACKWARDS); // start full opacity
    }

    public void drawScreen(int baseColor) {
        if (scaleAnim.isDone() && scaleAnim.getDirection() == Direction.FORWARDS) {
            // When expansion is done, start fade out + shrink
            scaleAnim.setDirection(Direction.BACKWARDS);
            fadeAnim.setDirection(Direction.FORWARDS);
        }

        float scale = lerp(0.5f, 5f, scaleAnim.getOutput().floatValue());
        float alpha = lerp(1f, 0f, fadeAnim.getOutput().floatValue());

        int fadedColor = applyAlpha(baseColor, alpha);

        GlStateManager.alphaFunc(516, 0.15f);
        GlStateManager.color(1, 1, 1, 1);
        RenderUtils.drawUnfilledCircle(x, y, scale, 4, fadedColor);
        GlStateManager.alphaFunc(516, 0.1f);
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255f);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public boolean isDone() {
        return fadeAnim.isDone() && fadeAnim.getDirection() == Direction.FORWARDS;
    }
}
