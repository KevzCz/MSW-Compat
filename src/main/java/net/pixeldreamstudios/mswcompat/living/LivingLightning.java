package net.pixeldreamstudios.mswcompat.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class LivingLightning {

    private LivingLightning() {
    }

    public static Entity mark(Entity spawned, @Nullable Entity owner) {
        if (spawned instanceof LightningEntity bolt && owner instanceof LivingEntity caster && !(owner instanceof PlayerEntity)) {
            ((LightningCaster) bolt).mswcompat$setCaster(caster);
        }
        return spawned;
    }

    @Nullable
    public static LivingEntity caster(LightningEntity bolt) {
        return bolt.getChanneler() != null ? bolt.getChanneler() : ((LightningCaster) bolt).mswcompat$caster();
    }
}
