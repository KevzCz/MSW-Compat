package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.statboost.RainBoostsStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = RainBoostsStats.class, remap = false )
public abstract class RainBoostsStatsMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageBoost = ThreadLocal.withInitial(() -> 0.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$speedBoost = ThreadLocal.withInitial(() -> 0.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$calculateBoost(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isAnyRainBoost(stack);

        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale || world.isClient || ! world.isRaining() || !(entity instanceof LivingEntity living)) {
            mswcompat$damageBoost.set(0.0F);
            mswcompat$speedBoost.set(0.0F);
            return;
        }

        String configPrefix = ItemMatcher.isDragonslayerSwordspear(stack) ? "dragonslayer_swordspear" : "mjolnir";

        float lightningBaseline = ConfigHelper.getBaselineValue(configPrefix + ".lightning_baseline", 20.0F);
        float waterBaseline = ConfigHelper.getBaselineValue(configPrefix + ".water_baseline", 20.0F);
        float lightningWeight = ConfigHelper.getFloatValue(configPrefix + ".rain_lightning_weight", 0.5F);
        float waterWeight = ConfigHelper.getFloatValue(configPrefix + ".rain_water_weight", 0.5F);

        double lightning = SpellPowerHelper.getEffectiveSpellPower(living, MSWCompatIdentifiers.SpellPower.LIGHTNING);
        double water = SpellPowerHelper.getEffectiveSpellPower(living, MSWCompatIdentifiers.MoreRPGLibrary.WATER);

        float lightningPart = lightningBaseline > 0.0F ? (float)(lightning / lightningBaseline) : 0.0F;
        float waterPart = waterBaseline > 0.0F ? (float)(water / waterBaseline) : 0.0F;

        float totalBoost = lightningWeight * lightningPart + waterWeight * waterPart;

        mswcompat$damageBoost.set(totalBoost);
        mswcompat$speedBoost.set(totalBoost * 0.1F);
    }

    @ModifyVariable(
            method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
    )
    private float mswcompat$addDamageBoost(float damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage + mswcompat$damageBoost.get();
    }

    @ModifyVariable(
            method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            require = 0
    )
    private float mswcompat$addSpeedBoost(float attackSpeed) {
        if (!mswcompat$shouldScale.get()) {
            return attackSpeed;
        }
        return attackSpeed + mswcompat$speedBoost.get();
    }

    @Inject(
            method = "inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clear(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        mswcompat$damageBoost.remove();
        mswcompat$speedBoost.remove();
        mswcompat$shouldScale.remove();
    }
}