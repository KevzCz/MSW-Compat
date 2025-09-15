package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.DarkinBlade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(DarkinBlade.class)
public abstract class DarkinBladeMixin {

    @Unique
    private static final float mswcompat$BASELINE_ATTACK_DAMAGE = 11.0F;

    @Unique
    private static final float mswcompat$HEAL_MIN_SCALE = 0.75F;

    @Unique
    private static final float mswcompat$HEAL_MAX_SCALE = 1.25F;

    @Unique
    private static final ThreadLocal<Float> mswcompat$healScale = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static final ThreadLocal<Float> mswcompat$abilityScale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void mswcompat$cacheHealScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / mswcompat$BASELINE_ATTACK_DAMAGE);
        }
        float clamped = Math.max(mswcompat$HEAL_MIN_SCALE, Math.min(mswcompat$HEAL_MAX_SCALE, factor));
        mswcompat$healScale.set(clamped);
    }

    @ModifyVariable(method = "postHit", at = @At("STORE"), ordinal = 0, require = 0)
    private float mswcompat$scaleHealing(float healing) {
        return healing * mswcompat$healScale.get();
    }

    @Inject(method = "postHit", at = @At("RETURN"))
    private void mswcompat$clearHealScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        mswcompat$healScale.remove();
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void mswcompat$cacheAbilityScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / mswcompat$BASELINE_ATTACK_DAMAGE);
        }
        mswcompat$abilityScale.set(factor);
    }

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void mswcompat$clearAbilityScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$abilityScale.remove();
    }

    @Redirect(
            method = "onStoppedUsing",
            at = @At(value = "FIELD", target = "Lnet/soulsweaponry/config/ConfigConstructor;darkin_blade_ability_damage:F", remap = false),
            require = 0
    )
    private float mswcompat$scaleAbilityDamageField() {
        float base = ConfigConstructor.darkin_blade_ability_damage;
        return base * mswcompat$abilityScale.get();
    }

    @Redirect(
            method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/soulsweaponry/config/ConfigConstructor;darkin_blade_calculated_fall_max_damage:F", remap = false),
            require = 0
    )
    private float mswcompat$scaleCalculatedFallMaxDamage() {
        float base = ConfigConstructor.darkin_blade_calculated_fall_max_damage;
        float weaponAd = ((DarkinBlade)(Object)this).getAttackDamage();
        float factor = weaponAd > 0.0F ? weaponAd / mswcompat$BASELINE_ATTACK_DAMAGE : 1.0F;
        return base * factor;
    }
}
