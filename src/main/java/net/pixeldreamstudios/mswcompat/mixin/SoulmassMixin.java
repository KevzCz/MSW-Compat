package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.entity.mobs.Soulmass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( targets = "net.soulsweaponry.entity.mobs.Soulmass$SoulmassGoal")
public abstract class SoulmassMixin {

    @Unique
    private static float mswcompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof Soulmass sm)) return 1.0F;

        float soulBaseline = ConfigHelper.getBaselineValue("soulmass.soul_baseline", 20.0F);
        return SpellPowerHelper.getScalingFactor(sm, MSWCompatIdentifiers.SpellPower.SOUL, soulBaseline);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            remap = true
    )
    private boolean mswcompat$scaleBeam(net.minecraft.entity.LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * mswcompat$factorFromSource(source));
    }
}