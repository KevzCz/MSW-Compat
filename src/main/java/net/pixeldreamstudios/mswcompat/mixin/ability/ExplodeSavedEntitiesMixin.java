package net.pixeldreamstudios.mswcompat.mixin. ability;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.soulsweaponry.items.abilities.abilitykeybind.ExplodeSavedEntities;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = ExplodeSavedEntities.class, remap = false)
public abstract class ExplodeSavedEntitiesMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$explosionScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDraupnirSpear(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            mswcompat$explosionScale. set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (player != null) {
            float adBaseline = ConfigHelper.getBaselineValue("draupnir_spear. attack_damage_baseline", 8.0F);

            if (adBaseline > 0.0F) {
                double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }

        mswcompat$damageScale.set(factor);
        mswcompat$explosionScale.set(factor);
    }

    @ModifyVariable(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            require = 0
    )
    private float mswcompat$scaleAoEDamage(float damage) {
        if (! mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$damageScale.get();
    }

    @ModifyVariable(
            method = "explodeSavedEntities(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
    )
    private float mswcompat$scaleExplosionPower(float power) {
        if (!mswcompat$shouldScale.get()) {
            return power;
        }
        return power * mswcompat$explosionScale.get();
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$explosionScale.remove();
        mswcompat$shouldScale.remove();
    }
}