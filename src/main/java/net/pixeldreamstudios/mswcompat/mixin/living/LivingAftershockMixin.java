package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.userdamaged.Aftershock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = Aftershock.class, remap = false)
public abstract class LivingAftershockMixin {

    @Inject(method = "onUserDamaged(Lnet/minecraft/entity/damage/DamageSource;FLnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"), cancellable = true, remap = true)
    private void mswcompat$living(DamageSource source, float amount, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir) {
        if (user instanceof PlayerEntity || user.getWorld().isClient()) {
            return;
        }
        LivingAbilities.aftershock((Aftershock) (Object) this, stack, user);
        cir.setReturnValue(true);
    }
}
