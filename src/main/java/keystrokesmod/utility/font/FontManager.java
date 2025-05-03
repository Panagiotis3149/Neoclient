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
            logo, logoa,
            icon20, icon24,
            ptSans24,
            sfRegular20,
            greyCliffCF20,
            poppinsBold20,
            productSans16, productSans20, productSansLight16, productSansLight22, productSansMedium, productSansMedium36, productSansLight40, productSansMedium18,
            tenacity16, tenacity20, tenacity80,
            google, googleMedium, googleRegular20, googleMedium20,
            googleSansMedium, googleSansBold, googleSansRegular16, googleSansRegular20, googleSansMedium40;

    private static int prevScale;

    static {
        Map<String, java.awt.Font> locationMap = new HashMap<>();

        ScaledResolution sr = new ScaledResolution(mc);

        int scale = sr.getScaleFactor();

        if (scale != prevScale) {
            prevScale = scale;
            google = new FontRenderer(FontUtil.getResource(locationMap, "googleregular.ttf", 16));
            poppinsBold20 = new FontRenderer(FontUtil.getResource(locationMap, "poppins_bold.ttf", 20));
            googleSansRegular16 = new FontRenderer(FontUtil.getResource(locationMap, "google_sans_regular.ttf", 16));
            googleSansBold = new FontRenderer(FontUtil.getResource(locationMap, "google_sans_bold.ttf", 20));
            googleSansMedium = new FontRenderer(FontUtil.getResource(locationMap, "google_sans_medium.ttf", 20));
            googleSansRegular16 = new FontRenderer(FontUtil.getResource(locationMap, "google_sans_regular.ttf", 16));
            googleSansRegular20 = new FontRenderer(FontUtil.getResource(locationMap, "google_sans_regular.ttf", 20));
            googleSansMedium40 = new FontRenderer(FontUtil.getResource(locationMap, "google_sans_medium.ttf", 40));
            googleRegular20 = new FontRenderer(FontUtil.getResource(locationMap, "googleregular.ttf", 20));
            googleMedium = new FontRenderer(FontUtil.getResource(locationMap, "googlemedium.ttf", 18));
            googleMedium20 = new FontRenderer(FontUtil.getResource(locationMap, "googlemedium.ttf", 20));
            regular16 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 16));
            regular22 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 22));
            regular20 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 20));
            regular40 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 40));
            sfRegular20 = new FontRenderer(FontUtil.getResource(locationMap, "sf_pro_rounded_regular.ttf", 20)); 
            greyCliffCF20 = new FontRenderer(FontUtil.getResource(locationMap, "greycliffcf_regular.otf", 20));
            icon20 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 20));
            icon24 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 24));
            helveticaNeue = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 20));
            helveticaNeue16 = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 16));
            helveticaNeueBigger = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 28));
            helveticaNeue24 = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 24));
            logo = new FontRenderer(FontUtil.getResource(locationMap, "is.otf", 42));
            logoa = new FontRenderer(FontUtil.getResource(locationMap, "is.otf", 68));
            ptSans24 = new FontRenderer(FontUtil.getResource(locationMap, "ptsans.ttf", 22));
            productSans16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 16));
            productSans20 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 20));
            productSansLight22 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 22));
            productSansLight40 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 40));
            productSansLight16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 16));
            productSansMedium = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 22));
            productSansMedium36 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 36));
            productSansMedium18 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 18));
            tenacity16 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 16));
            tenacity20 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 20));
            tenacity80 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 80));
        }
    }
    public static MinecraftFontRenderer getMinecraft() {
        return MinecraftFontRenderer.INSTANCE;
    }

}