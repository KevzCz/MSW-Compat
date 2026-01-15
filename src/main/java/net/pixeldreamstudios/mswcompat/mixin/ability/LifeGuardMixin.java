package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.userdamaged.LifeGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = LifeGuard.class, remap = false )
public abstract class LifeGuardMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$explosionScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "modifyUserDamageTaken(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)F",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(LivingEntity user, float damageTaken, DamageSource source, ItemStack stack, Hand hand, CallbackInfoReturnable<Float> cir) {
        boolean shouldScale = ItemMatcher.isExcalibur(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$explosionScale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (user != null) {
            float arcaneBaseline = ConfigHelper.getBaselineValue("excalibur.arcane_baseline", 20.0F);
            float soulBaseline = ConfigHelper.getBaselineValue("excalibur.soul_baseline", 20.0F);
            float arcaneWeight = ConfigHelper.getFloatValue("excalibur.arcane_weight", 0.5F);
            float soulWeight = ConfigHelper.getFloatValue("excalibur.soul_weight", 0.5F);

            RegistryEntry.Reference<EntityAttribute> arcRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            RegistryEntry.Reference<EntityAttribute> soulRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.SOUL);

            double arc = arcRef != null ? user.getAttributeValue(arcRef) : 0.0;
            double soul = soulRef != null ? user.getAttributeValue(soulRef) : 0.0;

            float arcPart = arcaneBaseline > 0.0F ? (float)(arc / arcaneBaseline) : 0.0F;
            float soulPart = soulBaseline > 0.0F ?  (float)(soul / soulBaseline) : 0.0F;

            factor = 1.0F + arcaneWeight * arcPart + soulWeight * soulPart;
        }
        mswcompat$explosionScale.set(factor);
    }

    @ModifyVariable(
            method = "modifyUserDamageTaken(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)F",
            at = @At(value = "STORE"),
            ordinal = 2,
            require = 0
    )
    private float mswcompat$scaleExplosionDamage(float explosionDamage) {
        if (!mswcompat$shouldScale.get()) {
            return explosionDamage;
        }
        return explosionDamage * mswcompat$explosionScale.get();
    }

    @Inject(
            method = "modifyUserDamageTaken(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)F",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$clearScale(LivingEntity user, float damageTaken, DamageSource source, ItemStack stack, Hand hand, CallbackInfoReturnable<Float> cir) {
        mswcompat$explosionScale.remove();
        mswcompat$shouldScale.remove();
    }
}