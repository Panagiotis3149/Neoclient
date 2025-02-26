package keystrokesmod.utility.render;

import keystrokesmod.utility.Container;
import keystrokesmod.utility.RenderUtils;
import net.minecraft.util.MathHelper;
import java.awt.*;


public class ClickCircle extends Container<ClickCircle.Circle> {

    public void render() {
        this.getItems().removeIf(circle -> circle.getAlpha() <= 0);

        this.getItems().forEach(Circle::render);
    }

    public void addCircle(double x, double y, double startRadius, double maxRadius, Color color) {
        this.getItems().add(new Circle(x, y, startRadius, maxRadius, color));
    }

    public static class Circle {
        private final double x;
        private final double y;
        private final double startRadius;
        private final double maxRadius;
        private final double speed;
        private final Color color;

        private double radius;
        private int alpha = 255;

        public Circle(double x, double y, double startRadius, double maxRadius, Color color) {
            this.x = x;
            this.y = y;
            this.startRadius = startRadius;
            this.maxRadius = maxRadius;
            double calculatedSpeed = (maxRadius - startRadius) / 34.0;
            this.speed = calculatedSpeed * RenderUtils.fpsMultiplier();
            this.color = color;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }


        public double getSpeed() {
            return speed;
        }

        public Color getColor() {
            return color;
        }

        public double getRadius() {
            return radius;
        }

        public int getAlpha() {
            return alpha;
        }

        public void render() {
            this.radius += this.speed * RenderUtils.fpsMultiplier();
            this.radius = MathHelper.clamp_double(this.radius, this.startRadius, this.maxRadius);

            if (this.radius >= (this.maxRadius / 2)) {
                this.alpha -= (int) (25 * RenderUtils.fpsMultiplier());
                this.alpha = MathHelper.clamp_int(this.alpha, 0, 255);
            }

            RenderUtils.drawRoundedOutline(
                    (float) ((float) this.x - (this.radius / 2f)), (float) (this.y - (this.radius / 2f)),
                    (float) this.radius, (float) this.radius, (float) (radius / 2), 3f,
                    RenderUtils.toARGBInt(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.alpha))
            );
        }
    }
}
