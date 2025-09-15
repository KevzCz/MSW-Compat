package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.soulsweaponry.items.hammer.Tonitrus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Scales Tonitrus behavior by spell_power:lightning:
 * - ChainLightning damage: × (1 + lightning/40)
 * - STORMVEIL amp on use: + (lightning/20) before ceil(...)
 * - Empowered lightning: sets channeler to the attacking player
 */
@Pseudo
@Mixin(value = Tonitrus.class, remap = false)
public abstract class TonitrusLightningScalingMixin {
    @Unique private static final Identifier mswcompat$LIGHTNING_ID = Identifier.of("spell_power", "lightning");

    @Unique private static final ThreadLocal<Float> mswcompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> mswcompat$ampBonus   = ThreadLocal.withInitial(() -> 0.0F);
    @Unique private static final ThreadLocal<ServerPlayerEntity> mswcompat$channeler = new ThreadLocal<>();


    @Unique
    private static RegistryEntry.Reference<EntityAttribute> mswcompat$getLightningAttrRef() {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$LIGHTNING_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(mswcompat$LIGHTNING_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
            }
        }
        return entry;
    }

    @Unique
    private static double mswcompat$getLightningPower(LivingEntity user) {
        RegistryEntry.Reference<EntityAttribute> entry = mswcompat$getLightningAttrRef();
        if (entry == null || user == null) return 0.0;
        return user.getAttributeValue(entry);
    }


    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheDamageScaleAndChanneler(net.minecraft.item.ItemStack stack,
                                                        LivingEntity target,
                                                        LivingEntity attacker,
                                                        CallbackInfoReturnable<Boolean> cir) {
        float scale = 1.0F;
        if (attacker != null) {
            double power = mswcompat$getLightningPower(attacker);
            scale = (float)(1.0 + power / 40.0);
            if (attacker instanceof ServerPlayerEntity sp) {
                mswcompat$channeler.set(sp);
            } else {
                mswcompat$channeler.set(null);
            }
        }
        mswcompat$damageScale.set(scale);
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/items/abilities/ChainLightning;trigger(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;ZFD)V",
                    remap = false
            ),
            index = 4,
            require = 0
    )
    private float mswcompat$scaleChainLightningDamage(float baseDamage) {
        return baseDamage * mswcompat$damageScale.get();
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
            ),
            require = 0
    )
    private boolean mswcompat$spawnWithChanneler(World world, Entity entity) {
        if (entity instanceof net.minecraft.entity.LightningEntity lightning) {
            ServerPlayerEntity sp = mswcompat$channeler.get();
            if (sp != null) {
                lightning.setChanneler(sp);
            }
        }
        return world.spawnEntity(entity);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$clearPostHitCaches(net.minecraft.item.ItemStack stack,
                                              LivingEntity target,
                                              LivingEntity attacker,
                                              CallbackInfoReturnable<Boolean> cir) {
        mswcompat$damageScale.remove();
        mswcompat$channeler.remove();
    }


    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("HEAD"),
            require = 0
    )
    private void mswcompat$cacheAmpBonus(World world, PlayerEntity user, Hand hand,
                                         CallbackInfoReturnable<net.minecraft.util.TypedActionResult<net.minecraft.item.ItemStack>> cir) {
        float bonus = 0.0F;
        if (user != null) {
            double power = mswcompat$getLightningPower(user);
            bonus = (float)(power / 20.0);
        }
        mswcompat$ampBonus.set(bonus);
    }

    @ModifyArg(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I"
            ),
            index = 0,
            require = 0
    )
    private float mswcompat$addAmpBonus(float original) {
        return original + mswcompat$ampBonus.get();
    }

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("RETURN"),
            require = 0
    )
    private void mswcompat$clearUseBonus(World world, PlayerEntity user, Hand hand,
                                         CallbackInfoReturnable<net.minecraft.util.TypedActionResult<net.minecraft.item.ItemStack>> cir) {
        mswcompat$ampBonus.remove();
    }
}
