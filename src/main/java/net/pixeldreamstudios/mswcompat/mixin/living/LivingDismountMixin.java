package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.mswcompat.living.LivingActives;
import net.soulsweaponry.entitydata.UmbralTrespassData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingDismountMixin {

    @Inject(method = "onDismounted", at = @At("HEAD"))
    private void mswcompat$trespassStrike(Entity vehicle, CallbackInfo ci) {
        LivingEntity user = (LivingEntity) (Object) this;
        if (user instanceof PlayerEntity || user.getWorld().isClient() || !(vehicle instanceof LivingEntity target)
                || !UmbralTrespassData.shouldDamageRiding(user)) {
            return;
        }
        LivingActives.trespassStrike(user, target);
    }
}
