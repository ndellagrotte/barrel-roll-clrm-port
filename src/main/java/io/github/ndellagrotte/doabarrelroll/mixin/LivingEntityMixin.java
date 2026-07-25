package io.github.ndellagrotte.doabarrelroll.mixin;

import io.github.ndellagrotte.doabarrelroll.api.RollEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class LivingEntityMixin extends EntityMixin {
    /**
     * This handler must stay behaviour-free: overriding an <em>injector handler</em> in a child
     * mixin does not work on Cleanroom 0.6.x. Per-subclass behaviour is reached through
     * {@link RollEntity}, cast via {@code Object} so javac emits INVOKEINTERFACE - see
     * {@link RollEntity#doABarrelRoll$tickRoll()}.
     */
    @Inject(
            method = "onUpdate",
            at = @At("TAIL")
    )
    protected void doABarrelRoll$onUpdateTail(CallbackInfo ci) {
        ((RollEntity) (Object) this).doABarrelRoll$tickRoll();
    }
}
