package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.posthit.DawnbreakerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = DawnbreakerExplosion.class, remap = false )
public abstract class DawnbreakerExplosionMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$eventFactor = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "dawnbreakerEvent(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheEventFactor(World world, LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDawnbreaker(stack) || ItemMatcher.isEmpoweredDawnbreaker(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!  shouldScale) {
            mswcompat$eventFactor.set(1.0F);
            return;
        }

        float fireBaseline = ConfigHelper.getBaselineValue("dawnbreaker.fire_baseline", 20.0F);
        float factor = 1.0F;

        if (attacker != null) {
            factor = SpellPowerHelper.getScalingFactor(attacker, MSWCompatIdentifiers.SpellPower.FIRE, fireBaseline);
        }

        mswcompat$eventFactor.set(factor);
    }

    @ModifyArg(
            method = "dawnbreakerEvent(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            index = 1,
            require = 0
    )
    private float mswcompat$scaleDawnbreakerEventDamage(float amount) {
        if (! mswcompat$shouldScale.get()) {
            return amount;
        }
        return amount * mswcompat$eventFactor.get();
    }

    @Inject(
            method = "dawnbreakerEvent(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearEventFactor(World world, LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        mswcompat$eventFactor.remove();
        mswcompat$shouldScale.remove();
    }
}