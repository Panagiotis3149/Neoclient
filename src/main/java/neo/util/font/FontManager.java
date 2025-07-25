package neo.util.font;

import neo.util.font.impl.FontRenderer;
import neo.util.font.impl.FontUtil;
import neo.util.font.impl.MinecraftFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import static neo.Neo.mc;

import java.util.HashMap;
import java.util.Map;

public class FontManager {
    public static FontRenderer
            helveticaNeue16, helveticaNeue, helveticaNeue24, helveticaNeueBigger,
            logo, logoa,
            icon20, icon24, newicon20, newicon24, newicon35,
            sfRegular, sf300,
            proximaNova,
            comfortaa,
            greyCliffCF,
            poppinsBold20,
            productSans16, productSans20, productSansLight16, productSansLight22, productSansLight18, productSansMedium, productSansMedium36, productSansLight40, productSansMedium18,
            google, googleMedium, googleRegular20, googleMedium20,
            googleSansMedium, googleSansBold, googleSansRegular16, googleSansRegular20, googleSansMedium40, productSansMedium24;

    public final static String
            BUG = "a",
            LIST = "b",
            BOMB = "c",
            EYE = "d",
            PERSON = "e",
            WHEELCHAIR = "f",
            SCRIPT = "g",
            SKIP_LEFT = "h",
            PAUSE = "i",
            PLAY = "j",
            SKIP_RIGHT = "k",
            SHUFFLE = "l",
            INFO = "m",
            SETTINGS = "n",
            CHECKMARK = "o",
            XMARK = "p",
            TRASH = "q",
            WARNING = "r",
            FOLDER = "s",
            LOAD = "t",
            SAVE = "u",
            UPVOTE_OUTLINE = "v",
            UPVOTE = "w",
            DOWNVOTE_OUTLINE = "x",
            DOWNVOTE = "y",
            DROPDOWN_ARROW = "z",
            PIN = "s",
            EDIT = "A",
            SEARCH = "B",
            UPLOAD = "C",
            REFRESH = "D",
            ADD_FILE = "E",
            STAR_OUTLINE = "F",
            STAR = "G";
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
            comfortaa = new FontRenderer(FontUtil.getResource(locationMap, "comfortaa_regular.ttf", 22));
            sfRegular = new FontRenderer(FontUtil.getResource(locationMap, "sf_pro_rounded_regular.ttf", 22));
            sf300 = new FontRenderer(FontUtil.getResource(locationMap, "sf_pro_rounded_regular.ttf", 300));
            proximaNova = new FontRenderer(FontUtil.getResource(locationMap, "proximanova_regular.ttf", 22));
            greyCliffCF = new FontRenderer(FontUtil.getResource(locationMap, "greycliffcf_regular.otf", 22));
            icon20 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 20));
            icon24 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 24));
            newicon20 = new FontRenderer(FontUtil.getResource(locationMap, "newicon.ttf", 20));
            newicon24 = new FontRenderer(FontUtil.getResource(locationMap, "newicon.ttf", 24));
            newicon20 = new FontRenderer(FontUtil.getResource(locationMap, "newicon.ttf", 35));
            helveticaNeue = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 20));
            helveticaNeue16 = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 16));
            helveticaNeueBigger = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 28));
            helveticaNeue24 = new FontRenderer(FontUtil.getResource(locationMap, "helveticaneue.ttf", 24));
            logo = new FontRenderer(FontUtil.getResource(locationMap, "is.otf", 42));
            logoa = new FontRenderer(FontUtil.getResource(locationMap, "is.otf", 68));
            productSans16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 16));
            productSans20 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 20));
            productSansLight22 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 22));
            productSansLight40 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 40));
            productSansLight16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 16));
            productSansLight18 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 18));
            productSansMedium = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 22));
            productSansMedium36 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 36));
            productSansMedium18 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 18));
            productSansMedium24 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 24));
        }
    }
    public static MinecraftFontRenderer getMinecraft() {
        return MinecraftFontRenderer.INSTANCE;
    }

}