package neo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NeoCloud {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static String onlineVersion = "";
    public static String onlinePretty = "";

    public static final String URL = "https://raw.githubusercontent.com/Panagiotis3149/NeoCloud/main";
    public static final String VERSION_URL = URL + "/data/version.ver";
    public static final String CONFIGS_URL = URL + "/configs/";

    public NeoCloud() {
    }

    public static void checkVersionAsync() {
        if (Variables.DEVELOPMENT_SWITCH) {
            return;
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String rawVersion = fetchUrl(VERSION_URL);
                    String[] lines = rawVersion.split("\\r?\\n");
                    if (lines.length < 1) {
                        System.out.println("NeoCloud version file empty or invalid");
                        return;
                    }

                    String onlineVerNumber = lines[0].trim();
                    String onlineVerTag = lines.length > 1 ? lines[1].trim() : "";
                    onlinePretty = lines.length > 2 ? lines[2].trim() : "";

                    System.out.println("NeoCloud online version: " + onlineVerNumber + " " + onlineVerTag);

                    String localVersion = Variables.CLIENT_FULL_VERSION;
                    String localTag = Variables.BUILD_TYPE != null ? Variables.BUILD_TYPE : "";

                    if (isVersionOutdated(localVersion, onlineVerNumber)) {
                        Variables.OUTDATED = true;
                    } else if (localVersion.equals(onlineVerNumber)) {
                        int cmp = compareTags(localTag, onlineVerTag);
                        Variables.OUTDATED = cmp < 0;
                    }

                    onlineVersion = onlineVerNumber + (onlineVerTag.isEmpty() ? "" : " " + onlineVerTag);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static boolean isVersionOutdated(String localVersion, String onlineVersion) {
        if (localVersion == null || onlineVersion == null) {
            return false;
        }

        String[] localParts = localVersion.split("\\.");
        String[] onlineParts = onlineVersion.split("\\.");

        int length = Math.max(localParts.length, onlineParts.length);

        for (int i = 0; i < length; i++) {
            int localPart = i < localParts.length ? tryParseInt(localParts[i]) : 0;
            int onlinePart = i < onlineParts.length ? tryParseInt(onlineParts[i]) : 0;

            if (localPart < onlinePart) {
                return true;
            } else if (localPart > onlinePart) {
                return false;
            }
        }
        return false;
    }

    private static int compareTags(String localTag, String onlineTag) {
        int localRank = tagRank(localTag);
        int onlineRank = tagRank(onlineTag);

        if (localRank != onlineRank) {
            return localRank - onlineRank;
        }

        if (localTag.startsWith("PRE") && onlineTag.startsWith("PRE")) {
            int localNum = extractPreNumber(localTag);
            int onlineNum = extractPreNumber(onlineTag);
            return localNum - onlineNum;
        }

        return 0;
    }

    private static int tagRank(String tag) {
        tag = tag.toUpperCase();

        if (tag.equals("BETA")) return 1;
        if (tag.startsWith("PRE")) return 2;
        if (tag.equals("RELEASE")) return 3;
        if (tag.equals("DEV")) return 4;

        return 0;
    }

    private static int extractPreNumber(String tag) {
        try {
            return Integer.parseInt(tag.substring(3));
        } catch (Exception e) {
            return 0;
        }
    }

    private static int tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        reader.close();
        return result.toString().trim();
    }

    public static ScheduledExecutorService getExecutor() {
        return executor;
    }
}
