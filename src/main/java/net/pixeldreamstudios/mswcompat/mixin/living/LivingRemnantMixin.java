package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.targetdeath.SummonRemnant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SummonRemnant.class, remap = false)
public abstract class LivingRemnantMixin {

    @Inject(method = "onTargetDeath(Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void mswcompat$living(DamageSource source, ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (attacker instanceof PlayerEntity) {
            return;
        }
        LivingAbilities.summonRemnant((SummonRemnant) (Object) this, target, attacker);
        ci.cancel();
    }
}
