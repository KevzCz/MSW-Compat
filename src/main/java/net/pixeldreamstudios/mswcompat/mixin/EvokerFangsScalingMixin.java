package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( value = EvokerFangsEntity.class)
public abstract class EvokerFangsScalingMixin {

    @Unique
    private static float mswcompat$factorFromOwner(EvokerFangsEntity self) {
        LivingEntity owner = self.getOwner();
        if (owner == null) return 1.0F;

        float soulBaseline = ConfigHelper.getBaselineValue("evoker_fangs.soul_baseline", 10.0F);
        if (soulBaseline <= 0.0F) return 1.0F;

        double soul = SpellPowerHelper.getEffectiveSpellPower(owner, MSWCompatIdentifiers.SpellPower.SOUL);
        float factor = (float)(soul / soulBaseline);
        return Math.max(0.0F, factor);
    }

    @Redirect(
            method = "damage(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            ),
            require = 0
    )
    private boolean mswcompat$scaleFangsDamageNoOwner(LivingEntity target,
                                                      DamageSource src,
                                                      float amount) {
        EvokerFangsEntity self = (EvokerFangsEntity)(Object)this;
        return target.damage(src, amount * mswcompat$factorFromOwner(self));
    }

    @Redirect(
            method = "damage(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 1
            ),
            require = 0
    )
    private boolean mswcompat$scaleFangsDamageWithOwner(LivingEntity target,
                                                        DamageSource src,
                                                        float amount) {
        EvokerFangsEntity self = (EvokerFangsEntity)(Object)this;
        return target.damage(src, amount * mswcompat$factorFromOwner(self));
    }
}