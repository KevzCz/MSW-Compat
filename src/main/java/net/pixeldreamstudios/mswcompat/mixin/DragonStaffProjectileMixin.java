package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.projectile.DragonStaffProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DragonStaffProjectile.class)
public abstract class DragonStaffProjectileMixin {
    @Unique private static final Identifier mswcompat$ARCANE_ID = Identifier.of("spell_power", "arcane");
    @Unique private static final ThreadLocal<Float> mswcompat$auraAmp =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_aura_strength);

    @Inject(method = "detonate", at = @At("HEAD"), remap = false)
    private void mswcompat$cacheArcaneScaling(CallbackInfo ci) {
        float baseAmp = ConfigConstructor.dragon_staff_aura_strength;
        float arcane = 0.0F;

        DragonStaffProjectile self = (DragonStaffProjectile)(Object)this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$ARCANE_ID);
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry != null) {
                arcane = (float) living.getAttributeValue(entry);
            }
        }
        mswcompat$auraAmp.set(baseAmp + arcane / 10.0F);
    }

    @Inject(method = "detonate", at = @At("TAIL"), remap = false)
    private void mswcompat$clearArcaneScaling(CallbackInfo ci) {
        mswcompat$auraAmp.remove();
    }

    @Redirect(
            method = "detonate",
            at = @At(value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_aura_strength:F"),
            remap = false
    )
    private float mswcompat$redirectDetonateAuraAmp() {
        return mswcompat$auraAmp.get();
    }
}
