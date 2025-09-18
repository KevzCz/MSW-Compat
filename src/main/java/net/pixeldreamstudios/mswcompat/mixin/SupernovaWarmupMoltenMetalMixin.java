package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DamagingWarmupEntityEvents.class, remap = false)
public abstract class SupernovaWarmupMoltenMetalMixin {
    @Unique private static final Identifier FIRE_ID = Identifier.of("spell_power", "fire");
    @Unique private static final ThreadLocal<Float> MSWCOMPAT_MM_FACTOR = ThreadLocal.withInitial(() -> 1.0F);

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
    private static float computeScale(Entity owner) {
        if (!(owner instanceof LivingEntity living)) return 1.0F;
        var fireRef = fireAttr();
        double fire = (fireRef != null) ? living.getAttributeValue(fireRef) : 0.0;
        float s = (float) (1.0 + 0.25 * (fire / 20.0));
        return s < 0.0F ? 0.0F : s;
    }

    // Cache factor at the start of the lambda that spawns molten metal.
    // We use method="*" so this will match the synthetic methods like lambda$static$1(...),
    // provided the descriptor matches (DamagingWarmupEntity, OtherAttributes) -> void.
    @Inject(method = "*",
            at = @At("HEAD"),
            require = 0)
    private static void mswcompat$cacheFactor(DamagingWarmupEntity warm,
                                              DamagingWarmupEntityEvents.OtherAttributes attrs,
                                              CallbackInfo ci) {
        MSWCOMPAT_MM_FACTOR.set(computeScale(warm.getOwner()));
    }

    // Scale double signature
    @ModifyArg(method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/MoltenMetal;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0)
    private static double mswcompat$scaleMoltenDamageD(double base) {
        return base * MSWCOMPAT_MM_FACTOR.get();
    }

    // Scale float signature
    @ModifyArg(method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/MoltenMetal;setDamage(F)V",
                    remap = false
            ),
            index = 0,
            require = 0)
    private static float mswcompat$scaleMoltenDamageF(float base) {
        return base * MSWCOMPAT_MM_FACTOR.get();
    }

    // Clear to avoid lingering threadlocal state
    @Inject(method = "*",
            at = @At("TAIL"),
            require = 0)
    private static void mswcompat$clearFactor(DamagingWarmupEntity warm,
                                              DamagingWarmupEntityEvents.OtherAttributes attrs,
                                              CallbackInfo ci) {
        MSWCOMPAT_MM_FACTOR.remove();
    }
}
