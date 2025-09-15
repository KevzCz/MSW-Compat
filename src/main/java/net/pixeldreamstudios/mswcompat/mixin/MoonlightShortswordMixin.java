package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.items.sword.MoonlightShortsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MoonlightShortsword.class)
public abstract class MoonlightShortswordMixin {
    @Unique
    private static final float mswcompat$BASELINE_ATTACK_DAMAGE = 8.0F;

    @Unique
    private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "summonSmallProjectile", at = @At("HEAD"))
    private static void mswcompat$cacheScale(World world, PlayerEntity user, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) {
                factor = (float)(ad / mswcompat$BASELINE_ATTACK_DAMAGE);
            }
        }
        mswcompat$scale.set(factor);
    }

    @Inject(method = "summonSmallProjectile", at = @At("TAIL"))
    private static void mswcompat$clearScale(World world, PlayerEntity user, CallbackInfo ci) {
        mswcompat$scale.remove();
    }

    @Redirect(
            method = "summonSmallProjectile",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V")
    )
    private static void mswcompat$scaleShortswordDamage(MoonlightProjectile projectile, double damage) {
        projectile.setDamage(damage * mswcompat$scale.get());
    }
}
