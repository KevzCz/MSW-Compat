package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.DamageScalingHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.soulsweaponry.items.abilities.stoppedusing.Moonfall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin( value = Moonfall.class, remap = false )
public abstract class MoonfallMixin {
    @Unique private static final ThreadLocal<Float> mswcompat$scale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            remap = true
)
    private void mswcompat$cacheScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        boolean shouldScale = ItemMatcher.isHolyMoonlightGreatsword(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (! shouldScale) {
            mswcompat$scale.set(1.0F);
            return;
        }

        float factor = 1.0F;
        if (user != null) {
            factor = DamageScalingHelper.attackDamageOnly(
                    user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                    ConfigHelper.getBaselineValue("holy_moonlight_greatsword.attack_damage_baseline", 10.0F));
        }

        mswcompat$scale.set(factor);
    }

    @ModifyVariable(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "STORE"),
            ordinal = 0,
            remap = true
)
    private float mswcompat$scalePower(float power) {
        if (! mswcompat$shouldScale.get()) {
            return power;
        }
        return power * mswcompat$scale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            remap = true
)
    private void mswcompat$clearScale(ItemStack stack, World world, LivingEntity user, int ticksUsed, CallbackInfo ci) {
        mswcompat$scale.remove();
        mswcompat$shouldScale.remove();
    }
}