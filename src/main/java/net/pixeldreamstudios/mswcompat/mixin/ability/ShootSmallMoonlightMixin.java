package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.soulsweaponry.items.abilities.attackclick.ShootSmallMoonlight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = ShootSmallMoonlight.class, remap = false )
public abstract class ShootSmallMoonlightMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "shootMoonlight(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;II)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ServerWorld world, ItemStack stack, PlayerEntity player, int amp, int lvl, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isMoonlightShortsword(stack) || ItemMatcher.isBluemoonShortsword(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        String configKey = ItemMatcher.isMoonlightShortsword(stack)
                ? "moonlight_shortsword.attack_damage_baseline"
                : "bluemoon_shortsword.attack_damage_baseline";

        float baseline = ConfigHelper.getBaselineValue(configKey, 8.0F);
        float factor = 1.0F;

        if (player != null && baseline > 0.0F) {
            double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) {
                factor = (float)(ad / baseline);
            }
        }

        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "shootMoonlight(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;II)V",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V"),
            index = 0,
            require = 0
    )
    private double mswcompat$scaleDamage(double damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$scale.get();
    }

    @Inject(
            method = "shootMoonlight(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;II)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ServerWorld world, ItemStack stack, PlayerEntity player, int amp, int lvl, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}