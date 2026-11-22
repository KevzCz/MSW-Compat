package net.pixeldreamstudios.mswcompat.living;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface LightningCaster {

    @Nullable
    LivingEntity mswcompat$caster();

    void mswcompat$setCaster(LivingEntity caster);
}
