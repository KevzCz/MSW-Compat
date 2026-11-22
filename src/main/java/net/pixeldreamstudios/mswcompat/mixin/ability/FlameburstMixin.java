package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
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
@Mixin(value = Flameburst.class, remap = false)
public abstract class FlameburstMixin {
    @Unique
    private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Float> mswcompat$moltenScale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            remap = true
    )
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isSupernova(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$moltenScale.set(1.0F);
            return;
        }

        float fireBaseline = ConfigHelper.getBaselineValue("supernova.fire_baseline", 20.0F);
        float moltenScaling = ConfigHelper.getBaselineValue("supernova.molten_metal_scaling", 0.1F);

        float scale = 1.0F;
        if (user != null) {
            double fire = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.FIRE);
            scale = DamageScalingHelper.factor(fire, fireBaseline, moltenScaling, DamageScalingHelper.Mode.FROM_ZERO);
        }

        mswcompat$moltenScale.set(scale);
    }

    @ModifyVariable(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "STORE"),
            ordinal = 1,
            remap = true
    )
    private float mswcompat$scaleMoltenDamage(float damage) {
        if (!mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$moltenScale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            remap = true
    )
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$moltenScale.remove();
        mswcompat$shouldScale.remove();
    }

    @Inject(method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V", at = @At("HEAD"), cancellable = true, remap = true)
    private void mswcompat$living(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        if (user instanceof PlayerEntity || world.isClient()) {
            return;
        }
        LivingAbilities.flameburst((Flameburst) (Object) this, stack, world, user, ticksUsed, mswcompat$shouldScale.get() ? mswcompat$moltenScale.get() : LivingAbilities.NO_SCALE);
        mswcompat$shouldScale.remove();
        mswcompat$moltenScale.remove();
        ci.cancel();
    }
}
