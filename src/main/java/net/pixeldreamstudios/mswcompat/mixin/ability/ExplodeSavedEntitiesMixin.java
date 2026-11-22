package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AbilityScales;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.soulsweaponry.items.abilities.abilitykeybind.ExplodeSavedEntities;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
            remap = true
)
    private void mswcompat$cacheScale(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDraupnirSpear(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            mswcompat$explosionScale.set(1.0F);
            return;
        }

        float factor = player != null ? AbilityScales.draupnir(player) : 1.0F;
        mswcompat$damageScale.set(factor);
        mswcompat$explosionScale.set(AbilityScales.draupnirExplosion(factor));
    }

    @Inject(
            method = "explodeSavedEntities(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheExplosionScale(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        if (!ItemMatcher.isDraupnirSpear(stack)) {
            return;
        }
        mswcompat$shouldScale.set(true);
        float factor = player != null ? AbilityScales.draupnir(player) : 1.0F;
        mswcompat$damageScale.set(factor);
        mswcompat$explosionScale.set(AbilityScales.draupnirExplosion(factor));
    }

    @Inject(
            method = "explodeSavedEntities(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearExplosionScale(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$explosionScale.remove();
        mswcompat$shouldScale.remove();
    }

    @ModifyArg(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            index = 1,
            remap = true
    )
    private float mswcompat$scaleAoEDamage(float damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$damageScale.get();
    }

    @ModifyArg(
            method = "explodeSavedEntities(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;",
                    remap = true
            ),
            index = 4,
            remap = true
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
            remap = true
)
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$explosionScale.remove();
        mswcompat$shouldScale.remove();
    }
}