package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.stoppedusing.Obliterate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Obliterate.class, remap = false )
public abstract class ObliterateMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            remap = true
)
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isNightfall(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (user != null) {
            factor = DamageScalingHelper.attackDamageAndSecondary(
                    user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                    ConfigHelper.getBaselineValue("nightfall.attack_damage_baseline", 11.0F),
                    ConfigHelper.getFloatValue("nightfall.attack_damage_weight", 0.5F),
                    SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.SOUL),
                    ConfigHelper.getBaselineValue("nightfall.soul_baseline", 20.0F),
                    ConfigHelper.getFloatValue("nightfall.soul_weight", 0.5F));
        }

        mswcompat$damageScale.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            index = 1,
            remap = true
)
    private float mswcompat$scalePower(float damage) {
        if (! mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$damageScale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            remap = true
)
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$shouldScale.remove();
    }
}