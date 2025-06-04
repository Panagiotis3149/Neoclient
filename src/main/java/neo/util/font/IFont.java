package neo.util.font;

public interface IFont {

    double drawString(String text, double x, double y, int color, boolean dropShadow);

    double drawString(String text, double x, double y, int color);

    default double drawStringWithShadow(String text, double x, double y, int color) {
        return drawString(text, x, y, color, true);
    }

    double width(String text);

    double height();
}
