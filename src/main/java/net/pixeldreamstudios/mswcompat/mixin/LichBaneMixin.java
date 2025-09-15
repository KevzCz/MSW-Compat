package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.items.sword.LichBane;
import net.soulsweaponry.mixin.LivingEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(LichBane.class)
public abstract class LichBaneMixin {
    @Unique private static final Identifier mswcompat$FIRE_ID = Identifier.of("spell_power", "fire");
    @Unique private static final float mswcompat$FIRE_BASELINE = 20.0F;
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheFactor(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            double fire = 0.0D;

            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$FIRE_ID);
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry == null) {
                EntityAttribute attr = Registries.ATTRIBUTE.get(mswcompat$FIRE_ID);
                if (attr != null) {
                    entry = Registries.ATTRIBUTE.getEntry(attr);
                }
            }
            if (entry != null) {
                fire = attacker.getAttributeValue(entry);
            }

            factor = 1.0F + (float)(fire / mswcompat$FIRE_BASELINE);
            if (factor < 0.0F) factor = 0.0F;
        }
        mswcompat$factor.set(factor);
    }

    @Inject(method = "postHit", at = @At("TAIL"))
    private void mswcompat$clearFactor(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        mswcompat$factor.remove();
    }

    @Redirect(
            method = "postHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/mixin/LivingEntityInvoker;invokeApplyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
            )
    )
    private void mswcompat$scaleMagicDamage(LivingEntityInvoker inv, net.minecraft.entity.damage.DamageSource src, float amount) {
        float scaled = amount * mswcompat$factor.get();
        inv.invokeApplyDamage(src, scaled);
    }
}
