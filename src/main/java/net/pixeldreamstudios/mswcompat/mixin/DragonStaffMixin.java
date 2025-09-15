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
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.staff.DragonStaff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DragonStaff.class)
public abstract class DragonStaffMixin {

    @Unique private static final Identifier mswcompat$ARCANE_ID = Identifier.of("spell_power", "arcane");
    @Unique private static final float mswcompat$BASELINE = 20F;
    @Unique private static final float mswcompat$HEAL_CAP = 1.25F;

    @Unique private static final ThreadLocal<Float> mswcompat$fogHeal =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_vigorous_fog_damage_and_heal);
    @Unique private static final ThreadLocal<Float> mswcompat$fogDamage =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_vigorous_fog_damage_and_heal);
    @Unique private static final ThreadLocal<Float> mswcompat$auraAmp =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_aura_strength);

    @Inject(method = "usageTick", at = @At("HEAD"))
    private void mswcompat$cacheArcaneScaling(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        float arcane = 0.0F;
        if (user != null) {
            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$ARCANE_ID);
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry != null) {
                arcane = (float) user.getAttributeValue(entry);
            }
        }

        float baseFog = ConfigConstructor.dragon_staff_vigorous_fog_damage_and_heal;
        float factor = 1.0F + (arcane / mswcompat$BASELINE);

        mswcompat$fogDamage.set(baseFog * factor);
        mswcompat$fogHeal.set(baseFog * Math.min(mswcompat$HEAL_CAP, factor));
        mswcompat$auraAmp.set(ConfigConstructor.dragon_staff_aura_strength + arcane / 10.0F);
    }

    @Inject(method = "usageTick", at = @At("TAIL"))
    private void mswcompat$clearArcaneScaling(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$fogHeal.remove();
        mswcompat$fogDamage.remove();
        mswcompat$auraAmp.remove();
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_vigorous_fog_damage_and_heal:F",
                    ordinal = 0,
                    remap = false
            ),
            require = 0
    )
    private float mswcompat$redirectFogHeal() {
        return mswcompat$fogHeal.get();
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_vigorous_fog_damage_and_heal:F",
                    ordinal = 1,
                    remap = false
            ),
            require = 0
    )
    private float mswcompat$redirectFogDamage() {
        return mswcompat$fogDamage.get();
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_aura_strength:F",
                    remap = false
            ),
            require = 0
    )
    private float mswcompat$redirectAuraAmp() {
        return mswcompat$auraAmp.get();
    }
}
