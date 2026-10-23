package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.soulsweaponry.items.abilities.posthit.BladeDance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin( value = BladeDance.class, remap = false )
public abstract class BladeDanceMixin {

    @Unique
    private static final ThreadLocal<Float> mswcompat$aoeScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique
    private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D",
                    remap = true
            ),
            remap = true
    )
    private double mswcompat$cacheScaleOnAttributeGet(LivingEntity entity, RegistryEntry<EntityAttribute> registryEntry, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        double originalValue = entity.getAttributeValue(registryEntry);

        boolean shouldScale = ItemMatcher.isGlaiveOfHodir(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$aoeScale.set(1.0F);
            return originalValue;
        }

        float adBaseline = ConfigHelper.getBaselineValue("blade_dance.attack_damage_baseline", 8.0F);
        float asBaseline = ConfigHelper.getBaselineValue("blade_dance.attack_speed_baseline", 1.3F);
        float adWeight = ConfigHelper.getFloatValue("blade_dance.attack_damage_weight", 0.75F);
        float asWeight = ConfigHelper.getFloatValue("blade_dance.attack_speed_weight", 0.75F);

        double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double as = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);

        float scale = DamageScalingHelper.factor(
                ad, adBaseline, adWeight, DamageScalingHelper.Mode.ABOVE_BASELINE,
                as, asBaseline, asWeight, DamageScalingHelper.Mode.ABOVE_BASELINE);
        mswcompat$aoeScale.set(scale);

        return originalValue;
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            remap = true
    )
    private boolean mswcompat$scaleAoESweep(LivingEntity target, DamageSource source, float amount) {
        if (! mswcompat$shouldScale.get()) {
            return target.damage(source, amount);
        }
        float scaled = amount * mswcompat$aoeScale.get();
        mswcompat$shouldScale.remove();
        mswcompat$aoeScale.remove();
        return target.damage(source, scaled);
    }
}
