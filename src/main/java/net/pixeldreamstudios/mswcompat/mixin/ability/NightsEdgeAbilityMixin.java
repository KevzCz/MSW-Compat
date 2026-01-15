package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.abilitykeybind.NightsEdgeAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = NightsEdgeAbility.class, remap = false )
public abstract class NightsEdgeAbilityMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);
    @Unique private static final ThreadLocal<ItemStack> mswcompat$currentStack = ThreadLocal.withInitial(() -> null);

    @Inject(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Vec3d;IFF)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(World world, LivingEntity user, Vec3d position, int warmup, float yaw, float damage, CallbackInfo ci) {
        ItemStack stack = mswcompat$currentStack.get();
        boolean shouldScale = stack != null && ItemMatcher.isNightsEdge(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$damageScale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("nights_edge.attack_damage_baseline", 10.0F);
            float arcaneBaseline = ConfigHelper.getBaselineValue("nights_edge.arcane_baseline", 20.0F);
            float adWeight = ConfigHelper.getFloatValue("nights_edge.attack_damage_weight", 0.5F);
            float arcaneWeight = ConfigHelper.getFloatValue("nights_edge.arcane_weight", 0.5F);

            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double arcane = 0.0;

            RegistryEntry.Reference<EntityAttribute> arcaneEntry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            if (arcaneEntry != null) {
                arcane = user.getAttributeValue(arcaneEntry);
            }

            float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
            float arcanePart = arcaneBaseline > 0.0F ? (float)(arcane / arcaneBaseline) : 0.0F;

            factor = adWeight * adPart + arcaneWeight * arcanePart;
        }

        mswcompat$damageScale.set(Math.max(0.0F, factor));
    }

    @ModifyVariable(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Vec3d;IFF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private float mswcompat$scaleDamage(float damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$damageScale.get();
    }

    @Inject(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Vec3d;IFF)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(World world, LivingEntity user, Vec3d position, int warmup, float yaw, float damage, CallbackInfo ci) {
        mswcompat$damageScale.remove();
        mswcompat$shouldScale.remove();
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$captureStackKeybind(net.minecraft.server.world.ServerWorld world, ItemStack stack, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand, CallbackInfo ci) {
        mswcompat$currentStack.set(stack);
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearStackKeybind(net.minecraft.server.world.ServerWorld world, ItemStack stack, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand, CallbackInfo ci) {
        mswcompat$currentStack.remove();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$captureStackCharge(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$currentStack.set(stack);
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearStackCharge(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$currentStack.remove();
    }
}