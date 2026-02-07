package net.pixeldreamstudios.mswcompat.mixin.ability;

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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z"),
            require = 0
    )
    private void mswcompat$calculateScaling(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDragonslayerSwordspear(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$stompScale.set(1.0F);
            mswcompat$lightningBonus.set(0);
            return;
        }

        float stompFactor = 1.0F;
        int lightningAdd = 0;

        if (player != null) {
            float lightningBaseline = ConfigHelper.getBaselineValue("dragonslayer_swordspear.lightning_baseline", 20.0F);
            float waterBaseline = ConfigHelper.getBaselineValue("dragonslayer_swordspear.water_baseline", 20.0F);
            float lightningWeight = ConfigHelper.getFloatValue("dragonslayer_swordspear.lightning_weight", 0.5F);
            float waterWeight = ConfigHelper.getFloatValue("dragonslayer_swordspear.water_weight", 0.5F);

            double lightning = SpellPowerHelper.getEffectiveSpellPower(player, MSWCompatIdentifiers.SpellPower.LIGHTNING);
            double water = SpellPowerHelper.getEffectiveSpellPower(player, MSWCompatIdentifiers.MoreRPGLibrary.WATER);

            float lightningPart = lightningBaseline > 0.0F ? (float)(lightning / lightningBaseline) : 0.0F;
            float waterPart = waterBaseline > 0.0F ? (float)(water / waterBaseline) : 0.0F;

            float totalFactor = 1.0F + lightningWeight * lightningPart + waterWeight * waterPart;
            stompFactor = totalFactor;

            float lightningPerPower = ConfigHelper.getFloatValue("dragonslayer_swordspear.lightning_per_spell_power", 0.1F);
            lightningAdd = (int)Math.floor(lightning * lightningPerPower);
        }

        mswcompat$stompScale.set(stompFactor);
        mswcompat$lightningBonus.set(lightningAdd);
    }

    @ModifyVariable(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            require = 0
    )
    private int mswcompat$addLightningAmount(int lightning) {
        if (! mswcompat$shouldScale.get()) {
            return lightning;
        }
        return lightning + mswcompat$lightningBonus.get();
    }

    @ModifyVariable(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            ordinal = 0,
            require = 0
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
            require = 0
    )
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$stompScale.remove();
        mswcompat$lightningBonus.remove();
        mswcompat$shouldScale.remove();
    }
}