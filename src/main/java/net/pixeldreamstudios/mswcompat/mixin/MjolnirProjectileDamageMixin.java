package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.soulsweaponry.entity.projectile.MjolnirProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = MjolnirProjectile.class)
public abstract class MjolnirProjectileDamageMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$didScale = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getDamage(Lnet/minecraft/entity/Entity;)F", at = @At("HEAD"))
    private void mswcompat$cacheFactor(Entity target, CallbackInfoReturnable<Float> cir) {
        float factor = 1.0F;
        MjolnirProjectile self = (MjolnirProjectile) (Object) this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            float adBaseline = ConfigHelper.getBaselineValue("mjolnir.projectile.attack_damage_baseline", 13.0F);
            if (adBaseline > 0.0F) {
                double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    factor = (float) (ad / adBaseline);
                }
            }
        }
        mswcompat$factor.set(Math.max(0.0F, factor));
        mswcompat$didScale.set(false);
    }

    @Redirect(
            method = "getDamage(Lnet/minecraft/entity/Entity;)F",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)F"
            )
    )
    private float mswcompat$scaleBaseForEnchant(ServerWorld serverWorld, ItemStack stack, Entity target, DamageSource src, float base) {
        mswcompat$didScale.set(true);
        return EnchantmentHelper.getDamage(serverWorld, stack, target, src, base * mswcompat$factor.get());
    }

    @Inject(method = "getDamage(Lnet/minecraft/entity/Entity;)F", at = @At("RETURN"), cancellable = true)
    private void mswcompat$scaleWhenNoEnchantPath(Entity target, CallbackInfoReturnable<Float> cir) {
        if (!mswcompat$didScale.get()) {
            cir.setReturnValue(cir.getReturnValueF() * mswcompat$factor.get());
        }
        mswcompat$factor.remove();
        mswcompat$didScale.remove();
    }
}