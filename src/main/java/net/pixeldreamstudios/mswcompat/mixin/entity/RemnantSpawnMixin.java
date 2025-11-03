package net.pixeldreamstudios.mswcompat.mixin.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.pixeldreamstudios.mswcompat.config.MSWCompatConfig;
import net.pixeldreamstudios.mswcompat.util.PetInheritanceUtil;
import net.soulsweaponry.entity.mobs.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Remnant.class)
public abstract class RemnantSpawnMixin {

    @Unique
    private boolean mswcompat$attributesApplied = false;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void mswcompat$applyOwnerStatsOnce(CallbackInfo ci) {
        Remnant remnant = (Remnant) (Object) this;

        if (mswcompat$attributesApplied || remnant.getWorld().isClient) {
            return;
        }

        if (remnant.isTamed() && remnant.getOwner() instanceof PlayerEntity owner) {
            Double ratio = null;
            boolean useKevsLibrary = true;

            if (remnant instanceof Forlorn || remnant instanceof Soulmass || remnant instanceof SoulReaperGhost) {
                MSWCompatConfig.SoulReaperConfig config = MSWCompatConfig.getInstance().soul_reaper;
                useKevsLibrary = config.useKevslibraryPetInheritanceAttribute;
                if (!useKevsLibrary) {
                    ratio = config.petInheritanceBonus;
                }
            } else if (remnant instanceof FrostGiant || remnant instanceof RimeSpectre) {
                MSWCompatConfig.FrostmourneConfig config = MSWCompatConfig.getInstance().frostmourne;
                useKevsLibrary = config.useKevslibraryPetInheritanceAttribute;
                if (!useKevsLibrary) {
                    ratio = config.petInheritanceBonus;
                }
            } else if (remnant.getClass() == Remnant.class) {
                MSWCompatConfig.NightfallConfig config = MSWCompatConfig.getInstance().nightfall;
                useKevsLibrary = config.useKevslibraryPetInheritanceAttribute;
                if (!useKevsLibrary) {
                    ratio = config.petInheritanceBonus;
                }
            }

            if (ratio != null && !useKevsLibrary) {
                NbtCompound previousData = new NbtCompound();
                PetInheritanceUtil.apply(owner, remnant, previousData, ratio);
                mswcompat$attributesApplied = true;
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void mswcompat$writeAttributesApplied(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("mswcompat_attributes_applied", mswcompat$attributesApplied);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void mswcompat$readAttributesApplied(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("mswcompat_attributes_applied")) {
            mswcompat$attributesApplied = nbt.getBoolean("mswcompat_attributes_applied");
        }
    }
}