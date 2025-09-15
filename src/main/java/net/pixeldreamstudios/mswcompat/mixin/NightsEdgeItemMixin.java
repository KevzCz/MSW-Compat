package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.NightsEdgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(NightsEdgeItem.class)
public abstract class NightsEdgeItemMixin {
    @Unique private static final Identifier mswcompat$ARCANE_ID = Identifier.of("spell_power", "arcane");
    @Unique private static final float mswcompat$AD_BASELINE = 10.0F;
    @Unique private static final float mswcompat$ARCANE_BASELINE = 20.0F;
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;IF)V",
            at = @At("HEAD")
    )
    private void mswcompat$cacheScale(World world, LivingEntity user, ItemStack stack, Vec3d position, int warmup, float yaw, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$ARCANE_ID);
            RegistryEntry<EntityAttribute> arcaneEntry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            double arcane = arcaneEntry != null ? user.getAttributeValue(arcaneEntry) : 0.0D;

            float adPart = mswcompat$AD_BASELINE > 0.0F ? (float)(ad / mswcompat$AD_BASELINE) : 1.0F;
            float arcanePart = mswcompat$ARCANE_BASELINE > 0.0F ? (float)(arcane / mswcompat$ARCANE_BASELINE) : 0.0F;
            factor = 0.5F * adPart + 0.5F * arcanePart;
            if (factor < 0.0F) factor = 0.0F;
        }
        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/NightsEdge;setDamage(F)V"
            ),
            index = 0
    )
    private float mswcompat$scaleAbilityDamage(float originalArg) {
        float base = (float) ConfigConstructor.nights_edge_ability_damage;
        float ench = originalArg - base;
        float factor = mswcompat$scale.get();
        return base * factor + ench;
    }

    @Inject(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;IF)V",
            at = @At("TAIL")
    )
    private void mswcompat$clearScale(World world, LivingEntity user, ItemStack stack, Vec3d position, int warmup, float yaw, CallbackInfo ci) {
        mswcompat$scale.remove();
    }
}
