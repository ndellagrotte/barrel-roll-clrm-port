package io.github.ndellagrotte.doabarrelroll.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class LivingEntityMixin extends EntityMixin {
    @Inject(
            method = "onUpdate",
            at = @At("TAIL")
    )
    protected void doABarrelRoll$onUpdateTail(CallbackInfo ci) {
    }
}
