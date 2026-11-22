package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.pixeldreamstudios.mswcompat.util.AbilityScales;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.abilitykeybind.Cloudburst;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Cloudburst.class, remap = false )
public abstract class CloudburstMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "shootArrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheScale(ServerWorld world, ItemStack stack, ItemStack arrowStack, PlayerEntity player, @Nullable Vec3d currentTargetPos, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isGaleforce(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$damageScale.set(1.0F);
            return;
        }

        float factor = player != null ? AbilityScales.galeforce(player) : 1.0F;

        mswcompat$damageScale.set(factor);
    }

    @ModifyVariable(
            method = "shootArrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0,
            remap = true
    )
    private double mswcompat$scaleDamage(double damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$damageScale.get();
    }

    @Inject(
            method = "shootArrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearScale(ServerWorld world, ItemStack stack, ItemStack arrowStack, PlayerEntity player, @Nullable Vec3d currentTargetPos, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$shouldScale.remove();
    }
}
