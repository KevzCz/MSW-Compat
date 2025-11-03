package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.soulsweaponry.items.sword.DragonslayerSwordBerserk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DragonslayerSwordBerserk.class)
public abstract class DragonslayerSwordBerserkMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cache(ServerWorld world, ItemStack stack, PlayerEntity user, CallbackInfo ci) {
        float f = 1.0F;
        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("dragonslayer_sword_berserk.attack_damage_baseline", 12.0F);

            if (adBaseline > 0.0F) {
                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) f = (float)(ad / adBaseline);
            }
        }
        mswcompat$factor.set(f);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "NEW", target = "net/minecraft/entity/effect/StatusEffectInstance",ordinal = 0),
            require = 0
    )
    private StatusEffectInstance mswcompat$newBloodthirsty(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        float f = mswcompat$factor.get();
        int amp = Math.max(0, (int)Math.floor((amplifier + 1) * f) - 1);
        return new StatusEffectInstance(effect, duration, amp);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "NEW", target = "net/minecraft/entity/effect/StatusEffectInstance",ordinal = 1),
            require = 0
    )
    private StatusEffectInstance mswcompat$newStrength(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        float f = mswcompat$factor.get();
        int amp = Math.max(0, (int)Math.floor((amplifier + 1) * f) - 1);
        return new StatusEffectInstance(effect, duration, amp);
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity user, CallbackInfo ci) {
        mswcompat$factor.remove();
    }
}