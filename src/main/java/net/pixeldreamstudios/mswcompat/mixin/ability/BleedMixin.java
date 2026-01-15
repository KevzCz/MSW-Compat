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
import net.soulsweaponry.items.abilities.posthit.Bleed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Bleed.class, remap = false )
public abstract class BleedMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$bleedScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheBleedScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isAnyBleedWeapon(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$bleedScale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (attacker != null) {
            String configPrefix = ItemMatcher.isMoonveil(stack) ? "moonveil" : "bloodlust";

            float adBaseline = ConfigHelper.getBaselineValue(configPrefix + ".attack_damage_baseline", 7.0F);
            float rageBaseline = ConfigHelper.getBaselineValue(configPrefix + ".rage_baseline", 100.0F);
            float adWeight = ConfigHelper.getFloatValue(configPrefix + ".bleed_attack_damage_weight", 0.7F);
            float rageWeight = ConfigHelper.getFloatValue(configPrefix + ".bleed_rage_weight", 0.3F);

            double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double rage = 0.0;

            RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
            if (rageEntry != null) {
                rage = attacker.getAttributeValue(rageEntry);
            }

            float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
            float ragePart = rageBaseline > 0.0F ? (float)(rage / rageBaseline) : 0.0F;

            factor = 1.0F + adWeight * (adPart - 1.0F) + rageWeight * ragePart;

            float minScale = ConfigHelper.getBaselineValue(configPrefix + ".bleed_buildup_min_scale", 0.5F);
            float maxScale = ConfigHelper.getBaselineValue(configPrefix + ".bleed_buildup_max_scale", 2.0F);
            factor = Math.max(minScale, Math.min(maxScale, factor));
        }

        mswcompat$bleedScale.set(factor);
    }

    @ModifyVariable(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entitydata/BleedData;addBleed(Lnet/minecraft/entity/LivingEntity;I)V"),
            ordinal = 0,
            require = 0
    )
    private int mswcompat$scaleBleedAmount(int bleedAmount) {
        if (!mswcompat$shouldScale.get()) {
            return bleedAmount;
        }
        return (int)(bleedAmount * mswcompat$bleedScale.get());
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearBleedScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        mswcompat$bleedScale.remove();
        mswcompat$shouldScale.remove();
    }
}