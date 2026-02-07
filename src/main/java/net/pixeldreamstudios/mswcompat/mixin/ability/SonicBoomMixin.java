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
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.stoppedusing.SonicBoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = SonicBoom.class, remap = false )
public abstract class SonicBoomMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isExcalibur(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (user != null) {
            float arcaneBaseline = ConfigHelper.getBaselineValue("excalibur.arcane_baseline", 20.0F);
            float soulBaseline = ConfigHelper.getBaselineValue("excalibur.soul_baseline", 20.0F);
            float arcaneWeight = ConfigHelper.getFloatValue("excalibur.arcane_weight", 0.5F);
            float soulWeight = ConfigHelper.getFloatValue("excalibur.soul_weight", 0.5F);

            RegistryEntry.Reference<EntityAttribute> arcRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.ARCANE);
            RegistryEntry.Reference<EntityAttribute> soulRef = AttributeHelper.getAttributeEntry(MSWCompatIdentifiers.SpellPower.SOUL);

            double arc = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.ARCANE);
            double soul = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.SOUL);

            float arcPart = arcaneBaseline > 0.0F ? (float)(arc / arcaneBaseline) : 0.0F;
            float soulPart = soulBaseline > 0.0F ? (float)(soul / soulBaseline) : 0.0F;

            factor = 1.0F + arcaneWeight * arcPart + soulWeight * soulPart;
        }
        mswcompat$scale.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            index = 1,
            require = 0
    )
    private float mswcompat$scaleSonicBoomDamage(float damage) {
        if (! mswcompat$shouldScale.get()) {
            return damage;
        }
        return damage * mswcompat$scale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}