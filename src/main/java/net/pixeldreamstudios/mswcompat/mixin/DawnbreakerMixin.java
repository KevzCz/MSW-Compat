package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
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
    @Unique private static final ThreadLocal<Float> mswcompat$eventFactor = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static double mswcompat$getFirePower(LivingEntity user) {
        if (user == null) return 0.0;
        RegistryEntry.Reference<EntityAttribute> ref = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
        return ref == null ? 0.0 : user.getAttributeValue(ref);
    }

    @Inject(method = "dawnbreakerEvent", at = @At("HEAD"), require = 0)
    private static void mswcompat$cacheEventFactor(LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        float fireBaseline = ConfigHelper.getBaselineValue("dawnbreaker.fire_baseline", 20.0F);
        float factor = 1.0F;

        if (fireBaseline > 0.0F) {
            factor = 1.0F + (float)(mswcompat$getFirePower(attacker) / fireBaseline);
        }

        mswcompat$eventFactor.set(factor);
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
        float scaled = amount * mswcompat$eventFactor.get();
        return targetHit.damage(source, scaled);
    }

    @Inject(method = "dawnbreakerEvent", at = @At("TAIL"), require = 0)
    private static void mswcompat$clearEventFactor(LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        mswcompat$eventFactor.remove();
    }
}