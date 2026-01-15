package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.abilitykeybind.ArrowStorm;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = ArrowStorm.class, remap = false )
public abstract class ArrowStormMixin {

    @Unique
    private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique
    private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$calculateScale(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDarkmoonLongbow(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        float factor = 1.0F;

        if (player != null) {
            float rangedBaseline = ConfigHelper.getBaselineValue("darkmoon_longbow.ranged_damage_baseline", 9.0F);
            float arcaneBaseline = ConfigHelper.getBaselineValue("darkmoon_longbow.arcane_baseline", 20.0F);
            float rangedWeight = ConfigHelper.getFloatValue("darkmoon_longbow.ranged_damage_weight", 0.5F);
            float arcaneWeight = ConfigHelper.getFloatValue("darkmoon_longbow.arcane_weight", 0.5F);

            double ranged = 0.0;
            double arcane = 0.0;

            RegistryEntry.Reference<EntityAttribute> rangedAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.RangedWeapon.DAMAGE);
            if (rangedAttr != null) {
                ranged = player.getAttributeValue(rangedAttr);
            }

            RegistryEntry.Reference<EntityAttribute> arcaneAttr = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            if (arcaneAttr != null) {
                arcane = player.getAttributeValue(arcaneAttr);
            }

            float rangedPart = rangedBaseline > 0.0F ? (float)(ranged / rangedBaseline) : 1.0F;
            float arcanePart = arcaneBaseline > 0.0F ? (float)(arcane / arcaneBaseline) : 0.0F;

            factor = 1.0F + rangedWeight * (rangedPart - 1.0F) + arcaneWeight * arcanePart;
        }

        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/ArrowStormEntity;setDamage(D)V"
            ),
            index = 0,
            require = 0
    )
    private double mswcompat$scaleAbilityDamage(double baseDamage) {
        if (!mswcompat$shouldScale.get()) {
            return baseDamage;
        }
        return baseDamage * mswcompat$scale.get();
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ServerWorld world, ItemStack stack, PlayerEntity player, @Nullable Hand hand, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}