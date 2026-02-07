package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.soulsweaponry.entity.mobs.RimeSpectre$RimeSpectreGoal")
public abstract class RimeSpectreFrostBeamScalingMixin {

    @Unique
    private static float mswcompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof LivingEntity living)) return 1.0F;

        float soulBaseline = ConfigHelper.getBaselineValue("rime_spectre.soul_baseline", 20.0F);
        float frostBaseline = ConfigHelper.getBaselineValue("rime_spectre.frost_baseline", 20.0F);
        float soulWeight = ConfigHelper.getFloatValue("rime_spectre.soul_weight", 0.25F);
        float frostWeight = ConfigHelper.getFloatValue("rime_spectre.frost_weight", 0.75F);

        double soul = SpellPowerHelper.getEffectiveSpellPower(living, MSWCompatIdentifiers.SpellPower.SOUL);
        double frost = SpellPowerHelper.getEffectiveSpellPower(living, MSWCompatIdentifiers.SpellPower.FROST);

        float soulPart = soulBaseline > 0.0F ? (float)(soul / soulBaseline) : 0.0F;
        float frostPart = frostBaseline > 0.0F ? (float)(frost / frostBaseline) : 0.0F;
        float weighted = soulWeight * soulPart + frostWeight * frostPart;

        return 1.0F + weighted;
    }

    @Redirect(
            method = "frostBeam(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            require = 0
    )
    private boolean mswcompat$scaleRimeSpectreBeamDamage(LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * mswcompat$factorFromSource(source));
    }
}