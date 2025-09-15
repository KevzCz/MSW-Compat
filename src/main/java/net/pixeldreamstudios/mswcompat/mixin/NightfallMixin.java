package net.pixeldreamstudios.mswcompat.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.hammer.Nightfall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(Nightfall.class)
public abstract class NightfallMixin {
    @Unique private static final Identifier mswcompat$SOUL_ID = Identifier.of("spell_power", "soul");
    @Unique private static final float mswcompat$AD_BASELINE = 11.0F;
    @Unique private static final float mswcompat$SOUL_BASELINE = 20.0F;

    @Unique
    private static float mswcompat$scale(LivingEntity attacker) {
        if (attacker == null) return 1.0F;

        double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, mswcompat$SOUL_ID);
        RegistryEntry<EntityAttribute> soulAttr = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        double soul = soulAttr != null ? attacker.getAttributeValue(soulAttr) : 0.0D;

        float adPart = mswcompat$AD_BASELINE > 0.0F ? (float)(ad / mswcompat$AD_BASELINE) : 1.0F;
        float soulPart = mswcompat$SOUL_BASELINE > 0.0F ? (float)(soul / mswcompat$SOUL_BASELINE) : 0.0F;

        float factor = 0.5F * adPart + 0.5F * soulPart;
        return Math.max(0.0F, factor);
    }

    @Redirect(
            method = "onStoppedUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean mswcompat$scaleNightfallAbilityDamage(Entity instance,
                                                          DamageSource source,
                                                          float originalAmount,
                                                          ItemStack stack,
                                                          World world,
                                                          LivingEntity user,
                                                          int remainingUseTicks) {
        float ability = (float) ConfigConstructor.nightfall_ability_damage;
        float scaledAbility = ability * mswcompat$scale(user);

        float ench = 0.0F;
        if (world instanceof ServerWorld serverWorld && instance instanceof LivingEntity target && user instanceof net.minecraft.entity.player.PlayerEntity player) {
            ench = EnchantmentHelper.getDamage(serverWorld, stack, target, world.getDamageSources().playerAttack(player), 0.0F);
        }

        float newAmount = scaledAbility + 2.0F * ench;
        return instance.damage(source, newAmount);
    }
}
