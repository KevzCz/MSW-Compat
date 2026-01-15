package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.posthit.LifeSteal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = LifeSteal.class, remap = false )
public abstract class LifeStealMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "checkAndHeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
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

                float adBaseline = ConfigHelper.getBaselineValue(configPrefix + ".attack_damage_baseline", 8.0F);
                if (adBaseline > 0.0F) {
                    double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (ad > 0.0) {
                        factor = (float)(ad / adBaseline);
                    }
                }

                RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
                if (rageEntry != null) {
                    double rage = attacker.getAttributeValue(rageEntry);
                    float rageBaseline = ConfigHelper.getBaselineValue(configPrefix + ".rage_baseline", 100.0F);
                    if (rageBaseline > 0.0F && rage > 0.0) {
                        factor *= (1.0F + (float)(rage / rageBaseline));
                    }
                }
            } else {
                configPrefix = ItemMatcher.isDarkinBlade(stack) ? "darkin_blade" : "darkin_scythe_prime";

                float adBaseline = ConfigHelper.getBaselineValue(configPrefix + ".attack_damage_baseline", 9.0F);
                if (adBaseline > 0.0F) {
                    double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                    if (ad > 0.0) {
                        factor = (float)(ad / adBaseline);
                    }
                }
            }

            float minScale = ConfigHelper.getBaselineValue(configPrefix + ".heal_min_scale", 0.75F);
            float maxScale = ConfigHelper.getBaselineValue(configPrefix + ".heal_max_scale", isBloodthirster ? 1.50F : 1.25F);
            factor = Math.max(minScale, Math.min(maxScale, factor));
        }

        mswcompat$scale.set(factor);
    }

    @ModifyVariable(
            method = "checkAndHeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;heal(F)V"),
            ordinal = 0,
            require = 0
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
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}