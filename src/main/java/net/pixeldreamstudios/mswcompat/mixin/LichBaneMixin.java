package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
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
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheFactor(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            double fire = 0.0D;

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
            if (entry != null) {
                fire = attacker.getAttributeValue(entry);
            }

            float fireBaseline = ConfigHelper.getBaselineValue("lich_bane.fire_baseline", 20.0F);
            factor = 1.0F + (float)(fire / fireBaseline);
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