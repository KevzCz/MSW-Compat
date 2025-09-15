package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.soulsweaponry.items.spear.GlaiveOfHodir;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = GlaiveOfHodir.class, remap = false)
public abstract class GlaiveOfHodirProjectileScalingMixin {
    @Unique private static final float mswcompat$AD_BASE = 8.0F;
    @Unique private static final float mswcompat$AS_BASE = 1.3F;
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double as = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);
            float adPart = mswcompat$AD_BASE > 0.0F ? (float)(ad / mswcompat$AD_BASE) : 1.0F;
            float asPart = mswcompat$AS_BASE > 0.0F ? (float)(as / mswcompat$AS_BASE) : 1.0F;
            factor = 1.0F + 0.75F * (adPart - 1.0F) + 0.75F * (asPart - 1.0F);
        }
        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/GhostGlaiveEntity;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private double mswcompat$scaleProjectileDamage(double baseDamage) {
        return baseDamage * mswcompat$scale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$scale.remove();
    }
}
