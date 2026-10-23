package net.pixeldreamstudios.mswcompat.util;

public final class MswCompatScaleContext {
    private static final ThreadLocal<Boolean> KEYBIND_ACTIVE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Float> KEYBIND_FACTOR = ThreadLocal.withInitial(() -> 1.0F);

    private MswCompatScaleContext() {}

    public static boolean isKeybindActive() {
        return KEYBIND_ACTIVE.get();
    }

    public static float keybindFactor() {
        return KEYBIND_FACTOR.get();
    }

    public static void setKeybindActive(boolean active) {
        KEYBIND_ACTIVE.set(active);
    }

    public static void setKeybindFactor(float factor) {
        KEYBIND_FACTOR.set(factor);
    }

    public static void clearKeybind() {
        KEYBIND_ACTIVE.remove();
        KEYBIND_FACTOR.remove();
    }

    private static final ThreadLocal<Boolean> MOONLIGHT_SHORTSWORD_PENDING =
            ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Float> MOONLIGHT_SHORTSWORD_FACTOR =
            ThreadLocal.withInitial(() -> 1.0F);

    public static void beginMoonlightShortsword(float factor) {
        MOONLIGHT_SHORTSWORD_PENDING.set(true);
        MOONLIGHT_SHORTSWORD_FACTOR.set(factor);
    }

    public static double applyMoonlightShortswordDamage(double damage) {
        if (!MOONLIGHT_SHORTSWORD_PENDING.get()) {
            return damage;
        }
        MOONLIGHT_SHORTSWORD_PENDING.set(false);
        return damage * MOONLIGHT_SHORTSWORD_FACTOR.get();
    }

    public static void clearMoonlightShortsword() {
        MOONLIGHT_SHORTSWORD_PENDING.remove();
        MOONLIGHT_SHORTSWORD_FACTOR.remove();
    }
}
