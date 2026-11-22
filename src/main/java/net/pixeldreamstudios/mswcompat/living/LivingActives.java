package net.pixeldreamstudios.mswcompat.living;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pixeldreamstudios.mswcompat.mixin.living.CannonballAccess;
import net.pixeldreamstudios.mswcompat.mixin.living.GatlingAccess;
import net.pixeldreamstudios.mswcompat.mixin.living.VeilOfFireAccess;
import net.pixeldreamstudios.mswcompat.mixin.living.WabbajackAccess;
import net.pixeldreamstudios.mswcompat.mixin.living.WitherReleaseAccess;
import net.pixeldreamstudios.mswcompat.util.AbilityScales;
import net.pixeldreamstudios.mswcompat.util.ItemMatcher;
import net.pixeldreamstudios.mswcompat.util.MswCompatBleedContext;
import net.pixeldreamstudios.mswcompat.util.MswCompatScaleContext;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.projectile.DragonStaffProjectile;
import net.soulsweaponry.entity.projectile.DraupnirSpearEntity;
import net.soulsweaponry.entity.projectile.GrowingFireball;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.entity.projectile.WitheredWabbajackProjectile;
import net.soulsweaponry.entity.projectile.arrow.ChargedArrow;
import net.soulsweaponry.entity.projectile.noclip.ArrowStormEntity;
import net.soulsweaponry.entity.projectile.noclip.WarmupLightningEntity;
import net.soulsweaponry.entitydata.BleedData;
import net.soulsweaponry.entitydata.UmbralTrespassData;
import net.soulsweaponry.items.abilities.IAbility;
import net.soulsweaponry.items.abilities.IHasAbilities;
import net.soulsweaponry.items.abilities.abilitykeybind.ArrowStorm;
import net.soulsweaponry.items.abilities.abilitykeybind.BasicKeybindAbility;
import net.soulsweaponry.items.abilities.abilitykeybind.BloodlustAbility;
import net.soulsweaponry.items.abilities.abilitykeybind.CircleLightningCall;
import net.soulsweaponry.items.abilities.abilitykeybind.Cloudburst;
import net.soulsweaponry.items.abilities.abilitykeybind.ExplodeSavedEntities;
import net.soulsweaponry.items.abilities.abilitykeybind.LifeLeach;
import net.soulsweaponry.items.abilities.abilitykeybind.LightningCall;
import net.soulsweaponry.items.abilities.abilitykeybind.LunarHerald;
import net.soulsweaponry.items.abilities.abilitykeybind.NightsEdgeAbility;
import net.soulsweaponry.items.abilities.abilitykeybind.ShootDragonProjectile;
import net.soulsweaponry.items.abilities.abilitykeybind.Unbreakable;
import net.soulsweaponry.items.abilities.abilitykeybind.VeilOfFireAbility;
import net.soulsweaponry.items.abilities.abilitykeybind.sneaking.SummonDraupnirSpears;
import net.soulsweaponry.items.abilities.attackclick.ShootSmallMoonlight;
import net.soulsweaponry.items.abilities.use.Cleanse;
import net.soulsweaponry.items.abilities.use.ShootCannonball;
import net.soulsweaponry.items.abilities.use.ShootChungusHeads;
import net.soulsweaponry.items.abilities.use.ShootRandomProjectile;
import net.soulsweaponry.items.abilities.use.ShootSilverBullet;
import net.soulsweaponry.items.abilities.use.Stormveil;
import net.soulsweaponry.items.abilities.use.UmbralTrespass;
import net.soulsweaponry.items.abilities.use.WitherSoulRelease;
import net.soulsweaponry.items.abilities.usagetick.GatlingSilverBullets;
import net.soulsweaponry.items.abilities.usagetick.IChargeUsageTicks;
import net.soulsweaponry.items.staff.ChungusStaff;
import net.soulsweaponry.items.sword.DarkMoonGreatsword;
import net.soulsweaponry.items.sword.HeapOfRawIron;
import net.soulsweaponry.particles.ParticleEvents;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.ComponentRegistry;
import net.soulsweaponry.registry.DamageSourceRegistry;
import net.soulsweaponry.registry.EffectRegistry;
import net.soulsweaponry.registry.EntityRegistry;
import net.soulsweaponry.registry.SoundRegistry;
import net.soulsweaponry.util.LuckChosenObject;
import net.soulsweaponry.util.WeaponUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LivingActives {

    private static final EquipmentSlot HAND = EquipmentSlot.MAINHAND;
    private static final int NO_AMPLIFIER = 0;
    private static final float VOLUME = 1.0F;
    private static final float QUIET = 0.75F;
    private static final float SOFT = 0.8F;
    private static final float HALF_VOLUME = 0.5F;
    private static final float PITCH = 1.0F;
    private static final float NO_ROLL = 0.0F;
    private static final float DIVERGENCE = 1.0F;
    private static final float NO_DIVERGENCE = 0.0F;
    private static final float NO_ENCHANT_BASE = 0.0F;
    private static final float NO_SCALE = 1.0F;
    private static final int LIGHT_WEAR = 1;
    private static final int HEAVY_WEAR = 3;
    private static final double CHUNGUS_DROP = 0.4D;
    private static final float LOOK_DELTA = 1.0F;
    private static final int WABBAJACK_REST = 1;
    private static final float DRAGON_PITCH_BASE = 2.0F;
    private static final float DRAGON_PITCH_SPREAD = 0.4F;
    private static final float DRAGON_PITCH_FLOOR = 0.8F;
    private static final float CLOUD_BASE = 2.0F;
    private static final float FIREBALL_POWER_BASE = 1.0F;
    private static final float FIREBALL_POWER_SPREAD = 10.0F;
    private static final int FIREBALL_MIN_AGE = 10;
    private static final int FIREBALL_MAX_AGE = 100;
    private static final int FIREBALL_AGE_PER_LUCK = 20;
    private static final int FIREBALL_MIN_SPEED = 25;
    private static final int FIREBALL_MAX_SPEED = 300;
    private static final int FIREBALL_SPEED_PER_LUCK = 20;
    private static final float PERCENT = 100.0F;
    private static final int RESET_LUCK = 10;
    private static final double MUZZLE = 1.0D;
    private static final double MUZZLE_DROP = 0.4D;
    private static final int SKULL_COUNTER_START = 1;
    private static final int SKULL_CRITICAL_COUNT = 3;
    private static final float SKULL_SPEED = 3.0F;
    private static final int SOUL_COST = -1;
    private static final int SKULL_REST = 10;
    private static final double STORM_RISE = 4.5D;
    private static final int STORM_ARROW_AGE = 40;
    private static final float STORM_SPEED = 1.0F;
    private static final double SMASH_REACH = 3.0D;
    private static final double SMASH_LIFT = 0.25D;
    private static final float FULL_CIRCLE = 360.0F;
    private static final int WARMUP_BASE = 2;
    private static final int WARMUP_PER_RING = 8;
    private static final double ARROW_RISE = 1.5D;
    private static final float ARROW_PITCH_SPREAD = 0.4F;
    private static final float ARROW_PITCH_FLOOR = 1.2F;
    private static final float ARROW_PITCH_LIFT = 0.5F;
    private static final double EYE_HEIGHT = 1.0D;
    private static final int SKYFALL_PARTICLES = 250;
    private static final float SKYFALL_SCALE = 0.5F;
    private static final float HALF = 0.5F;
    private static final int STORMVEIL_TICKS = 20;
    private static final int SPHERE_PARTICLES = 20;
    private static final float SPHERE_SCALE = 0.3F;
    private static final double RIPPLE_SEARCH = 3.0D;
    private static final Vec2f RIPPLE_RADII = new Vec2f(1.5F, 1.75F);
    private static final float RIPPLE_YAW = 90.0F;
    private static final double DRAGON_RISE = 1.0D;
    private static final int TRESPASS_PARTICLES = 150;
    private static final float OUTBURST_SCALE = 1.0F;
    private static final String BLOODLUST_SOURCE = "Bloodlust (self)";
    private static final double SPEAR_HEIGHT = 5.0D;
    private static final double SPEAR_DROP = -3.0D;
    private static final int SPEAR_PARTICLES = 10;
    private static final Vec3d SPEAR_SPREAD = new Vec3d(4.0D, 4.0D, 4.0D);
    private static final float SPEAR_PARTICLE_SPEED = 0.5F;
    private static final int SMALL_MOONLIGHT_AGE = 15;
    private static final int GATLING_SPIN_UP = 15;
    private static final float NO_HEAL = 0.0F;
    private static final int SMALL_MOONLIGHT_POINTS = 30;
    private static final byte SMALL_MOONLIGHT_SIZE = 1;
    private static final int NO_HERALD = 0;
    private static final int HERALD_LEVEL = 1;

    private LivingActives() {
    }

    public static boolean has(ItemStack stack) {
        if (!(stack.getItem() instanceof IHasAbilities abilities) || abilities.isDisabled(stack)) {
            return false;
        }
        return abilities.getAbilities().stream().anyMatch(LivingActives::supported);
    }

    private static boolean supported(IAbility ability) {
        if (ability.isOffhandAbility() || ability instanceof IChargeUsageTicks) {
            return false;
        }
        return ability instanceof SummonDraupnirSpears || ability instanceof Cleanse || ability instanceof Stormveil || ability instanceof ShootChungusHeads || ability instanceof ShootRandomProjectile
                || ability instanceof ShootSilverBullet || ability instanceof WitherSoulRelease || ability instanceof UmbralTrespass
                || ability instanceof ArrowStorm || ability instanceof BloodlustAbility || ability instanceof CircleLightningCall
                || ability instanceof Cloudburst || ability instanceof ExplodeSavedEntities || ability instanceof LifeLeach || ability instanceof LightningCall
                || ability instanceof LunarHerald || ability instanceof NightsEdgeAbility || ability instanceof ShootDragonProjectile
                || ability instanceof Unbreakable || ability instanceof VeilOfFireAbility || ability instanceof BasicKeybindAbility;
    }

    public static boolean attackClicks(ItemStack stack) {
        if (!(stack.getItem() instanceof IHasAbilities abilities) || abilities.isDisabled(stack)) {
            return false;
        }
        return abilities.getAbilities().stream().anyMatch(ShootSmallMoonlight.class::isInstance);
    }

    public static boolean attackClick(ServerWorld world, LivingEntity user, ItemStack stack) {
        if (!(stack.getItem() instanceof IHasAbilities abilities) || abilities.isDisabled(stack)) {
            return false;
        }
        boolean fired = false;
        for (IAbility ability : abilities.getAbilities()) {
            if (ability instanceof ShootSmallMoonlight moonlight) {
                fired |= smallMoonlight(moonlight, world, user, stack);
            }
        }
        return fired;
    }

    private static boolean smallMoonlight(ShootSmallMoonlight self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        StatusEffectInstance herald = user.getStatusEffect(EffectRegistry.MOON_HERALD);
        int amplifier = herald == null ? NO_HERALD : herald.getAmplifier() + HERALD_LEVEL;
        int level = WeaponUtil.getUpgradeLevel(stack);
        boolean scales = AbilityScales.scalesSmallMoonlight(stack);
        if (scales) {
            MswCompatScaleContext.beginMoonlightShortsword(AbilityScales.smallMoonlight(user, stack));
        }
        try {
            MoonlightProjectile projectile = new MoonlightProjectile(EntityRegistry.MOONLIGHT_ENTITY_TYPE, world, user, stack);
            projectile.setAgeAndPoints(SMALL_MOONLIGHT_AGE, SMALL_MOONLIGHT_POINTS, SMALL_MOONLIGHT_SIZE);
            projectile.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, self.projectileSpeed(), NO_DIVERGENCE);
            projectile.setDamage(self.damage() + self.bonusDamagePerLvl() * level + self.bonusDamagePerEffectAmp() * amplifier);
            world.spawnEntity(projectile);
        } finally {
            if (scales) {
                MswCompatScaleContext.clearMoonlightShortsword();
            }
        }
        world.playSound(null, user.getBlockPos(), SoundRegistry.MOONLIGHT_SMALL_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
        user.swingHand(Hand.MAIN_HAND, true);
        cool(user, stack, herald == null ? (int) Math.max(self.minCooldown(), self.cooldown() - self.reducedCooldownPerLvl() * level) : self.cooldownWithEffect());
        return true;
    }

    public static boolean use(ServerWorld world, LivingEntity user, ItemStack stack) {
        if (!(stack.getItem() instanceof IHasAbilities abilities) || abilities.isDisabled(stack)) {
            return false;
        }
        List<IAbility> options = new ArrayList<>(abilities.getAbilities().stream().filter(LivingActives::supported).toList());
        while (!options.isEmpty()) {
            IAbility ability = options.remove(user.getRandom().nextInt(options.size()));
            if (fire(ability, world, user, stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean fire(IAbility ability, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (ability instanceof Cleanse) {
            return cleanse(world, user, stack);
        } else if (ability instanceof Stormveil stormveil) {
            return stormveil(stormveil, world, user, stack);
        } else if (ability instanceof ShootChungusHeads heads) {
            return chungusHeads(heads, world, user, stack);
        } else if (ability instanceof ShootRandomProjectile random) {
            return wabbajack(random, world, user, stack);
        } else if (ability instanceof ShootSilverBullet gun) {
            return silverBullet(gun, world, user, stack);
        } else if (ability instanceof WitherSoulRelease release) {
            return witherRelease(release, world, user, stack);
        } else if (ability instanceof UmbralTrespass trespass) {
            return trespass(trespass, world, user, stack);
        } else if (ability instanceof ArrowStorm storm) {
            return arrowStorm(storm, world, user, stack);
        } else if (ability instanceof BloodlustAbility bloodlust) {
            return bloodlust(bloodlust, world, user, stack);
        } else if (ability instanceof CircleLightningCall circle) {
            return circleLightning(circle, world, user, stack);
        } else if (ability instanceof Cloudburst cloudburst) {
            return cloudburst(cloudburst, world, user, stack);
        } else if (ability instanceof ExplodeSavedEntities explode) {
            return explodeSaved(explode, world, user, stack);
        } else if (ability instanceof LifeLeach leach) {
            return lifeLeach(leach, world, user, stack);
        } else if (ability instanceof LightningCall call) {
            return lightningCall(call, world, user, stack);
        } else if (ability instanceof LunarHerald herald) {
            return lunarHerald(herald, world, user, stack);
        } else if (ability instanceof NightsEdgeAbility edge) {
            return nightsEdge(edge, world, user, stack);
        } else if (ability instanceof ShootDragonProjectile dragon) {
            return dragonProjectile(dragon, world, user, stack);
        } else if (ability instanceof Unbreakable unbreakable) {
            return unbreakable(unbreakable, world, user, stack);
        } else if (ability instanceof VeilOfFireAbility veil) {
            return veilOfFire(veil, world, user, stack);
        } else if (ability instanceof BasicKeybindAbility basic) {
            return basicKeybind(basic, user, stack);
        } else if (ability instanceof SummonDraupnirSpears spears) {
            return draupnirSpears(spears, world, user, stack);
        }
        return false;
    }

    private static boolean coolingDown(LivingEntity user, ItemStack stack) {
        return LivingCooldowns.coolingDown(user, stack.getItem());
    }

    private static void cool(LivingEntity user, ItemStack stack, int ticks) {
        LivingCooldowns.cool(user, stack.getItem(), ticks);
    }

    private static boolean onCooldownEffect(LivingEntity user) {
        return user.hasStatusEffect(EffectRegistry.COOLDOWN);
    }

    private static int scaled(ItemStack stack, int minimum, int cooldown, int perLevel) {
        return Math.max(minimum, cooldown - perLevel * WeaponUtil.getUpgradeLevel(stack));
    }

    private static LivingEntity foe(LivingEntity user) {
        return user instanceof MobEntity mob && mob.getTarget() != null ? mob.getTarget() : user.getAttacking();
    }

    private static boolean cleanse(ServerWorld world, LivingEntity user, ItemStack stack) {
        List<RegistryEntry<StatusEffect>> harmful = new ArrayList<>();
        for (StatusEffectInstance instance : user.getStatusEffects()) {
            if (instance.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) {
                harmful.add(instance.getEffectType());
            }
        }
        if (harmful.isEmpty()) {
            return false;
        }
        harmful.forEach(user::removeStatusEffect);
        world.playSound(null, user.getBlockPos(), SoundRegistry.RESTORE_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
        stack.damage(LIGHT_WEAR, user, HAND);
        return true;
    }

    private static boolean stormveil(Stormveil self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (onCooldownEffect(user)) {
            return false;
        }
        if (user.hasStatusEffect(EffectRegistry.STORMVEIL)) {
            Boolean empowered = stack.get(ComponentRegistry.STORMVEIL_SURGE_EMPOWERED);
            stack.set(ComponentRegistry.STORMVEIL_SURGE_EMPOWERED, true);
            return empowered != null && !empowered;
        }
        stack.set(ComponentRegistry.STORMVEIL_SURGE_EMPOWERED, false);
        stack.damage(LIGHT_WEAR, user, HAND);
        int amplifier = MathHelper.ceil(self.stormveilEffectBaseAmp() + WeaponUtil.getUpgradeLevel(stack) * self.bonusAmpPerLevelCeiled())
                + (ItemMatcher.isTonitrus(stack) ? AbilityScales.tonitrusAmpBonus(user) : NO_AMPLIFIER);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.STORMVEIL, self.stormveilEffectDuration(), amplifier));
        world.playSound(null, user.getBlockPos(), SoundRegistry.STORMVEIL_TRIGGER, SoundCategory.PLAYERS, VOLUME, PITCH);
        return true;
    }

    private static boolean chungusHeads(ShootChungusHeads self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack) || !(EntityRegistry.CHUNGUS_HEAD.create(world) instanceof TntEntity head)) {
            return false;
        }
        head.setPos(user.getX(), user.getEyeY() - CHUNGUS_DROP, user.getZ());
        head.noClip = true;
        Vec3d look = user.getRotationVec(LOOK_DELTA);
        head.setVelocity(look.x * self.speed(), look.y * self.speed(), look.z * self.speed());
        head.setPitch(user.getPitch());
        head.setYaw(user.getYaw());
        head.setFuse(self.ticksBeforeExplode());
        head.setNoGravity(true);
        world.spawnEntity(head);
        cool(user, stack, self.cooldown());
        stack.damage(HEAVY_WEAR, user, HAND);
        return true;
    }

    private static boolean wabbajack(ShootRandomProjectile self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        cool(user, stack, WABBAJACK_REST);
        Random random = user.getRandom();
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.NEUTRAL, HALF_VOLUME,
                DRAGON_PITCH_BASE / (world.getRandom().nextFloat() * DRAGON_PITCH_SPREAD + DRAGON_PITCH_FLOOR));
        EntityType<?> type = ((WabbajackAccess) (Object) self).mswcompat$pickProjectile(user);
        Entity entity = type.create(world);
        int luck = (int) (WeaponUtil.getLuckFactor(user) + self.baseLuckFactor() + WeaponUtil.getUpgradeLevel(stack) * self.bonusLuckFactorPerLvl());
        if (entity instanceof ProjectileEntity projectile) {
            projectile.setOwner(user);
            if (entity instanceof DragonStaffProjectile cloud) {
                cloud.setCloudRadius(CLOUD_BASE + random.nextFloat() * luck);
            }
            projectile.setPos(user.getX(), user.getEyeY(), user.getZ());
            if (entity instanceof GrowingFireball ball) {
                float power = FIREBALL_POWER_BASE + random.nextFloat() * FIREBALL_POWER_SPREAD * luck;
                int duration = random.nextBetween(FIREBALL_MIN_AGE, FIREBALL_MAX_AGE + FIREBALL_AGE_PER_LUCK * luck);
                float speed = random.nextBetween(FIREBALL_MIN_SPEED, FIREBALL_MAX_SPEED + FIREBALL_SPEED_PER_LUCK * luck) / PERCENT;
                ball.setMaxAge(duration);
                ball.setRadiusGrowth(power / duration);
                ball.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, speed, NO_DIVERGENCE);
            } else {
                projectile.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, self.projectileSpeed(), NO_DIVERGENCE);
            }
            if (projectile instanceof WitheredWabbajackProjectile withered) {
                withered.setCollisionAttributes(self.collisionAttributes());
                withered.setEntityHitAttributes(self.entityHitAttributes());
            }
            world.spawnEntity(entity);
            stack.damage(LIGHT_WEAR, user, HAND);
        }
        for (LuckChosenObject<EntityType<?>> option : WabbajackAccess.mswcompat$projectiles()) {
            option.setLuckFactor(RESET_LUCK);
        }
        return true;
    }

    private static boolean silverBullet(ShootSilverBullet self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        silverShots(self, world, user, stack);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
        cool(user, stack, self.getCooldown(stack));
        if (self instanceof ShootCannonball cannon) {
            WeaponUtil.launchTarget(user, ((CannonballAccess) (Object) cannon).mswcompat$launchPower(), true);
        }
        return true;
    }

    public static void gatlingTick(GatlingSilverBullets self, ServerWorld world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 0 || remainingUseTicks >= self.getMaxUseTime(stack, user) - GATLING_SPIN_UP
                || remainingUseTicks % ((GatlingAccess) (Object) self).mswcompat$updateTick() != 0) {
            return;
        }
        silverShots(self, world, user, stack);
        world.playSound(null, user.getBlockPos(), SoundRegistry.GATLING_GUN_BARRAGE_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
    }

    private static void silverShots(ShootSilverBullet self, ServerWorld world, LivingEntity user, ItemStack stack) {
        int count = (int) (self.projectileCount + self.projectileCountPerLvl * WeaponUtil.getUpgradeLevel(stack));
        for (int shot = 0; shot < count; shot++) {
            world.spawnEntity(self.createSilverBulletEntity(world, user, stack));
        }
        Vec3d look = user.getRotationVector();
        Vec3d muzzle = user.getEyePos().add(look.multiply(MUZZLE)).subtract(0.0D, MUZZLE_DROP, 0.0D);
        Random random = user.getRandom();
        for (int particle = 0; particle < self.particleAmount; particle++) {
            world.spawnParticles(particle % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 0,
                    look.x + random.nextGaussian() * self.particleSpread, look.y + random.nextGaussian() * self.particleSpread,
                    look.z + random.nextGaussian() * self.particleSpread, VOLUME);
        }
        stack.damage(self.stackDamage, user, HAND);
    }

    private static boolean witherRelease(WitherSoulRelease self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        WitherReleaseAccess access = (WitherReleaseAccess) (Object) self;
        UUID previous = stack.get(ComponentRegistry.SAVED_UUID);
        if (previous != null && world.getEntity(previous) instanceof WitherSkullEntity skull) {
            world.createExplosion(skull, skull.getX(), skull.getY(), skull.getZ(), skull.isCharged() ? access.mswcompat$criticalPower() : access.mswcompat$basePower(),
                    false, World.ExplosionSourceType.MOB);
            skull.discard();
        }
        if (stack.get(ComponentRegistry.SOULS_HARVESTED) == null || self.getSouls(stack) <= 0) {
            return false;
        }
        WitherSkullEntity skull = new WitherSkullEntity(EntityType.WITHER_SKULL, world);
        skull.setPos(user.getX(), user.getEyeY(), user.getZ());
        skull.setOwner(user);
        Integer counter = stack.get(ComponentRegistry.WITHER_SOUL_RELEASE_COUNTER);
        int count = counter == null ? SKULL_COUNTER_START : counter;
        if (count >= SKULL_CRITICAL_COUNT) {
            skull.setCharged(true);
            stack.set(ComponentRegistry.WITHER_SOUL_RELEASE_COUNTER, SKULL_COUNTER_START);
        } else {
            stack.set(ComponentRegistry.WITHER_SOUL_RELEASE_COUNTER, count + 1);
        }
        skull.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, SKULL_SPEED, DIVERGENCE);
        world.spawnEntity(skull);
        stack.set(ComponentRegistry.SAVED_UUID, skull.getUuid());
        self.addAmount(stack, SOUL_COST);
        cool(user, stack, SKULL_REST);
        stack.damage(LIGHT_WEAR, user, HAND);
        return true;
    }

    public static void trespassStrike(LivingEntity user, LivingEntity target) {
        float damage = UmbralTrespassData.getAbilityDamage(user);
        float heal = UmbralTrespassData.getHealModifier(user);
        if (heal > NO_HEAL) {
            damage += (float) (target.getMaxHealth() * UmbralTrespassData.getMaxHealthDamageBonus(user));
            user.heal(damage * heal);
        }
        user.removeStatusEffect(StatusEffects.INVISIBILITY);
        user.removeStatusEffect(EffectRegistry.GHOSTLY);
        target.damage(user.getWorld().getDamageSources().mobAttack(user), damage);
        UmbralTrespassData.setShouldDamageRiding(user, false);
        user.getWorld().playSound(null, user.getBlockPos(), SoundRegistry.SLICE_TARGET_EVENT, SoundCategory.PLAYERS, SOFT, PITCH);
        ParticleHandler.particleOutburstMap(user.getWorld(), TRESPASS_PARTICLES, user.getX(), user.getEyeY(), user.getZ(), ParticleEvents.SOUL_FLAME_SMALL_OUTBURST_MAP,
                OUTBURST_SCALE);
    }

    private static boolean trespass(UmbralTrespass self, ServerWorld world, LivingEntity user, ItemStack stack) {
        LivingEntity target = foe(user);
        if (onCooldownEffect(user) || target == null || user.squaredDistanceTo(target) >= self.maxRange() || !user.startRiding(target, true)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        if (!UmbralTrespassData.shouldDamageRiding(user)) {
            UmbralTrespassData.setShouldDamageRiding(user, true);
            float damage = self.baseDamage() + level * self.bonusDamagePerLvl()
                    + self.bonusEnchantDamageModifier() * EnchantmentHelper.getDamage(world, stack, target, world.getDamageSources().mobAttack(user), NO_ENCHANT_BASE);
            UmbralTrespassData.setOtherStats(user, damage * AbilityScales.umbralTrespass(stack, user), scaled(stack, self.minCooldown(), self.cooldown(),
                    self.reducedCooldownPerLvl()), self.healMod(), self.maxHealthBonusDamage());
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, self.ticksBeforeDismount(), NO_AMPLIFIER));
            user.addStatusEffect(new StatusEffectInstance(EffectRegistry.GHOSTLY, self.ticksBeforeDismount(), NO_AMPLIFIER));
        }
        world.playSound(null, user.getBlockPos(), SoundRegistry.UMBRAL_TRESPASS_EVENT, SoundCategory.PLAYERS, SOFT, PITCH);
        ParticleHandler.particleOutburstMap(world, TRESPASS_PARTICLES, user.getX(), user.getEyeY(), user.getZ(), ParticleEvents.SOUL_FLAME_SMALL_OUTBURST_MAP,
                OUTBURST_SCALE);
        return true;
    }

    private static boolean arrowStorm(ArrowStorm self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        ArrowStormEntity storm = new ArrowStormEntity(EntityRegistry.ARROW_STORM_ENTITY, world);
        storm.setPos(user.getX(), user.getY() + STORM_RISE, user.getZ());
        storm.setVelocity(user, NO_ROLL, user.getYaw(), NO_ROLL, STORM_SPEED, DIVERGENCE);
        storm.setOwner(user);
        storm.setDamage(self.projectileDamage() + self.bonusDamagePerLvl() * level);
        storm.setMaxArrowAge(STORM_ARROW_AGE);
        world.spawnEntity(storm);
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        stack.damage(HEAVY_WEAR, user, HAND);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.PLAYERS, VOLUME, PITCH);
        return true;
    }

    private static boolean bloodlust(BloodlustAbility self, ServerWorld world, LivingEntity user, ItemStack stack) {
        boolean scaling = ItemMatcher.isBloodlust(stack);
        float factor = scaling ? AbilityScales.bloodlust(user) : NO_SCALE;
        user.damage(DamageSourceRegistry.create(world, DamageSourceRegistry.BLEED),
                scaling ? AbilityScales.bloodlustSelfDamage(user, self.selfDamage(), factor) : self.selfDamage());
        int bleed = scaling ? (int) (self.selfBleed() * factor) : self.selfBleed();
        MswCompatBleedContext.begin(BLOODLUST_SOURCE, user, user);
        if (scaling) {
            MswCompatBleedContext.setBuildup(self.selfBleed(), bleed, factor);
        }
        BleedData.addBleed(user, bleed);
        stack.damage(LIGHT_WEAR, user, HAND);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.BLOODTHIRSTY, self.bloodthirstyDuration(), self.bloodthirstyAmp()));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, self.strengthDuration(), self.strengthAmp()));
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, SoundCategory.PLAYERS, QUIET, PITCH);
        return true;
    }

    private static boolean circleLightning(CircleLightningCall self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        DamageSource attack = world.getDamageSources().mobAttack(user);
        float damage = self.baseSmashDamage() + self.bonusSmashDamagePerLvl() * level;
        for (Entity entity : world.getOtherEntities(user, user.getBoundingBox().expand(SMASH_REACH))) {
            if (entity instanceof LivingEntity living) {
                entity.damage(attack, damage + self.enchantBonusSmashDamageMultiplier() * EnchantmentHelper.getDamage(world, stack, living, attack, NO_ENCHANT_BASE));
                entity.addVelocity(0.0D, SMASH_LIFT, 0.0D);
            }
        }
        world.playSoundFromEntity(null, user, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, QUIET, PITCH);
        float step = FULL_CIRCLE / self.lightningPerCircle();
        for (int ring = 1; ring < self.lightningCircleAmount() + 1; ring++) {
            float radius = self.rangeBetweenCircles() * ring;
            for (float theta = 0.0F; theta < FULL_CIRCLE; theta += step) {
                WarmupLightningEntity bolt = new WarmupLightningEntity(EntityRegistry.WARMUP_LIGHTNING, world);
                bolt.setPos(user.getX() + radius * Math.cos(Math.toRadians(theta)), user.getY(), user.getZ() + radius * Math.sin(Math.toRadians(theta)));
                bolt.setWarmup(WARMUP_BASE + ring * WARMUP_PER_RING);
                bolt.setOwner(user);
                world.spawnEntity(bolt);
            }
        }
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        stack.damage(HEAVY_WEAR, user, HAND);
        return true;
    }

    private static boolean cloudburst(Cloudburst self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (onCooldownEffect(user)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.COOLDOWN, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()),
                NO_AMPLIFIER));
        float velocity = self.velocity() + self.velocityPerLvl() * level;
        double damage = (self.damage() + self.damagePerLvl() * level) / velocity * (ItemMatcher.isGaleforce(stack) ? AbilityScales.galeforce(user) : NO_SCALE);
        ChargedArrow arrow = new ChargedArrow(world, user, new ItemStack(Items.ARROW), stack, true);
        arrow.setPos(user.getX(), user.getY() + ARROW_RISE, user.getZ());
        LivingEntity target = foe(user);
        if (target != null && !target.isTeammate(user) && !target.equals(user)) {
            arrow.setVelocity(target.getX() - user.getX(), target.getEyeY() - user.getBodyY(EYE_HEIGHT), target.getZ() - user.getZ(), velocity, DIVERGENCE);
        } else {
            arrow.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, velocity, DIVERGENCE);
        }
        arrow.setCritical(true);
        arrow.setDamage(damage);
        arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
        world.spawnEntity(arrow);
        stack.damage(LIGHT_WEAR, user, HAND);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, VOLUME,
                VOLUME / (world.getRandom().nextFloat() * ARROW_PITCH_SPREAD + ARROW_PITCH_FLOOR) + ARROW_PITCH_LIFT);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, (int) (self.speedDuration() + self.speedDurationPerLvl() * level),
                (int) (self.speedAmp() + self.speedAmpPerLvl() * level)));
        return true;
    }

    private static boolean explodeSaved(ExplodeSavedEntities self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        boolean scaling = ItemMatcher.isDraupnirSpear(stack);
        float damageScale = scaling ? AbilityScales.draupnir(user) : NO_SCALE;
        float explosionScale = scaling ? AbilityScales.draupnirExplosion(damageScale) : NO_SCALE;
        int level = WeaponUtil.getUpgradeLevel(stack);
        float damage = self.baseDamage() + self.bonusDamagePerLvl() * level;
        DamageSource attack = world.getDamageSources().mobAttack(user);
        for (Entity entity : world.getOtherEntities(user, user.getBoundingBox().expand(self.userExplosionRadius()))) {
            if (entity instanceof LivingEntity living) {
                entity.damage(attack, (damage + self.bonusEnchantDmgMod() * EnchantmentHelper.getDamage(world, stack, living, attack, NO_ENCHANT_BASE)) * damageScale);
                entity.addVelocity(0.0D, self.knockup(), 0.0D);
            }
        }
        ParticleHandler.particleOutburstMap(world, SKYFALL_PARTICLES, user.getX(), user.getY(), user.getZ(), ParticleEvents.DEFAULT_GRAND_SKYFALL_MAP,
                SKYFALL_SCALE);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        List<UUID> saved = stack.get(ComponentRegistry.SAVED_ENTITY_UUID_LIST);
        if (saved != null) {
            for (UUID uuid : saved) {
                Entity spear = world.getEntity(uuid);
                if (spear == null) {
                    continue;
                }
                float power = (self.spearBaseExplosionPower() + self.spearBonusExplosionPowerPerLvl() * level) * explosionScale;
                world.createExplosion(user, spear.getX(), spear.getY(), spear.getZ(), power, false, World.ExplosionSourceType.TRIGGER);
                if (power >= self.powerNeededToApplyWeakness()) {
                    for (Entity near : world.getOtherEntities(user, spear.getBoundingBox().expand(power))) {
                        if (near instanceof LivingEntity living) {
                            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, self.weaknessDuration(),
                                    (int) (self.weaknessBaseAmp() + self.bonusWeaknessAmpPerPower() * power)));
                        }
                    }
                }
                spear.discard();
            }
            stack.set(ComponentRegistry.SAVED_ENTITY_UUID_LIST, List.of());
        }
        return true;
    }

    private static boolean lifeLeach(LifeLeach self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.LIFE_LEACH, (int) (self.duration() + self.bonusDurationPerLvl() * level),
                (int) (self.amp() + self.bonusAmpPerLvl() * level), false, false));
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        world.playSound(null, user.getBlockPos(), SoundRegistry.DEMON_BOSS_IDLE_EVENT, SoundCategory.PLAYERS, QUIET, PITCH);
        return true;
    }

    private static boolean lightningCall(LightningCall self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        float radius = self.blockRadius();
        Box area = new Box(user.getX() - radius, user.getY() - radius * HALF, user.getZ() - radius, user.getX() + radius, user.getY() + radius * HALF,
                user.getZ() + radius);
        List<Entity> nearby = world.getOtherEntities(user, area);
        if (nearby.isEmpty()) {
            return false;
        }
        boolean scaling = ItemMatcher.isDragonslayerSwordspear(stack);
        stack.damage(HEAVY_WEAR, user, HAND);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.STORMVEIL, STORMVEIL_TICKS, NO_AMPLIFIER));
        int level = WeaponUtil.getUpgradeLevel(stack);
        int bolts = (int) (self.lightningAmount() + self.bonusLightningPerLvl() * level) + (scaling ? AbilityScales.dragonslayerLightning(user) : 0);
        float stomp = (self.stormStompDamage() + self.bonusStormStompDamagePerLvl() * level) * (scaling ? AbilityScales.dragonslayerStomp(user) : NO_SCALE);
        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity target) || entity instanceof TameableEntity tamed && tamed.isTamed()) {
                continue;
            }
            if (world.isSkyVisible(target.getBlockPos())) {
                for (int bolt = 0; bolt < bolts; bolt++) {
                    LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
                    lightning.setPos(target.getX(), target.getY(), target.getZ());
                    LivingLightning.mark(lightning, user);
                    world.spawnEntity(lightning);
                }
            } else {
                target.takeKnockback(self.stormStompKnockback(), user.getX() - target.getX(), user.getZ() - target.getZ());
                target.damage(world.getDamageSources().mobAttack(user), stomp);
                ParticleHandler.particleSphereList(world, SPHERE_PARTICLES, target.getX(), target.getY(), target.getZ(), ParticleEvents.DARK_EXPLOSION_LIST,
                        SPHERE_SCALE);
            }
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, VOLUME, PITCH);
        }
        float rain = world.isRaining() ? self.rainingCooldownMod() : NO_SCALE;
        cool(user, stack, (int) Math.max(self.minCooldown(), (self.cooldown() - level * self.reducedCooldownPerLvl()) * rain));
        return true;
    }

    private static boolean lunarHerald(LunarHerald self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (user.hasStatusEffect(EffectRegistry.MOON_HERALD)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.MOON_HERALD, (int) (self.duration() + self.bonusDurationPerLvl() * level),
                (int) (self.amp() + self.bonusAmpPerLvl() * level)));
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.PLAYERS, VOLUME, PITCH);
        stack.damage(LIGHT_WEAR, user, HAND);
        return true;
    }

    private static boolean nightsEdge(NightsEdgeAbility self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        int level = WeaponUtil.getUpgradeLevel(stack);
        int ripples = (int) (self.rippleAmount() + self.bonusRipplesPerLvl() * level);
        float damage = (self.rippleDamage() + self.rippleBonusDmgPerLvl() * level) * (ItemMatcher.isNightsEdge(stack) ? AbilityScales.nightsEdge(user) : NO_SCALE);
        WeaponUtil.doConsumerOnCircle(world, user.getYaw() + RIPPLE_YAW, user.getPos(), RIPPLE_SEARCH, ripples, RIPPLE_RADII,
                (position, warmup, yaw) -> self.spawnNightsEdge(world, user, position, warmup, yaw, damage));
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        stack.damage(LIGHT_WEAR, user, HAND);
        return true;
    }

    private static boolean dragonProjectile(ShootDragonProjectile self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        int level = WeaponUtil.getUpgradeLevel(stack);
        DragonStaffProjectile fireball = new DragonStaffProjectile(world, user);
        fireball.setProjectileMaxAge(self.projectileMaxAge());
        fireball.setCloudRadius(self.cloudBaseRadius() + self.cloudBonusRadiusPerLvl() * level);
        fireball.setCloudDuration(self.cloudDuration() + self.cloudBonusDurationPerLvl() * level);
        fireball.setCloudRadiusGrowth(self.cloudRadiusGrowth() + self.cloudBonusRadiusGrowthPerLvl() * level);
        fireball.setEffectDuration(self.effectDuration());
        fireball.setEffectAmp((int) (self.effectAmp() + (ItemMatcher.isDragonStaff(stack) ? AbilityScales.dragonStaffAura(user) : 0.0F)));
        fireball.setPos(user.getX(), user.getY() + DRAGON_RISE, user.getZ());
        fireball.setVelocity(user, user.getPitch(), user.getYaw(), NO_ROLL, self.projectileSpeed(), NO_DIVERGENCE);
        world.spawnEntity(fireball);
        stack.damage(LIGHT_WEAR, user, HAND);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.NEUTRAL, HALF_VOLUME,
                DRAGON_PITCH_BASE / (world.getRandom().nextFloat() * DRAGON_PITCH_SPREAD + DRAGON_PITCH_FLOOR));
        return true;
    }

    private static boolean unbreakable(Unbreakable self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        cool(user, stack, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()));
        stack.damage(HEAVY_WEAR, user, HAND);
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, self.effectsDuration(), self.absorptionAmp()));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, self.effectsDuration(), self.resistanceAmp()));
        world.playSound(null, user.getBlockPos(), SoundRegistry.NIGHTFALL_SHIELD_EVENT, SoundCategory.PLAYERS, VOLUME, PITCH);
        return true;
    }

    private static boolean veilOfFire(VeilOfFireAbility self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (coolingDown(user, stack)) {
            return false;
        }
        VeilOfFireAccess access = (VeilOfFireAccess) self;
        int level = WeaponUtil.getUpgradeLevel(stack);
        self.dawnbreakerEvent(world, user, user, stack);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.VEIL_OF_FIRE, (int) (access.mswcompat$veilDuration() + access.mswcompat$veilDurationPerLevel() * level),
                (int) (access.mswcompat$veilAmplifier() + access.mswcompat$veilAmplifierPerLevel() * level)));
        cool(user, stack, scaled(stack, access.mswcompat$minCooldown(), access.mswcompat$cooldown(), access.mswcompat$cooldownPerLevel()));
        return true;
    }

    private static boolean draupnirSpears(SummonDraupnirSpears self, ServerWorld world, LivingEntity user, ItemStack stack) {
        if (onCooldownEffect(user)) {
            return false;
        }
        int step = (int) FULL_CIRCLE / self.spearAmount();
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, VOLUME, PITCH);
        for (int theta = 0; theta < FULL_CIRCLE; theta += step) {
            double radians = Math.toRadians(theta);
            double x = user.getX() + self.rangeOut() * Math.cos(radians);
            double z = user.getZ() + self.rangeOut() * Math.sin(radians);
            DraupnirSpearEntity spear = new DraupnirSpearEntity(world, user, stack.copy());
            spear.setPos(x, user.getY() + SPEAR_HEIGHT, z);
            spear.setVelocity(Math.cos(radians), SPEAR_DROP, Math.sin(radians));
            spear.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
            world.spawnEntity(spear);
            ExplodeSavedEntities.saveEntityOnItem(stack, spear);
            ParticleHandler.particleOutburst(world, SPEAR_PARTICLES, x, user.getY() + SPEAR_HEIGHT, z, ParticleTypes.CLOUD, SPEAR_SPREAD, SPEAR_PARTICLE_SPEED);
        }
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.COOLDOWN, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()),
                NO_AMPLIFIER));
        return true;
    }

    private static boolean basicKeybind(BasicKeybindAbility self, LivingEntity user, ItemStack stack) {
        if (onCooldownEffect(user)) {
            return false;
        }
        if (stack.getItem() instanceof ChungusStaff) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, (int) ConfigConstructor.chungus_staff_buff_haste_duration,
                    (int) ConfigConstructor.chungus_staff_buff_haste_amp));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, (int) ConfigConstructor.chungus_staff_buff_speed_duration,
                    (int) ConfigConstructor.chungus_staff_buff_speed_amp));
        } else if (stack.getItem() instanceof DarkMoonGreatsword) {
            user.addStatusEffect(new StatusEffectInstance(EffectRegistry.FROST_MOON, (int) ConfigConstructor.dark_moon_greatsword_frost_moon_duration,
                    (int) ConfigConstructor.dark_moon_greatsword_frost_moon_amp));
        } else if (stack.getItem() instanceof HeapOfRawIron) {
            int level = WeaponUtil.getUpgradeLevel(stack);
            MswCompatScaleContext.setKeybindActive(true);
            MswCompatScaleContext.setKeybindFactor(AbilityScales.heapOfRawIron(user));
            user.addStatusEffect(new StatusEffectInstance(EffectRegistry.BLOODTHIRSTY,
                    (int) (ConfigConstructor.heap_of_raw_iron_rage_bloodthristy_duration + ConfigConstructor.heap_of_raw_iron_rage_bloodthristy_bonus_duration_per_lvl * level),
                    (int) (ConfigConstructor.heap_of_raw_iron_rage_bloodthristy_amp + ConfigConstructor.heap_of_raw_iron_rage_bloodthristy_bonus_amp_per_lvl * level)));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH,
                    (int) (ConfigConstructor.heap_of_raw_iron_rage_strenth_duration + ConfigConstructor.heap_of_raw_iron_rage_strenth_bonus_duration_per_lvl * level),
                    (int) (ConfigConstructor.heap_of_raw_iron_rage_strenth_amp + ConfigConstructor.heap_of_raw_iron_rage_strenth_bonus_amp_per_lvl * level)));
            MswCompatScaleContext.clearKeybind();
        } else {
            return false;
        }
        stack.damage(self.stackDamage(), user, HAND);
        user.addStatusEffect(new StatusEffectInstance(EffectRegistry.COOLDOWN, scaled(stack, self.minCooldown(), self.cooldown(), self.reducedCooldownPerLvl()),
                NO_AMPLIFIER));
        return true;
    }
}
