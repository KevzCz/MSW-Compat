package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.soulsweaponry.entitydata.UmbralTrespassData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class UmbralTrespassDataMixin {

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            )
    )
    private boolean mswcompat$forcePlayerAttackSource(LivingEntity target, DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity player
                && UmbralTrespassData.shouldDamageRiding(player)) {
            source = player.getDamageSources().playerAttack(player);
        }
        return ((Entity) target).damage(source, amount);
    }
}
