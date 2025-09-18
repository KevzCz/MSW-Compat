package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.soulsweaponry.items.hammer.Supernova;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Supernova.class, remap = false)
public abstract class SupernovaMixin {
    @Unique private static final Identifier FIRE_ID = Identifier.of("spell_power", "fire");
    @Unique private static final ThreadLocal<Float> PILLAR_SCALE = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> fireAttr() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, FIRE_ID);
        RegistryEntry.Reference<EntityAttribute> ref = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (ref == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(FIRE_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) {
                    ref = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
                }
            }
        }
        return ref;
    }

    @Unique
    private static float computeScale(LivingEntity user) {
        if (user == null) return 1.0F;
        var fireRef = fireAttr();
        double fire = (fireRef != null) ? user.getAttributeValue(fireRef) : 0.0;
        float s = (float)(1.0 + 0.75 * (fire / 20.0));
        return s < 0.0F ? 0.0F : s;
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cache(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        PILLAR_SCALE.set(computeScale(user));
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/FlamePillar;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private double mswcompat$scalePillarDamageDouble(double base) {
        return base * PILLAR_SCALE.get();
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/FlamePillar;setDamage(F)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private float mswcompat$scalePillarDamageFloat(float base) {
        return base * PILLAR_SCALE.get();
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/DamagingWarmupEntityEvents$OtherAttributes;<init>(DD)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private double mswcompat$scaleOtherAttributesDamageDD(double base) {
        return base * PILLAR_SCALE.get();
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/DamagingWarmupEntityEvents$OtherAttributes;<init>(FF)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private float mswcompat$scaleOtherAttributesDamageFF(float base) {
        return base * PILLAR_SCALE.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clear(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        PILLAR_SCALE.remove();
    }
}
