package keystrokesmod.utility.font;

import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.font.impl.FontUtil;
import keystrokesmod.utility.font.impl.MinecraftFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import static keystrokesmod.Raven.mc;

import java.util.HashMap;
import java.util.Map;

public class FontManager {
    public static FontRenderer
            regular16, regular20, regular22, regular40,
            helveticaNeue16, helveticaNeue, helveticaNeue24, helveticaNeueBigger,
            intentStore,
            icon20, icon24,
            ptSans24,
            productSans16, productSans20, productSansLight16, productSansLight22, productSansMedium,
            tenacity16, tenacity20, tenacity80,
            google, googleMedium, googleRegular20, googleMedium20,
            googleSansMedium, googleSansBold, googleSansRegular16, googleSansRegular20, googleSansMedium16;

    private static int prevScale;

    static {
        Map<String, java.awt.Font> locationMap = new HashMap<>();

        ScaledResolution sr = new ScaledResolution(mc);

        int scale = sr.getScaleFactor();

        if (scale != prevScale) {
            prevScale = scale;
            google = new FontRenderer(FontUtil.getResource(locationMap, "GoogleRegular.ttf", 16));
            googleSansRegular16 = new FontRenderer(FontUtil.getResource(locationMap, "googlesansregular.ttf", 16));
            googleSansBold = new FontRenderer(FontUtil.getResource(locationMap, "googlesansbold.ttf", 20));
            googleSansMedium = new FontRenderer(FontUtil.getResource(locationMap, "googlesansmedium.ttf", 20));
            googleSansRegular16 = new FontRenderer(FontUtil.getResource(locationMap, "googlesansmedium.ttf", 16));
            googleRegular20 = new FontRenderer(FontUtil.getResource(locationMap, "GoogleRegular.ttf", 20));
            googleMedium = new FontRenderer(FontUtil.getResource(locationMap, "GoogleMedium.ttf", 18));
            googleMedium20 = new FontRenderer(FontUtil.getResource(locationMap, "GoogleMedium.ttf", 20));
            regular16 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 16));
            regular22 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 22));
            regular20 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 20));
            regular40 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 40));
            icon20 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 20));
            icon24 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 24));
            helveticaNeue = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 20));
            helveticaNeue16 = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 16));
            helveticaNeueBigger = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 28));
            helveticaNeue24 = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 24));
            intentStore = new FontRenderer(FontUtil.getResource(locationMap, "is.otf", 42));
            ptSans24 = new FontRenderer(FontUtil.getResource(locationMap, "ptsans.ttf", 22));
            productSans16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 16));
            productSans20 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 20));
            productSansLight22 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 22));
            productSansLight16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 16));
            productSansMedium = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 22));
            tenacity16 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 16));
            tenacity20 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 20));
            tenacity80 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 80));
        }
    }
    public static MinecraftFontRenderer getMinecraft() {
        return MinecraftFontRenderer.INSTANCE;
    }

}