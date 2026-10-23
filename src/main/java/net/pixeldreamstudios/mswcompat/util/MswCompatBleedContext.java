package net.pixeldreamstudios.mswcompat.util;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class MswCompatBleedContext {

    private static final ThreadLocal<Snapshot> CONTEXT = new ThreadLocal<>();

    private MswCompatBleedContext() {}

    public static void begin(String source, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
        Snapshot snapshot = new Snapshot();
        snapshot.source = source;
        snapshot.attacker = attacker;
        snapshot.target = target;
        CONTEXT.set(snapshot);
    }

    public static void setBuildup(int baseAmount, int scaledAmount, float factor) {
        Snapshot snapshot = CONTEXT.get();
        if (snapshot != null) {
            snapshot.baseAmount = baseAmount;
            snapshot.scaledAmount = scaledAmount;
            snapshot.factor = factor;
        }
    }

    @Nullable
    public static Snapshot consume() {
        Snapshot snapshot = CONTEXT.get();
        CONTEXT.remove();
        return snapshot;
    }

    public static final class Snapshot {
        public String source = "BleedData";
        @Nullable public LivingEntity attacker;
        @Nullable public LivingEntity target;
        public int baseAmount = -1;
        public int scaledAmount = -1;
        public float factor = 1.0F;
    }
}
