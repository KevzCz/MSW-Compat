package net.pixeldreamstudios.mswcompat.living;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.soulsweaponry.entity.mobs.Remnant;
import net.soulsweaponry.entity.projectile.CometSpearEntity;
import net.soulsweaponry.entity.projectile.DragonslayerSwordspearEntity;
import net.soulsweaponry.entity.projectile.DraupnirSpearEntity;
import net.soulsweaponry.entity.projectile.LeviathanAxeEntity;
import net.soulsweaponry.entity.projectile.MjolnirProjectile;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntityEvents;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;
import net.soulsweaponry.entity.projectile.noclip.GhostGlaiveEntity;
import net.soulsweaponry.entity.projectile.noclip.HolyMoonlightPillar;
import net.soulsweaponry.entity.projectile.noclip.MoonveilWave;
import net.soulsweaponry.entitydata.EchoDamageData;
import net.pixeldreamstudios.mswcompat.mixin.living.FlipEffectsAccess;
import net.pixeldreamstudios.mswcompat.mixin.living.LivingRiptideAccess;
import net.soulsweaponry.items.abilities.IAbility;
import net.soulsweaponry.items.abilities.IHasAbilities;
import net.soulsweaponry.items.abilities.abilitykeybind.ExplodeSavedEntities;
import net.soulsweaponry.items.abilities.detonateground.IDetonateGround;
import net.soulsweaponry.items.abilities.inventorytick.FlipEffects;
import net.soulsweaponry.items.abilities.inventorytick.HalfHealthResistances;
import net.soulsweaponry.items.abilities.inventorytick.SoulFeast;
import net.soulsweaponry.items.abilities.posthit.ApplyEchoEffect;
import net.soulsweaponry.items.abilities.posthit.LifeSteal;
import net.soulsweaponry.items.abilities.posthit.Overheal;
import net.soulsweaponry.items.abilities.stoppedusing.ChaosStorm;
import net.soulsweaponry.items.abilities.stoppedusing.Flameburst;
import net.soulsweaponry.items.abilities.stoppedusing.Moonfall;
import net.soulsweaponry.items.abilities.stoppedusing.MoonveilHorizontal;
import net.soulsweaponry.items.abilities.stoppedusing.Obliterate;
import net.soulsweaponry.items.abilities.stoppedusing.sneaking.MoonveilVertical;
import net.soulsweaponry.items.abilities.stoppedusing.sneaking.Riptide;
import net.soulsweaponry.items.abilities.stoppedusing.ShootMoonlight;
import net.soulsweaponry.items.abilities.stoppedusing.SonicBoom;
import net.soulsweaponry.items.abilities.stoppedusing.SwordLeap;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowCometSpear;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowDragonslayerSwordspear;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowDraupnirSpear;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowGhostGlaive;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowLeviathanAxe;
import net.soulsweaponry.items.abilities.stoppedusing.ThrowMjolnir;
import net.soulsweaponry.items.abilities.statboost.ShadowStep;
import net.soulsweaponry.items.abilities.targetdeath.SummonRemnant;
import net.soulsweaponry.items.abilities.usagetick.IChargeUsageTicks;
import net.soulsweaponry.items.abilities.userdamaged.Aftershock;
import net.soulsweaponry.particles.ParticleEvents;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.DamageSourceRegistry;
import net.soulsweaponry.registry.EffectRegistry;
import net.soulsweaponry.registry.EntityRegistry;
import net.soulsweaponry.registry.ParticleRegistry;
import net.soulsweaponry.registry.SoundRegistry;
import net.soulsweaponry.util.IAnimatedDeath;
import net.soulsweaponry.util.WeaponUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class LivingAbilities {

    public static final int CHARGE_TICKS = 10;
    public static final float NO_SCALE = 1.0F;
    private static final EquipmentSlot HAND = EquipmentSlot.MAINHAND;
    private static final int LIGHT_WEAR = 1;
    private static final int THROW_WEAR = 2;
    private static final int HEAVY_WEAR = 3;
    private static final int MOONFALL_WEAR = 5;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;
    private static final float LOW_PITCH = 0.5F;
    private static final float NO_ROLL = 0.0F;
    private static final float DIVERGENCE = 1.0F;
    private static final float NO_ENCHANT_BASE = 0.0F;
    private static final float LINE_YAW = 90.0F;
    private static final double LINE_START = 4.0D;
    private static final double MOONFALL_REACH = 3.0D;
    private static final double MOONFALL_SPREAD = 3.0D;
    private static final float MOONFALL_SPACING = 1.75F;
    private static final float PILLAR_PARTICLES = 1.0F;
    private static final int OUTBURST_PARTICLES = 150;
    private static final float OUTBURST_SCALE = 1.0F;
    private static final float GROUND_LIFT = 0.1F;
    private static final double WAVE_DROP = 0.3D;
    private static final float WAVE_SPEED = 1.0F;
    private static final byte WAVE_PARTICLES = 15;
    private static final int WAVE_DESPAWN_PARTICLES = 40;
    private static final double GLAIVE_DROP = 0.3D;
    private static final byte SONIC_STATUS = 62;
    private static final double SONIC_MOUTH = 1.6D;
    private static final int SONIC_TRAIL_EXTRA = 7;
    private static final float SONIC_VOLUME = 3.0F;
    private static final double SONIC_LIFT = 0.5D;
    private static final double SONIC_PUSH = 2.5D;
    private static final double UNIT = 1.0D;
    private static final double LEAP_RISE = 1.0D;
    private static final float NO_FALL = 0.0F;
    private static final double AFTERSHOCK_HEIGHT = 0.5D;
    private static final int NO_AMPLIFIER = 0;
    private static final byte VERTICAL_WAVE_PARTICLES = 10;
    private static final double VERTICAL_WAVE_DROP = 1.0D;
    private static final int VERTICAL_WAVE_ROTATION = 90;
    private static final float VERTICAL_WAVE_LIFT = 1.0F;
    private static final int RIPTIDE_TICKS = 20;
    private static final double RIPTIDE_HOP = 1.2D;
    private static final int CHARGE_MARGIN = 2;
    private static final int CHANNEL_END_MARGIN = 1;
    private static final int FINISHED_CHANNEL = 0;
    private static final double REMNANT_LIFT = 0.1D;
    private static final int REMNANT_PARTICLES = 50;
    private static final int RESISTANCE_TICKS = 40;
    private static final int JUST_DIED = 1;

    private LivingAbilities() {
    }

    public static boolean coolingDown(LivingEntity user, ItemStack stack) {
        return LivingCooldowns.coolingDown(user, stack.getItem());
    }

    public static boolean charges(ItemStack stack) {
        return stack.getItem() instanceof IHasAbilities abilities && !abilities.isDisabled(stack) && abilities.hasChargeToUseAbility();
    }

    public static boolean channels(ItemStack stack) {
        return stack.getItem() instanceof IHasAbilities abilities && abilities.getAbilities().stream().anyMatch(LivingAbilities::channel);
    }

    private static boolean channel(IAbility ability) {
        return ability instanceof IChargeUsageTicks && !ability.isSneakAbility() && !ability.isOffhandAbility();
    }

    public static int releaseTicks(LivingEntity user, ItemStack stack) {
        return channels(stack) ? Math.max(CHARGE_TICKS, stack.getMaxUseTime(user) - CHANNEL_END_MARGIN) : CHARGE_TICKS + CHARGE_MARGIN;
    }

    public static void stopChannel(IChargeUsageTicks self, ItemStack stack, LivingEntity user, int ticksUsed) {
        cool(user, stack, self.getCooldown(stack, ticksUsed));
        stack.damage(HEAVY_WEAR, user, HAND);
    }

    public static void finishChannels(IHasAbilities self, ItemStack stack, LivingEntity user) {
        for (IAbility ability : self.getAbilities()) {
            if (channel(ability)) {
                stopChannel((IChargeUsageTicks) ability, stack, user, FINISHED_CHANNEL);
            }
        }
    }

    @Nullable
    public static PlayerEntity playerOwner(LivingEntity user) {
        return user instanceof Ownable ownable && ownable.getOwner() instanceof PlayerEntity player ? player : null;
    }

    public static void halfHealth(HalfHealthResistances self, ItemStack stack, LivingEntity user) {
        int level = WeaponUtil.getUpgradeLevel(stack);
        if (user.getHealth() > user.getMaxHealth() * self.getThreshold(level)) {
            return;
        }
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, RESISTANCE_TICKS, (int) (self.resistanceAmp() + self.resistanceAmpPerLvl() * level), false, false));
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.MAGIC_RESISTANCE, RESISTANCE_TICKS, (int) (self.magicResistAmp() + self.magicResistAmpPerLvl() * level), false,
                false));
    }

    public static void soulFeast(SoulFeast self, ItemStack stack, LivingEntity user) {
        int level = WeaponUtil.getUpgradeLevel(stack);
        float range = self.range() + self.bonusRangePerLvl() * level;
        float heal = self.heal() + self.healPerLvl() * level;
        for (Entity entity : user.getWorld().getOtherEntities(user, user.getBoundingBox().expand(range))) {
            if (entity instanceof LivingEntity living && living.isDead() && deathTicks(living) == JUST_DIED) {
                user.heal(heal);
            }
        }
    }

    private static int deathTicks(LivingEntity living) {
        return living instanceof IAnimatedDeath animated ? animated.getDeathTicks() : living.deathTime;
    }

    public static void flipEffects(FlipEffects self, ItemStack stack, LivingEntity user) {
        if (coolingDown(user, stack)) {
            return;
        }
        FlipEffectsAccess access = (FlipEffectsAccess) (Object) self;
        List<RegistryEntry<StatusEffect>> flipped = new ArrayList<>();
        for (StatusEffectInstance instance : new ArrayList<>(user.getStatusEffects())) {
            RegistryEntry<StatusEffect> effect = instance.getEffectType();
            if (effect.value().getCategory() != StatusEffectCategory.HARMFUL) {
                continue;
            }
            RegistryEntry<StatusEffect> replacement = FlipEffectsAccess.mswcompat$flippable().getOrDefault(effect, StatusEffects.REGENERATION);
            user.addStatusEffect(new StatusEffectInstance(replacement, (int) (instance.getDuration() * access.mswcompat$durationMod()),
                    (int) (instance.getAmplifier() * access.mswcompat$ampMod())));
            flipped.add(effect);
        }
        flipped.forEach(user::removeStatusEffect);
        if (!flipped.isEmpty()) {
            cool(user, stack, scaled(stack, access.mswcompat$minCooldown(), access.mswcompat$cooldown(), access.mswcompat$reducedCooldownPerLvl()));
        }
    }

    public static void shadowStep(ShadowStep self, ItemStack stack, LivingEntity attacker) {
        if (coolingDown(attacker, stack)) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        attacker.addStatusEffect(new StatusEffectInstance(EffectRegistry.SHADOW_STEP, self.shadowStepTicks(), (int) (self.shadowStepBaseAmp() + self.bonusAmpPerLvl() * level)));
        cool(attacker, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void summonRemnant(SummonRemnant self, LivingEntity target, LivingEntity attacker) {
        PlayerEntity owner = playerOwner(attacker);
        if (owner == null || !(attacker.getWorld() instanceof ServerWorld world) || !(target.hasInvertedHealingAndHarm() || self.allowNonUndeadChance())
                || !self.canSummonEntity(world, attacker, self.getSummonsListId()) || attacker.getRandom().nextDouble() >= self.summonChance()) {
            return;
        }
        Remnant remnant = new Remnant(EntityRegistry.REMNANT, world);
        remnant.setPosition(target.getX(), target.getY() + REMNANT_LIFT, target.getZ());
        remnant.setOwner(owner);
        world.spawnEntity(remnant);
        self.saveSummonUuid(attacker, remnant.getUuid());
        world.playSound(null, target.getBlockPos(), SoundRegistry.NIGHTFALL_SPAWN_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
        ParticleHandler.particleOutburstMap(world, REMNANT_PARTICLES, target.getX(), target.getY(), target.getZ(), ParticleEvents.SOUL_RUPTURE_MAP, OUTBURST_SCALE);
    }

    private static void cool(LivingEntity user, ItemStack stack, int ticks) {
        LivingCooldowns.cool(user, stack.getItem(), ticks);
    }

    private static boolean ready(LivingEntity user, ItemStack stack, int ticksUsed) {
        return ticksUsed >= CHARGE_TICKS && !coolingDown(user, stack);
    }

    private static int scaled(ItemStack stack, int minimum, int cooldown, int perLevel) {
        return Math.max(minimum, cooldown - perLevel * WeaponUtil.getUpgradeLevel(stack));
    }

    public static void releaseAny(IHasAbilities self, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        int ticksUsed = WeaponUtil.getChargeTime(stack, user, remainingUseTicks);
        List<IAbility> abilities = self.getAbilities();
        boolean sneakCharge = abilities.stream().anyMatch(ability -> ability.isSneakAbility() && ability.isChargeToUse());
        boolean normalCharge = abilities.stream().anyMatch(ability -> !ability.isSneakAbility() && !ability.isOffhandAbility() && ability.isChargeToUse());
        boolean sneaking = sneakCharge && (!normalCharge || user.getRandom().nextBoolean());
        for (IAbility ability : abilities) {
            if (sneaking) {
                if (ability.isSneakAbility() && ability.isChargeToUse()) {
                    ability.sneakingOnStoppedUsing(stack, world, user, ticksUsed);
                }
            } else if (!ability.isSneakAbility() && !ability.isOffhandAbility()) {
                ability.onStoppedUsing(stack, world, user, ticksUsed);
                if (ability instanceof IChargeUsageTicks held) {
                    stopChannel(held, stack, user, ticksUsed);
                }
            }
        }
    }

    public static void release(Object ability, ItemStack stack, World world, LivingEntity user, int ticksUsed) {
        if (ability instanceof ChaosStorm storm) {
            if (ready(user, stack, ticksUsed)) {
                stack.damage(LIGHT_WEAR, user, HAND);
                storm.summonFlamePillars(world, stack, user);
                cool(user, stack, scaled(stack, storm.minCooldown(), storm.cooldown(), storm.reducedCooldownPerLvl()));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, storm.fireResistanceDuration(), NO_AMPLIFIER));
            }
        } else if (ability instanceof ShootMoonlight moonlight) {
            if (ticksUsed >= CHARGE_TICKS) {
                stack.damage(HEAVY_WEAR, user, HAND);
                moonlight.shootMoonlightProjectiles(world, user, stack);
                world.playSound(null, user.getBlockPos(), SoundRegistry.MOONLIGHT_BIG_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
            }
        } else if (ability instanceof ThrowCometSpear comet) {
            if (ticksUsed >= CHARGE_TICKS) {
                stack.damage(THROW_WEAR, user, HAND);
                cool(user, stack, scaled(stack, comet.minCooldown(), comet.cooldown(), comet.reducedCooldownPerLvl()));
                throwCopy(world, user, new CometSpearEntity(world, user, stack.copy()), comet.speed());
            }
        } else if (ability instanceof ThrowDragonslayerSwordspear swordspear) {
            if (ticksUsed >= CHARGE_TICKS) {
                stack.damage(LIGHT_WEAR, user, HAND);
                throwCopy(world, user, new DragonslayerSwordspearEntity(world, user, stack.copy()), swordspear.speed());
                float rain = world.isRaining() ? swordspear.rainingCooldownMod() : NO_SCALE;
                cool(user, stack, (int) Math.max(swordspear.minCooldown(),
                        (swordspear.cooldown() - WeaponUtil.getUpgradeLevel(stack) * swordspear.reducedCooldownPerLvl()) * rain));
            }
        } else if (ability instanceof ThrowDraupnirSpear draupnir) {
            if (ready(user, stack, ticksUsed)) {
                DraupnirSpearEntity entity = new DraupnirSpearEntity(world, user, stack.copy());
                throwCopy(world, user, entity, draupnir.projectileSpeed());
                ExplodeSavedEntities.saveEntityOnItem(stack, entity);
                cool(user, stack, scaled(stack, draupnir.minCooldown(), draupnir.cooldown(), draupnir.reducedCooldownPerLvl()));
                stack.damage(LIGHT_WEAR, user, HAND);
            }
        } else if (ability instanceof ThrowGhostGlaive glaive) {
            if (ticksUsed >= CHARGE_TICKS) {
                stack.damage(HEAVY_WEAR, user, HAND);
                GhostGlaiveEntity entity = new GhostGlaiveEntity(world, user, glaive.maxProjectileAge());
                entity.setDamage(glaive.projectileDamage() + WeaponUtil.getUpgradeLevel(stack) * glaive.bonusDamagePerLvl());
                entity.setPos(user.getX(), user.getEyeY() - GLAIVE_DROP, user.getZ());
                entity.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, glaive.projectileSpeed(), DIVERGENCE);
                entity.setPostureLoss(glaive.postureLoss());
                world.spawnEntity(entity);
                world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_TRIDENT_THROW.value(), SoundCategory.PLAYERS, VOLUME, LOW_PITCH);
                cool(user, stack, scaled(stack, glaive.minCooldown(), glaive.cooldown(), glaive.reducedCooldownPerLvl()));
            }
        } else if (ability instanceof ThrowLeviathanAxe axe) {
            if (ticksUsed >= CHARGE_TICKS) {
                stack.damage(HEAVY_WEAR, user, HAND);
                throwCopy(world, user, new LeviathanAxeEntity(world, user, stack.copy()), axe.baseSpeed() + WeaponUtil.getUpgradeLevel(stack) * axe.speedPerLvl());
            }
        } else if (ability instanceof ThrowMjolnir mjolnir) {
            if (ticksUsed >= CHARGE_TICKS) {
                throwCopy(world, user, new MjolnirProjectile(world, user, stack.copy()),
                        mjolnir.baseVelocity() + WeaponUtil.getUpgradeLevel(stack) * mjolnir.bonusVelocityPerLvl());
                stack.damage(HEAVY_WEAR, user, HAND);
            }
        }
    }

    private static void throwCopy(World world, LivingEntity user, PersistentProjectileEntity entity, float speed) {
        entity.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, speed, DIVERGENCE);
        entity.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
        world.spawnEntity(entity);
        world.playSoundFromEntity(null, entity, SoundEvents.ITEM_TRIDENT_THROW.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
    }

    public static void flameburst(Flameburst self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float moltenScale) {
        if (ticksUsed < CHARGE_TICKS) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        float radius = self.moltenMetalBaseRadius() + self.moltenMetalRadiusPerLvl() * level;
        float damage = (self.moltenMetalBaseDamage() + self.moltenMetalDamagePerLvl() * level) * moltenScale;
        WeaponUtil.doConsumerOnLine(world, user.getYaw() + LINE_YAW, user.getPos(), LINE_START, self.flamePillarAmount(), self.spacingModifier(),
                (position, warmup, yaw) -> {
                    FlamePillar pillar = new FlamePillar(world, user, self.flamePillarBaseRadius() + self.flamePillarRadiusPerLvl() * level, warmup,
                            DamagingWarmupEntityEvents.SPAWN_MOLTEN_METAL);
                    pillar.setYaw(yaw);
                    pillar.setOtherAttributes(new DamagingWarmupEntityEvents.OtherAttributes(damage, radius));
                    pillar.setDamage(self.flamePillarBaseDamage() + self.flamePillarDamagePerLvl() * level);
                    pillar.setPos(position.getX(), position.getY(), position.getZ());
                    world.spawnEntity(pillar);
                });
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void moonfall(Moonfall self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float scale) {
        if (ticksUsed < CHARGE_TICKS || !(world instanceof ServerWorld server)) {
            return;
        }
        StatusEffectInstance herald = user.getStatusEffect(EffectRegistry.MOON_HERALD);
        int empowered = herald == null ? 0 : self.reducedCooldownPerEffectAmp() * herald.getAmplifier();
        int level = WeaponUtil.getUpgradeLevel(stack);
        int ruptures = (int) (self.pillarAmount() + self.bonusPillarPerLvl() * level);
        Vec3d away = user.getRotationVector().multiply(MOONFALL_REACH).add(user.getPos());
        BlockPos area = new BlockPos((int) away.x, (int) user.getY(), (int) away.z);
        float power = (self.damage() + self.bonusDamagePerLvl() * level) * scale;
        float knockup = self.knockup() + self.bonusKnockupPerLvl() * level;
        DamageSource attack = server.getDamageSources().mobAttack(user);
        for (Entity entity : world.getOtherEntities(user, new Box(area).expand(MOONFALL_SPREAD))) {
            if (entity instanceof LivingEntity target) {
                entity.damage(DamageSourceRegistry.create(world, DamageSourceRegistry.OBLITERATED, user),
                        power + self.bonusDamageEnchantMod() * EnchantmentHelper.getDamage(server, stack, target, attack, NO_ENCHANT_BASE));
                entity.addVelocity(0.0D, self.knockup(), 0.0D);
            }
        }
        WeaponUtil.doConsumerOnLine(world, user.getYaw() + LINE_YAW, user.getPos(), LINE_START, ruptures, MOONFALL_SPACING, (position, warmup, yaw) -> {
            HolyMoonlightPillar pillar = new HolyMoonlightPillar(EntityRegistry.HOLY_MOONLIGHT_PILLAR, world);
            pillar.setOwner(user);
            pillar.setParticleAmountMod(PILLAR_PARTICLES);
            pillar.setRadius(self.pillarRadius());
            pillar.setDamage(power);
            pillar.setKnockUp(knockup);
            pillar.setWarmup(warmup);
            pillar.setPos(position.getX(), position.getY(), position.getZ());
            world.spawnEntity(pillar);
        });
        cool(user, stack, Math.max(self.minCooldown(), self.cooldown() - level * self.reducedCooldownPerLvl() - empowered));
        stack.damage(MOONFALL_WEAR, user, HAND);
        world.playSound(null, area, SoundRegistry.MOONLIGHT_BIG_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
        world.playSound(null, area, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
        ParticleHandler.particleOutburstMap(world, OUTBURST_PARTICLES, away.getX(), user.getY(), away.getZ(), ParticleEvents.MOONFALL_MAP, OUTBURST_SCALE);
    }

    public static void moonveil(MoonveilHorizontal self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float damageScale) {
        if (!ready(user, stack, ticksUsed)) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        MoonveilWave entity = new MoonveilWave(world, user, self.maxAge());
        entity.setAreaParticle(ParticleRegistry.MOONVEIL_PARTICLE);
        entity.setAreaParticleCount(WAVE_PARTICLES);
        entity.setDespawnParticleCount(WAVE_DESPAWN_PARTICLES);
        entity.setDespawnParticle(ParticleRegistry.BLUE_FLAME);
        entity.setPos(user.getX(), user.getEyeY() - WAVE_DROP, user.getZ());
        entity.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, WAVE_SPEED, DIVERGENCE);
        entity.setDamage((self.baseDamage() + self.bonusDamagePerLvl() * level) * damageScale);
        world.spawnEntity(entity);
        world.playSound(null, user.getBlockPos(), SoundRegistry.MOONVEIL_HORIZONTAL, SoundCategory.PLAYERS, VOLUME, PITCH);
        stack.damage(HEAVY_WEAR, user, HAND);
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void moonveilVertical(MoonveilVertical self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float damageScale) {
        if (!ready(user, stack, ticksUsed)) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        MoonveilWave entity = new MoonveilWave(EntityRegistry.MOONVEIL_VERTICAL, world, user, self.maxAge());
        entity.setAreaParticle(ParticleRegistry.MOONVEIL_PARTICLE);
        entity.setAreaParticleCount(VERTICAL_WAVE_PARTICLES);
        entity.setDespawnParticle(ParticleRegistry.BLUE_FLAME);
        entity.setPos(user.getX(), user.getEyeY() - VERTICAL_WAVE_DROP, user.getZ());
        entity.setDespawnParticleCount(WAVE_DESPAWN_PARTICLES);
        entity.setModelRotationX(VERTICAL_WAVE_ROTATION);
        entity.setModelTranslationY(VERTICAL_WAVE_LIFT);
        entity.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, WAVE_SPEED, DIVERGENCE);
        entity.setDamage((self.baseDamage() + self.bonusDamagePerLvl() * level) * damageScale);
        world.spawnEntity(entity);
        world.playSound(null, user.getBlockPos(), SoundRegistry.MOONVEIL_VERTICAL, SoundCategory.PLAYERS, VOLUME, PITCH);
        stack.damage(HEAVY_WEAR, user, HAND);
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void riptide(Riptide self, ItemStack stack, World world, LivingEntity user, int ticksUsed) {
        if (ticksUsed < CHARGE_TICKS) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        WeaponUtil.launchTarget(user, self.launchPower() + level * self.bonusPowerPerLvl(), false);
        LivingRiptideAccess spin = (LivingRiptideAccess) user;
        spin.mswcompat$setRiptideTicks(RIPTIDE_TICKS);
        spin.mswcompat$setRiptideDamage(self.collisionDamage());
        spin.mswcompat$setRiptideStack(stack);
        user.removeAllPassengers();
        spin.mswcompat$setLivingFlag(LivingRiptideAccess.mswcompat$riptideFlag(), true);
        world.playSoundFromEntity(null, user, SoundEvents.ITEM_TRIDENT_RIPTIDE_3.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
        if (user.isOnGround()) {
            user.move(MovementType.SELF, new Vec3d(0.0D, RIPTIDE_HOP, 0.0D));
        }
        if (self.calculatedFallDuration() > 0) {
            user.addStatusEffect(new StatusEffectInstance(EffectRegistry.CALCULATED_FALL, self.calculatedFallDuration(),
                    (int) (self.calculatedFallAmp() + self.bonusAmpPerLvl() * level)));
        }
        if (self.shouldApplyCooldown().test(world, user)) {
            cool(user, stack, Math.max(self.minCooldown(), self.cooldown() - level * self.reducedCooldownPerLvl()));
        }
        stack.damage(HEAVY_WEAR, user, HAND);
    }

    public static void obliterate(Obliterate self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float damageScale) {
        if (!ready(user, stack, ticksUsed) || !(world instanceof ServerWorld server)) {
            return;
        }
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        stack.damage(HEAVY_WEAR, user, HAND);
        Vec3d away = user.getRotationVector().multiply(self.rangeOutwards()).add(user.getPos());
        BlockPos area = new BlockPos((int) away.x, (int) user.getY(), (int) away.z);
        float power = self.baseDamage() + WeaponUtil.getUpgradeLevel(stack) * self.bonusDamagePerLvl();
        DamageSource attack = server.getDamageSources().mobAttack(user);
        for (Entity entity : world.getOtherEntities(user, new Box(area).expand(self.aoeExpansion()))) {
            if (entity instanceof LivingEntity target) {
                entity.damage(DamageSourceRegistry.create(world, DamageSourceRegistry.OBLITERATED, user),
                        (power + self.enchantBonusModifier() * EnchantmentHelper.getDamage(server, stack, target, attack, NO_ENCHANT_BASE)) * damageScale);
                entity.addVelocity(0.0D, self.yVelocity(), 0.0D);
            }
        }
        world.playSound(null, area, SoundRegistry.NIGHTFALL_BONK_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
        ParticleHandler.particleOutburstMap(world, OUTBURST_PARTICLES, area.getX(), area.getY() + GROUND_LIFT, area.getZ(), ParticleEvents.OBLITERATE_MAP,
                OUTBURST_SCALE);
    }

    public static void sonicBoom(SonicBoom self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float scale) {
        if (!ready(user, stack, ticksUsed) || !(world instanceof ServerWorld server)) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        float searchRange = self.getSearchRange(level);
        float maxRange = self.getMaxRange(level);
        Predicate<LivingEntity> nonTeammate = entity -> !entity.isTeammate(user) && !(entity instanceof ArmorStandEntity) && !entity.equals(user);
        LivingEntity target = null;
        LivingEntity current = user.getAttacking();
        if (current != null && nonTeammate.test(current) && user.squaredDistanceTo(current) <= maxRange * maxRange) {
            target = current;
        }
        if (target == null) {
            TargetPredicate predicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(searchRange).ignoreVisibility().setPredicate(nonTeammate);
            target = world.getClosestEntity(LivingEntity.class, predicate, user, user.getX(), user.getY(), user.getZ(),
                    user.getBoundingBox().expand(Math.max(searchRange, maxRange)));
        }
        if (target == null || user.squaredDistanceTo(target) > maxRange * maxRange) {
            world.playSound(null, user.getBlockPos(), SoundEvents.PARTICLE_SOUL_ESCAPE.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
            return;
        }
        float damage = (self.damage() + self.bonusDamagePerLvl() * level
                + self.bonusEnchantDamageMod() * EnchantmentHelper.getDamage(server, stack, target, server.getDamageSources().mobAttack(user), NO_ENCHANT_BASE))
                * scale;
        float knockback = self.knockbackPowerMod() + self.bonusKnockbackModPerLvl() * level;
        world.sendEntityStatus(user, SONIC_STATUS);
        Vec3d from = user.getPos().add(0.0D, SONIC_MOUTH, 0.0D);
        Vec3d to = target.getEyePos();
        Vec3d direction = to.subtract(from).normalize();
        double length = from.distanceTo(to);
        for (int step = 1; step < MathHelper.floor(length) + SONIC_TRAIL_EXTRA; step++) {
            Vec3d point = from.add(direction.multiply(step));
            server.spawnParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, SONIC_VOLUME, PITCH);
        target.damage(world.getDamageSources().sonicBoom(user), damage);
        user.onAttacking(target);
        double resist = target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        double lift = SONIC_LIFT * (UNIT - resist);
        double push = SONIC_PUSH * (UNIT - resist);
        target.addVelocity(new Vec3d(direction.x * push, direction.y * lift, direction.z * push).multiply(knockback));
        stack.damage(THROW_WEAR, user, HAND);
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void swordLeap(SwordLeap self, ItemStack stack, World world, LivingEntity user, int ticksUsed, float scale) {
        int level = WeaponUtil.getUpgradeLevel(stack);
        int amplifier = (int) ((int) (self.damage() + self.bonusDamagePerLvl() * level) * scale);
        int cooldown = Math.max(self.minCooldown(), self.cooldown() - self.reducedCooldownPerLvl() * level);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.CALCULATED_FALL, self.calculatedFallDuration(), amplifier));
        if (ticksUsed >= CHARGE_TICKS) {
            cooldown = (int) (cooldown * self.cooldownModFullyCharged());
            Vec3d rotation = user.getRotationVector();
            user.addVelocity(rotation.getX(), LEAP_RISE, rotation.getZ());
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, VOLUME, PITCH);
        } else {
            IDetonateGround.triggerCalculateFall(user, NO_FALL, world.getDamageSources().fall());
            cooldown = (int) (cooldown * self.cooldownModNotCharged());
        }
        stack.damage(HEAVY_WEAR, user, HAND);
        cool(user, stack, cooldown);
    }

    public static void lifeSteal(LifeSteal self, ItemStack stack, LivingEntity attacker, float scale) {
        if (coolingDown(attacker, stack) || attacker.getHealth() >= attacker.getMaxHealth()) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        attacker.heal((self.baseHeal + self.bonusHealPerLvl * level) * scale);
        cool(attacker, stack, Math.max(self.minCooldown, self.cooldown - self.reducedCooldownPerLvl * level));
    }

    public static void overheal(Overheal self, ItemStack stack, LivingEntity attacker, float durationScale, float amplifierScale) {
        if (attacker.getHealth() < attacker.getMaxHealth() || coolingDown(attacker, stack)) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        int duration = (int) ((int) (self.baseDuration() + self.bonusDurationPerLvl() * level) * durationScale);
        int amplifier = (int) ((int) (self.baseAbsorptionAmp() + self.bonusAmpPerLvl() * level) * amplifierScale);
        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, duration, amplifier));
        cool(attacker, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void echo(ApplyEchoEffect self, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (coolingDown(attacker, stack) || target.hasStatusEffect(EffectRegistry.ECHO)) {
            return;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        int duration = (int) (self.duration() + self.durationPerLvl() * level);
        int amplifier = (int) (self.amp() + self.ampPerLvl() * level);
        EchoDamageData.setEchoDamageSavedMod(target, self.savedDamageMod() + self.savedDamageAddedModPerLvl() * level);
        target.addStatusEffect(new StatusEffectInstance(EffectRegistry.ECHO, duration, amplifier));
        cool(attacker, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }

    public static void aftershock(Aftershock self, ItemStack stack, LivingEntity user) {
        World world = user.getWorld();
        int level = WeaponUtil.getUpgradeLevel(stack);
        if (user.getHealth() > user.getMaxHealth() * self.getThreshold(level) || coolingDown(user, stack) || !(world instanceof ServerWorld server)) {
            return;
        }
        float knockback = self.knockback() + self.knockbackPerLvl() * level;
        float damage = self.damage() + self.bonusDamagePerLvl() * level;
        ParticleHandler.singleParticle(world, ParticleTypes.EXPLOSION_EMITTER, user.getX(), user.getBodyY(AFTERSHOCK_HEIGHT), user.getZ(), 0.0D, 0.0D, 0.0D);
        DamageSource attack = server.getDamageSources().mobAttack(user);
        for (Entity entity : world.getOtherEntities(user, user.getBoundingBox().expand(self.expansion()))) {
            if (entity instanceof LivingEntity target && !target.isTeammate(user)) {
                float dealt = damage + EnchantmentHelper.getDamage(server, stack, target, attack, NO_ENCHANT_BASE) * self.bonusEnchantDamageMod();
                for (StatusEffectInstance instance : self.targetHitEffects()) {
                    target.addStatusEffect(new StatusEffectInstance(instance));
                }
                target.damage(attack, dealt);
                target.takeKnockback(knockback, user.getX() - target.getX(), user.getZ() - target.getZ());
            }
        }
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
    }
}
