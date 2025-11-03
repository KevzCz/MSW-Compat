package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
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
    @Unique private static final ThreadLocal<Float> mswcompat$pillarScale = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static float mswcompat$computeScale(LivingEntity user) {
        if (user == null) return 1.0F;

        float fireBaseline = ConfigHelper.getBaselineValue("supernova.fire_baseline", 20.0F);
        float pillarScaling = ConfigHelper.getBaselineValue("supernova.flame_pillar_scaling", 0.75F);

        RegistryEntry.Reference<EntityAttribute> fireRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.FIRE);
        double fire = (fireRef != null) ? user.getAttributeValue(fireRef) : 0.0;

        float firePart = fireBaseline > 0.0F ? (float)(fire / fireBaseline) : 0.0F;
        float s = 1.0F + pillarScaling * firePart;
        return s < 0.0F ? 0.0F : s;
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cache(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$pillarScale.set(mswcompat$computeScale(user));
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
        return base * mswcompat$pillarScale.get();
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
        return base * mswcompat$pillarScale.get();
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
        return base * mswcompat$pillarScale.get();
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
        return base * mswcompat$pillarScale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clear(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$pillarScale.remove();
    }
}