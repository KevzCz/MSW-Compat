package net.pixeldreamstudios.mswcompat.mixin.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.config.ConfigHelper;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MSWCompatIdentifiers;
import net.pixeldreamstudios.mswcompat.util.SpellPowerHelper;
import net.soulsweaponry.items.abilities.use.Stormveil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin( value = Stormveil.class, remap = false )
public abstract class StormveilMixin {
    @Unique private static final ThreadLocal<Integer> mswcompat$ampBonus = ThreadLocal.withInitial(() -> 0); // Changed to Integer
    @Unique private static final ThreadLocal<Boolean> mswcompat$shouldScale = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheBonus(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        boolean shouldScale = ItemMatcher.isTonitrus(stack);
        mswcompat$shouldScale.set(shouldScale);

        if (!shouldScale) {
            mswcompat$ampBonus.set(0);
            return;
        }

        int bonus = 0;
        if (user != null) {
            float ampPer = ConfigHelper.getBaselineValue("tonitrus.amplifier_per_lightning", 20.0F);

            if (ampPer > 0.0F) {
                double power = SpellPowerHelper.getEffectiveSpellPower(user, MSWCompatIdentifiers.SpellPower.LIGHTNING);
                bonus = (int)(power / ampPer);
            }
        }

        mswcompat$ampBonus.set(bonus);
    }

    @ModifyVariable(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 0
    )
    private int mswcompat$addAmpBonus(int amp) {
        if (!mswcompat$shouldScale.get()) {
            return amp;
        }
        return amp + mswcompat$ampBonus.get(); 
    }

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("TAIL"),
            require = 0
    )
    private void mswcompat$clearBonus(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        mswcompat$ampBonus.remove();
        mswcompat$shouldScale.remove();
    }
}