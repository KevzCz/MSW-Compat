package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.soulsweaponry.items.sword.Bloodthirster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(Bloodthirster.class)
public abstract class BloodthirsterMixin {

    @Unique
    private static final float mswcompat$BASELINE_ATTACK_DAMAGE = 8.0F;

    @Unique
    private static final float mswcompat$MIN_SCALE = 0.75F;

    @Unique
    private static final float mswcompat$MAX_SCALE = 1.50F;

    @Unique
    private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) {
                factor = (float)(ad / mswcompat$BASELINE_ATTACK_DAMAGE);
            }
        }
        float clamped = Math.max(mswcompat$MIN_SCALE, Math.min(mswcompat$MAX_SCALE, factor));
        mswcompat$scale.set(clamped);
    }

    @Inject(method = "postHit", at = @At("RETURN"))
    private void mswcompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        mswcompat$scale.remove();
    }

    @ModifyVariable(method = "postHit", at = @At("STORE"), ordinal = 0)
    private float mswcompat$scaleHealing(float healing) {
        return healing * mswcompat$scale.get();
    }
}
