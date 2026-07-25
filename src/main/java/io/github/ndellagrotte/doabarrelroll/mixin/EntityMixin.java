package io.github.ndellagrotte.doabarrelroll.mixin;

import io.github.ndellagrotte.doabarrelroll.api.RollEntity;
import io.github.ndellagrotte.doabarrelroll.config.Sensitivity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements RollEntity {
    @Override
    public void doABarrelRoll$changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta) {
    }

    @Override
    public void doABarrelRoll$changeElytraLook(float pitch, float yaw, float roll) {
    }

    @Override
    public boolean doABarrelRoll$isRolling() {
        return false;
    }

    @Override
    public void doABarrelRoll$setRolling(boolean rolling) {
    }

    @Override
    public float doABarrelRoll$getRoll() {
        return 0;
    }

    @Override
    public float doABarrelRoll$getRoll(float tickDelta) {
        return 0;
    }

    @Override
    public void doABarrelRoll$setRoll(float roll) {
    }

    @Override
    public void doABarrelRoll$tickRoll() {
    }

    @Override
    public void doABarrelRoll$tickRollState() {
    }
}
