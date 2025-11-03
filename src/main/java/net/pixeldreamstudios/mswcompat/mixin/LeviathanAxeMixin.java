package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.axe.LeviathanAxe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(LeviathanAxe.class)
public abstract class LeviathanAxeMixin {
    @Unique private static final ThreadLocal<Float> FROST_FACTOR = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Integer> AMP_BONUS   = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "iceExplosion", at = @At("HEAD"))
    private static void mswcompat$cacheFrostForExplosion(net.minecraft.world.World world,
                                                         net.minecraft.util.math.BlockPos pos,
                                                         Entity attacker, int amplifier, CallbackInfo ci) {
        float factor = 1.0F;
        int bonusAmp = 0;

        if (attacker instanceof LivingEntity living) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);
            if (entry != null) {
                double frost = living.getAttributeValue(entry);
                float frostBaseline = ConfigHelper.getBaselineValue("leviathan_axe.ice_explosion.frost_baseline", 200.0F);
                float frostPerAmp = ConfigHelper.getBaselineValue("leviathan_axe.frost_per_amplifier", 10.0F);
                factor   = 1.0F + (float)(frost / frostBaseline);
                bonusAmp = (int)Math.floor(frost / frostPerAmp);
            }
        }
        FROST_FACTOR.set(factor);
        AMP_BONUS.set(bonusAmp);
    }

    @Inject(method = "iceExplosion", at = @At("RETURN"))
    private static void mswcompat$clearFrostForExplosion(net.minecraft.world.World world,
                                                         net.minecraft.util.math.BlockPos pos,
                                                         Entity attacker, int amplifier, CallbackInfo ci) {
        FROST_FACTOR.remove();
        AMP_BONUS.remove();
    }

    @ModifyArg(method = "iceExplosion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            index = 1)
    private static float mswcompat$scaleFreezeDamage(float originalAmount) {
        return originalAmount * FROST_FACTOR.get();
    }

    @Redirect(
            method = "iceExplosion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z")
    )
    private static boolean mswcompat$addStatusEffectWithBonusExplosion(LivingEntity target, StatusEffectInstance original) {
        var type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + AMP_BONUS.get());
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }

    @Redirect(
            method = "postHit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z")
    )
    private boolean mswcompat$addStatusEffectWithBonusMelee(LivingEntity target, StatusEffectInstance original,
                                                            ItemStack stack, LivingEntity tgt, LivingEntity attacker) {
        int bonus = 0;
        if (attacker != null) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FROST);
            if (entry != null) {
                float frostPerAmp = ConfigHelper.getBaselineValue("leviathan_axe.frost_per_amplifier", 10.0F);
                bonus = (int)Math.floor(attacker.getAttributeValue(entry) / frostPerAmp);
            }
        }
        var type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + bonus);
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }
}