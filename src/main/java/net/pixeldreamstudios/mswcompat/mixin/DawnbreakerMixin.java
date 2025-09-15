package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.items.sword.AbstractDawnbreaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = AbstractDawnbreaker.class, remap = false)
public abstract class DawnbreakerMixin {
    @Unique private static final Identifier mswcompat$FIRE_ID = Identifier.of("spell_power", "fire");
    @Unique private static final ThreadLocal<Float> mswcompat$chanceAdd = ThreadLocal.withInitial(() -> 0.0F);

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> mswcompat$getFireAttrRef() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$FIRE_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(mswcompat$FIRE_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        return entry;
    }

    @Unique
    private static double mswcompat$getFirePower(LivingEntity user) {
        RegistryEntry.Reference<EntityAttribute> entry = mswcompat$getFireAttrRef();
        if (entry == null || user == null) return 0.0;
        return user.getAttributeValue(entry);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheChanceBonus(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float add = 0.0F;
        if (attacker != null) {
            double fp = mswcompat$getFirePower(attacker);
            add = (float)(fp / 200.0);
        }
        mswcompat$chanceAdd.set(add);
    }

    @ModifyVariable(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("STORE"),
            ordinal = 0,
            require = 0
    )
    private double mswcompat$increaseChance(double original) {
        double v = original + mswcompat$chanceAdd.get();
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        return v;
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$clearChanceBonus(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        mswcompat$chanceAdd.remove();
    }

    @Redirect(
            method = "dawnbreakerEvent(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private static boolean mswcompat$scaleDawnbreakerEventDamage(LivingEntity targetHit,
                                                                 DamageSource source,
                                                                 float amount,
                                                                 LivingEntity target,
                                                                 LivingEntity attacker,
                                                                 ItemStack stack) {
        float factor = 1.0F;
        if (attacker != null) {
            double fp = mswcompat$getFirePower(attacker);
            factor = (float)(1.0 + fp / 20.0);
        }
        return targetHit.damage(source, amount * factor);
    }
}
