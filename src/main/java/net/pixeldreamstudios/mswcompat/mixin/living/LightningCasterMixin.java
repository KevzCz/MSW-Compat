package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.pixeldreamstudios.mswcompat.living.LightningCaster;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LightningEntity.class)
public abstract class LightningCasterMixin implements LightningCaster {

    @Unique
    @Nullable
    private LivingEntity mswcompat$caster;

    @Override
    @Nullable
    public LivingEntity mswcompat$caster() {
        return mswcompat$caster;
    }

    @Override
    public void mswcompat$setCaster(LivingEntity caster) {
        mswcompat$caster = caster;
    }
}
