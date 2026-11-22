package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.mswcompat.util.AbilityScales;
import net.pixeldreamstudios.mswcompat.util.MswCompatScaleContext;
import net.soulsweaponry.items.abilities.attackclick.ShootSmallMoonlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShootSmallMoonlight.class, remap = false)
public abstract class ShootSmallMoonlightMixin {

    @Inject(
            method = "onAttackClickServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheScale(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        if (player != null && AbilityScales.scalesSmallMoonlight(stack)) {
            MswCompatScaleContext.beginMoonlightShortsword(AbilityScales.smallMoonlight(player, stack));
        }
    }

    @Inject(
            method = "onAttackClickServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearScale(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        MswCompatScaleContext.clearMoonlightShortsword();
    }
}
