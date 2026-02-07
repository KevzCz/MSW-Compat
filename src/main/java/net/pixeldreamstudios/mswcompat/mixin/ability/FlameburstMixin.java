package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.stoppedusing.Flameburst;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Flameburst.class, remap = false )
public abstract class FlameburstMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$pillarScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$moltenScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isSupernova(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$pillarScale.set(1.0F);
            mswcompat$moltenScale.set(1.0F);
            return;
        }

        float fireBaseline = ConfigHelper.getBaselineValue("supernova.fire_baseline", 20.0F);
        float pillarScaling = ConfigHelper.getBaselineValue("supernova.flame_pillar_scaling", 0.75F);
        float moltenScaling = ConfigHelper.getBaselineValue("supernova.molten_metal_scaling", 0.1F);

        float firePart = 0.0F;
        if (user != null) {
            double fire = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.FIRE);
            firePart = fireBaseline > 0.0F ? (float)(fire / fireBaseline) : 0.0F;
        }

        mswcompat$pillarScale.set(Math.max(0.0F, 1.0F + pillarScaling * firePart));
        mswcompat$moltenScale.set(Math.max(0.0F, 1.0F + moltenScaling * firePart));
    }

    @ModifyVariable(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            require = 0
    )
    private float mswcompat$scaleMoltenDamage(float damage) {
        if (! mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$moltenScale.get();
    }

    @ModifyVariable(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/projectile/noclip/FlamePillar;setDamage(D)V"),
            ordinal = 0,
            require = 0
    )
    private double mswcompat$scalePillarDamage(double damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$pillarScale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$pillarScale.remove();
        mswcompat$moltenScale.remove();
        mswcompat$shouldScale.remove();
    }
}