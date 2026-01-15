package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.api.entitystats.EntityBleed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = EntityBleed.class, remap = false)
public abstract class EntityBleedMixin {

    @Unique private static final ThreadLocal<Float> mswcompat$bloodLossScale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "triggerBloodLoss(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private static void mswcompat$cacheBloodLossScale(LivingEntity entity, CallbackInfo ci) {
        float factor = 1.0F;

        float adBaseline = ConfigHelper.getBaselineValue("blood_loss.attack_damage_baseline", 10.0F);
        float rageBaseline = ConfigHelper.getBaselineValue("blood_loss.rage_baseline", 100.0F);
        float adWeight = ConfigHelper.getFloatValue("blood_loss.attack_damage_weight", 0.5F);
        float rageWeight = ConfigHelper.getFloatValue("blood_loss.rage_weight", 0.75F);

        double ad = entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double rage = 0.0;

        RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
        if (rageEntry != null) {
            rage = entity.getAttributeValue(rageEntry);
        }

        float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
        float ragePart = rageBaseline > 0.0F ? (float)(rage / rageBaseline) : 0.0F;

        factor = 1.0F + adWeight * (adPart - 1.0F) + rageWeight * ragePart;

        float minScale = ConfigHelper.getBaselineValue("blood_loss.min_scale", 0.5F);
        float maxScale = ConfigHelper.getBaselineValue("blood_loss.max_scale", 10.0F);
        factor = Math.max(minScale, Math.min(maxScale, factor));

        mswcompat$bloodLossScale.set(factor);
    }

    @ModifyArg(
            method = "triggerBloodLoss(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            index = 1,
            require = 0
    )
    private static float mswcompat$scaleBloodLossDamage(float damage) {
        return damage * mswcompat$bloodLossScale.get();
    }

    @Inject(
            method = "triggerBloodLoss(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private static void mswcompat$clearBloodLossScale(LivingEntity entity, CallbackInfo ci) {
        mswcompat$bloodLossScale.remove();
    }
}