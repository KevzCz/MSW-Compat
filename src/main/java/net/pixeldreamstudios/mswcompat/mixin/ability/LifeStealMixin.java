package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.posthit.LifeSteal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = LifeSteal.class, remap = false )
public abstract class LifeStealMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "checkAndHeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            remap = true
)
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity attacker, CallbackInfo ci) {
        boolean isBloodthirster = ItemMatcher.isBloodthirster(stack);
        boolean isOmnivamp = ItemMatcher.isAnyOmnivamp(stack);
        boolean shouldScale = isBloodthirster || isOmnivamp;

        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        float factor = 1.0F;

        if (attacker != null) {
            String configPrefix;

            if (isBloodthirster) {
                configPrefix = "bloodthirster";
                double rage = 0.0;
                RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
                if (rageEntry != null) {
                    rage = attacker.getAttributeValue(rageEntry);
                }
                factor = DamageScalingHelper.multiplicativeAdRatioAndRageFromZero(
                        attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue(configPrefix + ".attack_damage_baseline", 8.0F),
                        rage,
                        ConfigHelper.getBaselineValue(configPrefix + ".rage_baseline", 100.0F));
            } else {
                configPrefix = ItemMatcher.isDarkinBlade(stack) ? "darkin_blade" : "darkin_scythe_prime";
                factor = DamageScalingHelper.attackDamageOnly(
                        attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                        ConfigHelper.getBaselineValue(configPrefix + ".attack_damage_baseline", 9.0F));
            }

            float minScale = ConfigHelper.getBaselineValue(configPrefix + ".heal_min_scale", 0.75F);
            float maxScale = ConfigHelper.getBaselineValue(configPrefix + ".heal_max_scale", isBloodthirster ? 1.50F : 1.25F);
            factor = DamageScalingHelper.clamp(factor, minScale, maxScale);
        }

        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "checkAndHeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;heal(F)V",
                    remap = true
            ),
            index = 0,
            remap = true
    )
    private float mswcompat$scaleHealing(float healing) {
        if (! mswcompat$shouldScale.get()) {
            return healing;
        }
        return healing * mswcompat$scale.get();
    }

    @Inject(
            method = "checkAndHeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            remap = true
)
    private void mswcompat$clearScale(ItemStack stack, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}