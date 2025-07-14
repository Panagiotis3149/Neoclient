package neo.util.account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UsernameGenerator {

    private static final String WORDLIST_URL = "https://raw.githubusercontent.com/jeanphorn/wordlist/master/usernames.txt";

    public static String[] retrieveUsernames() {
        try {
            InputStream stream = new URL(WORDLIST_URL).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            reader.close();
            return builder.toString().split("\n");
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static String generateRandomUsername() {
        String[] usernames = retrieveUsernames();
        if (usernames.length == 0) return "DefaultUser";

        List<String> filtered = Arrays.stream(usernames)
                .filter(u -> u.length() >= 3 && u.length() <= 8)
                .collect(Collectors.toList());


        if (filtered.isEmpty()) return "DefaultUser";

        String prefix = capitalize(filtered.get((int) (Math.random() * filtered.size())));
        String suffix = capitalize(filtered.get((int) (Math.random() * filtered.size())));

        return applyPattern(prefix, suffix);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    private static String applyPattern(String prefix, String suffix) {
        int pattern = (int) (Math.random() * 4);

        switch (pattern) {
            case 0:
                return prefix + "_" + suffix;
            case 1:
                return prefix + suffix.substring(0, Math.min(2, suffix.length())) + (int) (Math.random() * 100);
            case 2:
                int idx = (int) (Math.random() * Math.min(prefix.length(), suffix.length()));
                return prefix.substring(0, idx) + "_" + suffix.substring(idx);
            case 3:
                StringBuilder merge = new StringBuilder(prefix).append(suffix);
                int uIndex = (int) (Math.random() * merge.length());
                int nIndex = (int) (Math.random() * merge.length());
                merge.insert(uIndex, "_");
                merge.insert(nIndex, (int) (Math.random() * 100));
                return merge.toString();
            default:
                return prefix + suffix;
        }
    }
}
