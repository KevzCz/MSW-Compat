package net.pixeldreamstudios.mswcompat.mixin.living;

import net.soulsweaponry.items.abilities.abilitykeybind.VeilOfFireAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(value = VeilOfFireAbility.class, remap = false)
public interface VeilOfFireAccess {

    @Accessor(value = "veilOfFireDuration", remap = false)
    int mswcompat$veilDuration();

    @Accessor(value = "veilOfFireBonusDurationPerLvl", remap = false)
    float mswcompat$veilDurationPerLevel();

    @Accessor(value = "veilOfFireAmp", remap = false)
    int mswcompat$veilAmplifier();

    @Accessor(value = "veilOfFireBonusAmpPerLvl", remap = false)
    float mswcompat$veilAmplifierPerLevel();

    @Accessor(value = "minCooldown", remap = false)
    int mswcompat$minCooldown();

    @Accessor(value = "cooldown", remap = false)
    int mswcompat$cooldown();

    @Accessor(value = "reducedCooldownPerLvl", remap = false)
    int mswcompat$cooldownPerLevel();
}
