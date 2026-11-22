package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.statboost.ShadowStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShadowStep.class, remap = false)
public abstract class LivingShadowStepMixin {

    @Inject(method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), remap = true)
    private void mswcompat$living(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (attacker instanceof PlayerEntity || attacker.getWorld().isClient()) {
            return;
        }
        LivingAbilities.shadowStep((ShadowStep) (Object) this, stack, attacker);
    }
}
