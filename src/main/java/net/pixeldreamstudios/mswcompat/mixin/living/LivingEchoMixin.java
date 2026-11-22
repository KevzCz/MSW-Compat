package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.mswcompat.living.LivingAbilities;
import net.soulsweaponry.items.abilities.posthit.ApplyEchoEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = ApplyEchoEffect.class, remap = false)
public abstract class LivingEchoMixin {

    @Inject(method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"),
            cancellable = true, remap = true)
    private void mswcompat$living(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (attacker instanceof PlayerEntity || attacker.getWorld().isClient()) {
            return;
        }
        LivingAbilities.echo((ApplyEchoEffect) (Object) this, stack, target, attacker);
        ci.cancel();
    }
}
