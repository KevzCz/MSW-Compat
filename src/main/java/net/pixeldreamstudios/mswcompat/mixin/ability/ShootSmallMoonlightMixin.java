package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
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
        boolean shouldScale = ItemMatcher.isMoonlightShortsword(stack) || ItemMatcher.isBluemoonShortsword(stack);
        if (!shouldScale) {
            return;
        }

        String configKey = ItemMatcher.isMoonlightShortsword(stack)
                ? "moonlight_shortsword.attack_damage_baseline"
                : "bluemoon_shortsword.attack_damage_baseline";

        float baseline = ConfigHelper.getBaselineValue(configKey, 8.0F);
        float factor = 1.0F;
        if (player != null) {
            factor = DamageScalingHelper.attackDamageOnly(
                    player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), baseline);
        }

        MswCompatScaleContext.beginMoonlightShortsword(factor);
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
