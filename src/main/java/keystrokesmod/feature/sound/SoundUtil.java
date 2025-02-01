package keystrokesmod.feature.sound;

import net.minecraft.client.Minecraft;

public class SoundUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public enum ToggleType {
        RISE, SIGMA, QUICKMACRO
    }

    /**
     * Plays the enable sound for the specified toggle type.
     *
     * @param type The toggle type (RISE, SIGMA, or QUICKMACRO).
     */
    public void playEnableSound(ToggleType type) {
        playSound(getSoundPath(type, true), 1.0f, 1.0f);
    }

    /**
     * Plays the disable sound for the specified toggle type.
     *
     * @param type The toggle type (RISE, SIGMA, or QUICKMACRO).
     */
    public void playDisableSound(ToggleType type) {
        playSound(getSoundPath(type, false), 1.0f, 1.0f);
    }

    /**
     * Plays a sound by its registry name.
     *
     * @param soundName The registry name of the sound (e.g., "keystrokesmod:toggle/rise/enable").
     * @param volume    The volume of the sound.
     * @param pitch     The pitch of the sound.
     */
    private void playSound(String soundName, float volume, float pitch) {
        if (mc.thePlayer != null) {
            mc.thePlayer.playSound(soundName, volume, pitch);
        }
    }

    /**
     * Gets the sound path for the specified toggle type and state (enable/disable).
     *
     * @param type   The toggle type.
     * @param enable True for enable sound, false for disable sound.
     * @return The sound path as a string.
     */
    private String getSoundPath(ToggleType type, boolean enable) {
        String action = enable ? "enable" : "disable";
        switch (type) {
            case RISE:
                return "keystrokesmod:toggle/rise/" + action;
            case SIGMA:
                return "keystrokesmod:toggle/sigma/" + action;
            case QUICKMACRO:
                return "keystrokesmod:toggle/quickmacro/" + action;
            default:
                throw new IllegalArgumentException("Unknown ToggleType: " + type);
        }
    }
}
