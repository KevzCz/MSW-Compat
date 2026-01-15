package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.AttributeHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.soulsweaponry.items.abilities.usagetick.DragonMist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = DragonMist.class, remap = false )
public abstract class DragonMistMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$fogHealScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$fogDamageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheArcaneScaling(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isDragonStaff(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$fogHealScale.set(1.0F);
            mswcompat$fogDamageScale.set(1.0F);
            return;
        }

        float arcane = 0.0F;
        if (user != null) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            if (entry != null) {
                arcane = (float) user.getAttributeValue(entry);
            }
        }

        float arcaneBaseline = ConfigHelper.getBaselineValue("dragon_staff.arcane_baseline", 20.0F);
        float healCap = ConfigHelper.getBaselineValue("dragon_staff.heal_cap_multiplier", 1.25F);

        float factor = arcaneBaseline > 0.0F ? 1.0F + (arcane / arcaneBaseline) : 1.0F;
        mswcompat$fogDamageScale.set(factor);
        mswcompat$fogHealScale.set(Math.min(healCap, factor));
    }

    @ModifyVariable(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
    )
    private float mswcompat$scaleHealOrDamage(float healOrDamage, World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!mswcompat$shouldScale.get()) {
            return healOrDamage;
        }
        return healOrDamage * mswcompat$fogDamageScale.get();
    }

    @ModifyVariable(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;heal(F)V"),
            ordinal = 0,
            require = 0
    )
    private float mswcompat$scaleHealing(float healing) {
        if (!mswcompat$shouldScale.get()) {
            return healing;
        }
        return healing * mswcompat$fogHealScale.get();
    }

    @Inject(
            method = "usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearArcaneScaling(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        mswcompat$fogHealScale.remove();
        mswcompat$fogDamageScale.remove();
        mswcompat$shouldScale.remove();
    }
}