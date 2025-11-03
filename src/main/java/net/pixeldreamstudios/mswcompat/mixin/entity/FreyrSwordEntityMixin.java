package net.pixeldreamstudios.mswcompat.mixin.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.pixeldreamstudios.mswcompat.config.MSWCompatConfig;
import net.pixeldreamstudios.mswcompat.util.PetInheritanceUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FreyrSwordEntity.class)
public abstract class FreyrSwordEntityMixin {

    @Unique
    private boolean mswcompat$attributesApplied = false;

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mswcompat$applyOwnerStatsOnce(CallbackInfo ci) {
        FreyrSwordEntity freyrSword = (FreyrSwordEntity) (Object) this;

        if (mswcompat$attributesApplied || freyrSword.getWorld().isClient) {
            return;
        }

        if (freyrSword.isTamed() && freyrSword.getOwner() instanceof PlayerEntity owner) {
            MSWCompatConfig.FreyrSwordConfig config = MSWCompatConfig.getInstance().freyr_sword;

            if (!config.useKevslibraryPetInheritanceAttribute) {
                NbtCompound previousData = new NbtCompound();
                double ratio = config.petInheritanceBonus;
                PetInheritanceUtil.apply(owner, freyrSword, previousData, ratio);
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