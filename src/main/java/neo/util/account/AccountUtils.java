package neo.util.account;

import com.google.gson.Gson;
import fr.litarvan.openauth.microsoft.AuthTokens;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import neo.mixins.impl.client.MinecraftAccessor;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;


public class AccountUtils {
    private static final MicrosoftAuthenticator auth = new MicrosoftAuthenticator();

    public static String[] splitCreds(String input) {
        return input.split(":", 2);
    }


    public static MicrosoftAuthResult authenticateWithCredentials(String email, String password) throws MicrosoftAuthenticationException {
            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
            return authenticator.loginWithCredentials(email, password);
    };

    public static MicrosoftAuthResult authenticateWithWebView() throws MicrosoftAuthenticationException {
            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
            return authenticator.loginWithWebview();
    };



    public static void cookieLogin(Consumer<MicrosoftAuthResult> callback, Consumer<String> errorCallback) {
        SwingUtilities.invokeLater(() -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    List<String> lines = Files.readAllLines(chooser.getSelectedFile().toPath());
                    loginWithCookieAsync(lines.toArray(new String[0]), callback, errorCallback);
                } catch (Exception e) {
                    errorCallback.accept("Failed to read cookie file: " + e.getMessage());
                }
            } else {
                errorCallback.accept("File selection cancelled");
            }
        });
    }

    public static void loginWithCookieAsync(String[] cookieLines, Consumer<MicrosoftAuthResult> callback, Consumer<String> errorCallback) {
        new Thread(() -> {
            try {
                MicrosoftAuthResult account = loginWithCookie(cookieLines);
                callback.accept(account);
            } catch (Exception e) {
                errorCallback.accept(e.getMessage());
            }
        }, "Cookie Login Thread").start();
    }

    public static MicrosoftAuthResult loginWithCookie(String[] cookieLines) throws Exception {
        if (cookieLines == null || cookieLines.length == 0)
            throw new IllegalArgumentException("No cookie lines provided");

        StringBuilder cookies = new StringBuilder();
        ArrayList<String> seen = new ArrayList<>();

        for (String line : cookieLines) {
            String[] parts = line.split("\t");
            if (parts.length < 7) continue;

            if (parts[0].endsWith("login.live.com") && !seen.contains(parts[5])) {
                cookies.append(parts[5]).append("=").append(parts[6]).append("; ");
                seen.add(parts[5]);
            }
        }

        if (cookies.length() == 0)
            throw new IllegalStateException("No valid login.live.com cookies found");

        String cookieHeader = cookies.substring(0, cookies.length() - 2);


        HttpsURLConnection conn = (HttpsURLConnection) new URL(
                "https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin&aid=1142970254"
        ).openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        String location1 = conn.getHeaderField("Location").replace(" ", "%20");

        // 2. Follow first redirect with cookie
        conn = (HttpsURLConnection) new URL(location1).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Cookie", cookieHeader);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        String location2 = conn.getHeaderField("Location");

        // 3. Follow second redirect with cookie
        conn = (HttpsURLConnection) new URL(location2).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Cookie", cookieHeader);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        String location3 = conn.getHeaderField("Location");
        if (location3 == null || !location3.contains("accessToken="))
            throw new IllegalStateException("No access token in final redirect");

        String accessToken = location3.split("accessToken=")[1];


        String decoded = new String(Base64.getDecoder().decode(accessToken), StandardCharsets.UTF_8);


        String token = decoded.split("\"Token\":\"")[1].split("\"")[0];
        String uhs = decoded.split(Pattern.quote("{\"DisplayClaims\":{\"xui\":[{\"uhs\":\""))[1].split("\"")[0];


        String xblToken = "XBL3.0 x=" + uhs + ";" + token;

        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();


        String mcLoginPayload = "{\"identityToken\":\"" + xblToken + "\",\"ensureLegacyEnabled\":true}";
        URL mcLoginUrl = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        HttpsURLConnection mcConn = (HttpsURLConnection) mcLoginUrl.openConnection();
        mcConn.setRequestMethod("POST");
        mcConn.setDoOutput(true);
        mcConn.setRequestProperty("Content-Type", "application/json");
        mcConn.setRequestProperty("User-Agent", "Mozilla/5.0");
        mcConn.getOutputStream().write(mcLoginPayload.getBytes(StandardCharsets.UTF_8));

        try (InputStream is = mcConn.getInputStream()) {
            String response = new String(Utils.readStream(is));
            // parse MC response
            Gson gson = new Gson();
            class McResponse {
                String access_token;
            }
            McResponse mcRes = gson.fromJson(response, McResponse.class);
            if (mcRes == null || mcRes.access_token == null)
                throw new IllegalStateException("Invalid Minecraft login response");

            AuthTokens tokens = new AuthTokens(mcRes.access_token, null);
            return authenticator.loginWithTokens(tokens);
        }
    }

    public static AuthTokens getTokens(String token) throws MicrosoftAuthenticationException {
        if (looksLikeRefresh(token)) {
            MicrosoftAuthResult result = auth.loginWithRefreshToken(token);
            return new AuthTokens(result.getAccessToken(), result.getRefreshToken());
        }
        throw new IllegalArgumentException("Only refresh tokens supported, access tokens alone don't work.");
    }

    public static boolean looksLikeJwt(String token) {
        return token.split("\\.").length == 3;
    }

    public static boolean looksLikeRefresh(String token) {
        return !looksLikeJwt(token) && token.length() > 200;
    }

    public static MicrosoftAuthResult authenticateWithRefreshToken(String refreshToken) throws MicrosoftAuthenticationException {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        return authenticator.loginWithRefreshToken(refreshToken);
    }

    public static MicrosoftAuthResult authenticateWithToken(AuthTokens accessToken) throws MicrosoftAuthenticationException {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        return authenticator.loginWithTokens(accessToken);
    }

    public static String getUUID(String name) {
        String s = "OfflinePlayer:" + name;
        return UUID.nameUUIDFromBytes(s.getBytes()).toString().replace("-", "");
    }

    public static void crackedLogin(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        ((MinecraftAccessor)(Object) mc).setSession(new Session(name, getUUID(name), "accessToken", "mojang"));
    }

    public static void loginWithMicrosoftAuthResult(MicrosoftAuthResult authResult) {
        Minecraft mc = Minecraft.getMinecraft();
        MinecraftProfile profile = authResult.getProfile();
        String accessToken = authResult.getAccessToken();

        Session session = new Session(
                profile.getName(),
                profile.getId(),
                accessToken,
                "msa"
        );

        ((MinecraftAccessor)(Object) mc).setSession(session);
    }
}

