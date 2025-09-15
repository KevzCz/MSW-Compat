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
import net.minecraft.world.World;
import net.soulsweaponry.items.sword.DarkMoonGreatsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DarkMoonGreatsword.class)
public abstract class DarkMoonGreatswordMixin {

    @Unique private static final Identifier mswcompat$FROST_ID = Identifier.of("spell_power", "frost");
    @Unique private static final float mswcompat$SPELL_BASELINE = 20F;

    // Cached per call
    @Unique private static final ThreadLocal<Float> mswcompat$damageFactor =
            ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Integer> mswcompat$ampBonus =
            ThreadLocal.withInitial(() -> 0);

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float finalFactor = 1.0F;
        int bonusAmp = 0;

        if (user != null) {
            // Half-effective Attack Damage factor: 1 + 0.5*(AD/baseAD - 1)
            float adHalf = 1.0F;
            float baseAd = ((DarkMoonGreatsword)(Object)this).getAttackDamage();
            if (baseAd > 0.0F) {
                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    float full = (float)(ad / baseAd);
                    adHalf = 1.0F + 0.5F * (full - 1.0F);
                }
            }

            // Frost spell power: half-effective factor for damage AND additive amp bonus
            float frostHalf = 1.0F;
            double frostVal = 0.0;
            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$FROST_ID);
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry != null) {
                frostVal = user.getAttributeValue(entry);
                frostHalf = 1.0F + 0.5F * ((float)frostVal / mswcompat$SPELL_BASELINE);
            }

            finalFactor = adHalf * frostHalf;
            bonusAmp = Math.max(0, (int)Math.floor(frostVal / 10.0)); // +⌊frost/10⌋
        }

        mswcompat$damageFactor.set(finalFactor);
        mswcompat$ampBonus.set(bonusAmp);
    }

    // Scale projectile damage: baseDamage * factor
    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V"),
            index = 0
    )
    private double mswcompat$scaleProjectileDamage(double baseDamage) {
        return baseDamage * mswcompat$damageFactor.get();
    }

    // Add to status effect amplifier: amp + ⌊frost/10⌋
    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setEffectAmplifier(I)V"),
            index = 0
    )
    private int mswcompat$boostEffectAmplifier(int amp) {
        return amp + mswcompat$ampBonus.get();
    }

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$damageFactor.remove();
        mswcompat$ampBonus.remove();
    }
}
