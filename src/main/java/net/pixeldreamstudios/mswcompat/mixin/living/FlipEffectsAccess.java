package net.pixeldreamstudios.mswcompat.mixin.living;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.soulsweaponry.items.abilities.inventorytick.FlipEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(value = FlipEffects.class, remap = false)
public interface FlipEffectsAccess {

    @Accessor(value = "FLIPPABLE_EFFECTS", remap = false)
    static HashMap<RegistryEntry<StatusEffect>, RegistryEntry<StatusEffect>> mswcompat$flippable() {
        throw new AssertionError();
    }

    @Accessor(value = "newEffectDurationMod", remap = false)
    float mswcompat$durationMod();

    @Accessor(value = "newEffectAmpMod", remap = false)
    float mswcompat$ampMod();

    @Accessor(value = "minCooldown", remap = false)
    int mswcompat$minCooldown();

    @Accessor(value = "cooldown", remap = false)
    int mswcompat$cooldown();

    @Accessor(value = "reducedCooldownPerLvl", remap = false)
    int mswcompat$reducedCooldownPerLvl();
}
