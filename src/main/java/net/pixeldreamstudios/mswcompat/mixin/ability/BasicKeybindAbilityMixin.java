package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.pixeldreamstudios.mswcompat.util.AbilityScales;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.MswCompatScaleContext;
import net.soulsweaponry.items.abilities.abilitykeybind.BasicKeybindAbility;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BasicKeybindAbility.class, remap = false)
public abstract class BasicKeybindAbilityMixin {

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/util/TriConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            ),
            remap = true
    )
    private void mswcompat$cache(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isHeapOfRawIron(stack);
        MswCompatScaleContext.setKeybindActive(shouldScale);

        if (!shouldScale) {
            MswCompatScaleContext.setKeybindFactor(1.0F);
            return;
        }

        float f = player != null ? AbilityScales.heapOfRawIron(player) : 1.0F;
        MswCompatScaleContext.setKeybindFactor(f);
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        MswCompatScaleContext.clearKeybind();
    }
}
