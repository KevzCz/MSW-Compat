package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LightningDamageScalingMixin {
    @Unique
    private static final Identifier mswcompat$LIGHTNING_POWER_ID = Identifier.of("spell_power", "lightning");

    @Unique
    private static final ThreadLocal<Float> mswcompat$add = ThreadLocal.withInitial(() -> 0.0F);

    @Inject(method = "onStruckByLightning", at = @At("HEAD"))
    private void mswcompat$cacheAdditiveBonus(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        float add = 0.0F;
        ServerPlayerEntity sp = lightning.getChanneler();
        if (sp != null) {
            RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$LIGHTNING_POWER_ID);
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
            if (entry != null) {
                double power = sp.getAttributeValue(entry);
                add = (float) (power / 2.0);  // additive bonus when channeled by player
            }
        }
        mswcompat$add.set(add);
    }

    @Redirect(
            method = "onStruckByLightning",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean mswcompat$customLightningDamage(Entity instance,
                                                    net.minecraft.entity.damage.DamageSource source,
                                                    float baseAmount,
                                                    ServerWorld world,
                                                    LightningEntity lightning) {
        ServerPlayerEntity channeler = lightning.getChanneler();

        // Only modify behavior if a player actually channeled this lightning
        if (channeler == null) {
            return instance.damage(source, baseAmount);
        }

        // 1) Do not damage the channeler if they're in range and got struck
        if (instance == channeler) {
            return false;
        }

        // 2) Do not damage passive or tamed entities
        if (instance instanceof PassiveEntity) {
            return false;
        }
        if (instance instanceof TameableEntity tame && tame.isTamed()) {
            return false;
        }

        // 3) Apply additive bonus from spell_power:lightning
        float amount = baseAmount + mswcompat$add.get();

        // 4) Lightning is 80% effective against players (targets), only for player-channeled lightning
        if (instance instanceof PlayerEntity) {
            amount *= 0.9F;
        }

        return instance.damage(source, amount);
    }

    @Inject(method = "onStruckByLightning", at = @At("RETURN"))
    private void mswcompat$clear(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        mswcompat$add.remove();
    }
}
