package net.pixeldreamstudios.mswcompat.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class LivingCooldowns {

    private static final Map<LivingEntity, Map<Item, Long>> READY = new WeakHashMap<>();

    private LivingCooldowns() {
    }

    public static boolean coolingDown(LivingEntity user, Item item) {
        Long ready = READY.getOrDefault(user, Map.of()).get(item);
        return ready != null && user.getWorld().getTime() < ready;
    }

    public static void cool(LivingEntity user, Item item, int ticks) {
        if (ticks > 0) {
            READY.computeIfAbsent(user, key -> new HashMap<>()).put(item, user.getWorld().getTime() + ticks);
        }
    }
}
