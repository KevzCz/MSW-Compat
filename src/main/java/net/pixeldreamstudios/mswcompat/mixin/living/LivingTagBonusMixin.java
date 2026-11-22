package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.soulsweaponry.items.abilities.bonusdamage.EntityTagBonus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityTagBonus.class, remap = false)
public abstract class LivingTagBonusMixin {

    @Unique
    private static final float NO_BONUS = 0.0F;

    @Shadow
    @Final
    private TagKey<EntityType<?>> tag;

    @Inject(method = "getBonusAttackDamage(Lnet/minecraft/entity/Entity;FLnet/minecraft/entity/damage/DamageSource;)F", at = @At("HEAD"), cancellable = true, remap = true)
    private void mswcompat$living(Entity target, float baseAttackDamage, DamageSource source, CallbackInfoReturnable<Float> cir) {
        if (!(source.getAttacker() instanceof LivingEntity attacker) || attacker instanceof PlayerEntity) {
            return;
        }
        cir.setReturnValue(target.getType().isIn(tag) ? ((EntityTagBonus) (Object) this).getTotalBonus(attacker.getMainHandStack()) : NO_BONUS);
    }
}
