package neo.util.server.anticheat;

import neo.script.ScriptDefaults;

public class AnticheatUtils {

    public static String detectPattern(int[] a, long[] b) {
        if (a.length == 0) return "No data";
        b = (b == null || b.length <= 1) ? new long[0] : java.util.Arrays.copyOfRange(b, 1, b.length);

        if (isWatTheDog(a)) return "Watchdog";
        if (isAntigamingChair(a)) return "AGC";
        if (isGrim(a)) return "Grim";
        if (isKarhu(a, b)) return "Karhu";
        if (isVulcan(a, b)) return "Vulcan";
        if (isMatrix(a, b)) return "Matrix";
        if (isFrequency(a)) return "Frequency";
        if (isSparky(a)) return "Sparky";
        if (isPolar(a)) return "Polar";
        if (isIntave(a)) return "Intave";
        if (isVerus(a)) return "Verus";

        return "Unknown";
    }

    private static boolean isGrim(int[] a) {
        for (int i = 0; i < a.length; i++) if (a[i] != -i) return false;
        return true;
    }


    private static boolean isIntave(int[] a) {
        if (a.length < 2 || a[0] != -1) return false;
        int expected = -2;
        for (int i = 1; i < a.length; i++) {
            if (a[i] == -1 && expected == -8) {
                expected = -2;
                continue;
            }
            if (a[i] != expected) return false;
            expected--;
        }
        return true;
    }


    private static boolean isPolar(int[] a) {
        if (a.length < 3) return false;
        if (a[0] >= 0) return false;
        if (a[1] >= 0 || a[1] >= a[0]) return false;
        for (int i = 2; i < a.length; i++) if (a[i] != a[i-1] - 1) return false;
        return true;
    }

    private static boolean isVulcan(int[] actions, long[] delays) {
        if (actions.length < 5) return false;
        for (int i = 1; i < actions.length; i++) {
            if (actions[i] != actions[i - 1] + 1) return false;
        }
        for (long delay : delays) {
            if (delay < 230 || delay > 270) return false;
        }
        return true;
    }


    private static boolean isVerus(int[] a) {
        if (a.length < 2) return false;
        if (a[0] == 0) return false;
        if (a[0] > 0) {
            for (int i = 1; i < a.length; i++) {
                if (a[i] != a[i - 1] + 1) return false;
            }
        } else {
            for (int i = 1; i < a.length; i++) {
                if (a[i] != a[i - 1] - 1) return false;
            }
        }
        return true;
    }


    private static boolean isFrequency(int[] a) {
        int val = a[0];
        for (int x : a) if (x != val) return false;
        return true;
    }

    private static boolean isMatrix(int[] actions, long[] delays) {
        if (actions.length < 5) return false;
        for (int i = 1; i < actions.length; i++) {
            if (actions[i] != actions[i - 1] - 1) return false;
        }
        for (long delay : delays) {
            if (delay < 80 || delay > 180) return false;
        }
        return true;
    }

    private static boolean isSparky(int[] a) {
        if (a.length < 4) return false;
        if (a[0] >= 0 || a[1] <= 0) return false;
        for (int i = 2; i < a.length; i += 2) if (a[i] >= a[i-2]) return false;
        for (int i = 3; i < a.length; i += 2) if (a[i] <= a[i-2]) return false;
        return true;
    }

    private static boolean isKarhu(int[] a, long[] b) {
        if (a == null || a.length == 0) return false;
        if (a[0] > -2500 || a[0] < -4500) return false;
        for (long bs : b) if (bs > 100) return false;
        for (int i = 1; i < a.length; i++) if (a[i] != a[i - 1] - 1) return false;
        return true;
    }


    private static boolean isWatTheDog(int[] a) {
        return ScriptDefaults.client.getServerIP().contains("hypixel.net");
    }


    private static boolean isAntigamingChair(int[] a) {
        return ScriptDefaults.client.getServerIP().contains("minemen.club");
    }
}
