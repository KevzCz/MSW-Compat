package net.pixeldreamstudios.mswcompat.mixin.living;

import net.soulsweaponry.items.abilities.usagetick.GatlingSilverBullets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GatlingSilverBullets.class, remap = false)
public interface GatlingAccess {

    @Accessor(value = "updateTick", remap = false)
    int mswcompat$updateTick();
}
