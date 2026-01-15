package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
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
import net.soulsweaponry.items.abilities.abilitykeybind.BasicKeybindAbility;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = BasicKeybindAbility.class, remap = false)
public abstract class BasicKeybindAbilityMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$factor = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/util/TriConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false),
            remap = false,
            require = 0
    )
    private void mswcompat$cache(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isHeapOfRawIron(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$factor.set(1.0F);
            return;
        }

        float f = 1.0F;
        if (player != null) {
            float adBaseline = ConfigHelper.getBaselineValue("heap_of_raw_iron.attack_damage_baseline", 12.0F);

            if (adBaseline > 0.0F) {
                double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    f = (float)(ad / adBaseline);
                }
            }

            RegistryEntry.Reference<EntityAttribute> rageEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.MoreRPGLibrary.RAGE);
            if (rageEntry != null) {
                double rage = player.getAttributeValue(rageEntry);
                float rageBaseline = ConfigHelper.getBaselineValue("heap_of_raw_iron.rage_baseline", 20.0F);
                if (rageBaseline > 0.0F && rage > 0.0) {
                    f *= (1.0F + (float)(rage / rageBaseline));
                }
            }
        }
        mswcompat$factor.set(f);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z", remap = true),
            remap = false,
            require = 0
    )
    private boolean mswcompat$scaleStatusEffect(PlayerEntity player, StatusEffectInstance instance) {
        if (!mswcompat$shouldScale.get()) {
            return player.addStatusEffect(instance);
        }

        float f = mswcompat$factor.get();
        int scaledAmp = Math.max(0, (int)Math.floor((instance.getAmplifier() + 1) * f) - 1);
        StatusEffectInstance scaled = new StatusEffectInstance(
                instance.getEffectType(),
                instance.getDuration(),
                scaledAmp,
                instance.isAmbient(),
                instance.shouldShowParticles(),
                instance.shouldShowIcon()
        );
        return player.addStatusEffect(scaled);
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0
    )
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$factor.remove();
        mswcompat$shouldScale.remove();
    }
}