package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.abilitykeybind.BloodlustAbility;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = BloodlustAbility.class, remap = false )
public abstract class BloodlustAbilityMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cache(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isBloodlust(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$factor.set(1.0F);
            return;
        }

        float f = 1.0F;
        if (player != null) {
            float adBaseline = ConfigHelper.getBaselineValue("bloodlust.attack_damage_baseline", 7.0F);

            if (adBaseline > 0.0F) {
                double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) f = (float)(ad / adBaseline);
            }

            RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
            if (rageEntry != null) {
                double rage = player.getAttributeValue(rageEntry);
                float rageBaseline = ConfigHelper.getBaselineValue("bloodlust.rage_baseline", 100.0F);
                if (rageBaseline > 0.0F && rage > 0.0) {
                    f *= (1.0F + (float)(rage / rageBaseline));
                }
            }
        }
        mswcompat$factor.set(f);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            require = 0
    )
    private boolean mswcompat$scaledSelfDamage(PlayerEntity player, DamageSource source, float baseAmount) {
        if (!mswcompat$shouldScale.get()) {
            return player.damage(source, baseAmount);
        }

        float factor = mswcompat$factor.get();
        float scaled = baseAmount * factor;

        float capHearts = ConfigHelper.getBaselineValue("bloodlust.self_damage_cap_hearts", 12.0F);
        float capHealthPercent = ConfigHelper.getBaselineValue("bloodlust.self_damage_cap_health_percent", 0.5F);
        float capHalfHp = player.getMaxHealth() * capHealthPercent;

        float capped = Math.min(scaled, Math.min(capHearts, capHalfHp));
        return player.damage(source, capped);
    }

    @ModifyVariable(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entitydata/BleedData;addBleed(Lnet/minecraft/entity/LivingEntity;I)V"),
            ordinal = 0,
            require = 0
    )
    private int mswcompat$scaleSelfBleed(int bleed) {
        if (!mswcompat$shouldScale.get()) {
            return bleed;
        }
        return (int)(bleed * mswcompat$factor.get());
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "NEW", target = "net/minecraft/entity/effect/StatusEffectInstance", ordinal = 0),
            require = 0
    )
    private StatusEffectInstance mswcompat$newBloodthirsty(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        if (!mswcompat$shouldScale.get()) {
            return new StatusEffectInstance(effect, duration, amplifier);
        }

        float f = mswcompat$factor.get();
        int amp = Math.max(0, (int)Math.floor((amplifier + 1) * f) - 1);
        return new StatusEffectInstance(effect, duration, amp);
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$factor.remove();
        mswcompat$shouldScale.remove();
    }
}