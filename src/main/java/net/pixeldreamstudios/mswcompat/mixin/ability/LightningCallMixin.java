package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.pixeldreamstudios.mswcompat.util.AbilityScales;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.abilitykeybind.LightningCall;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = LightningCall.class, remap = false )
public abstract class LightningCallMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$stompScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Integer> mswcompat$lightningBonus = ThreadLocal.withInitial(() -> 0);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("HEAD"),
            remap = true
)
    private void mswcompat$calculateScaling(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDragonslayerSwordspear(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$stompScale.set(1.0F);
            mswcompat$lightningBonus.set(0);
            return;
        }

        float stompFactor = player != null ? AbilityScales.dragonslayerStomp(player) : 1.0F;
        int lightningAdd = player != null ? AbilityScales.dragonslayerLightning(player) : 0;

        mswcompat$stompScale.set(stompFactor);
        mswcompat$lightningBonus.set(lightningAdd);
    }

    @ModifyVariable(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            remap = true
)
    private int mswcompat$addLightningAmount(int lightning) {
        if (! mswcompat$shouldScale.get()) {
            return lightning;
        }
        return lightning + mswcompat$lightningBonus.get();
    }

    @ModifyArg(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            index = 1,
            remap = true
    )
    private float mswcompat$scaleStompDamage(float damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$stompScale.get();
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            remap = true
)
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$stompScale.remove();
        mswcompat$lightningBonus.remove();
        mswcompat$shouldScale.remove();
    }
}