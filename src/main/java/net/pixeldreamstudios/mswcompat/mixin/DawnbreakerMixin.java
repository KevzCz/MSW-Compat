package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.items.sword.AbstractDawnbreaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = AbstractDawnbreaker.class, remap = false)
public abstract class DawnbreakerMixin {
    @Unique private static final Identifier MSWCOMPAT_FIRE_ID = Identifier.of("spell_power", "fire");
    @Unique private static final ThreadLocal<Float> MSWCOMPAT_EVENT_FACTOR = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static RegistryEntry.Reference<EntityAttribute> mswcompat$getFireAttrRef() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, MSWCOMPAT_FIRE_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(MSWCOMPAT_FIRE_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        return entry;
    }

    @Unique
    private static double mswcompat$getFirePower(LivingEntity user) {
        if (user == null) return 0.0;
        var ref = mswcompat$getFireAttrRef();
        return ref == null ? 0.0 : user.getAttributeValue(ref);
    }

    @Inject(method = "dawnbreakerEvent", at = @At("HEAD"), require = 0)
    private static void mswcompat$cacheEventFactor(LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        float factor = (float)(1.0 + mswcompat$getFirePower(attacker) / 20.0);
        MSWCOMPAT_EVENT_FACTOR.set(factor);
    }

    @Redirect(
            method = "dawnbreakerEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            require = 0
    )
    private static boolean mswcompat$scaleDawnbreakerEventDamage(LivingEntity targetHit, DamageSource source, float amount) {
        float scaled = amount * MSWCOMPAT_EVENT_FACTOR.get();
        return targetHit.damage(source, scaled);
    }

    @Inject(method = "dawnbreakerEvent", at = @At("TAIL"), require = 0)
    private static void mswcompat$clearEventFactor(LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        MSWCOMPAT_EVENT_FACTOR.remove();
    }
}
