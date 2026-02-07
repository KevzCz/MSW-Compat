package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.stoppedusing.ChaosStorm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = ChaosStorm.class, remap = false )
public abstract class ChaosStormMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheFireScale(World world, ItemStack stack, LivingEntity user, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isEmpoweredDawnbreaker(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (user != null) {
            float fireBaseline = ConfigHelper.getBaselineValue("empowered_dawnbreaker.fire_baseline", 20.0F);
            factor = SpellPowerHelper.getScalingFactor(user, MSWCompatIdentifiers.SpellPower.FIRE, fireBaseline);
        }
        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/FlamePillar;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private double mswcompat$scaleFlamePillarDamage(double baseDamage) {
        if (!mswcompat$shouldScale.get()) {
            return baseDamage;
        }
        return baseDamage * mswcompat$scale.get();
    }

    @Inject(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearFireScale(World world, ItemStack stack, LivingEntity user, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}