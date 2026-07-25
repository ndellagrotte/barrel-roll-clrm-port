package io.github.ndellagrotte.doabarrelroll.mixin;

import io.github.ndellagrotte.doabarrelroll.DoABarrelRoll;
import io.github.ndellagrotte.doabarrelroll.api.RollEntity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityPlayer.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {
    @Unique
    protected boolean isRolling;
    @Unique
    protected float prevRoll;
    @Unique
    protected float roll;

    @Override
    public void doABarrelRoll$tickRoll() {
        ((RollEntity) (Object) this).doABarrelRoll$tickRollState();

        prevRoll = doABarrelRoll$getRoll();

        if (!doABarrelRoll$isRolling()) {
            doABarrelRoll$setRoll(0.0f);
        }
    }

    @Override
    public boolean doABarrelRoll$isRolling() {
        return isRolling;
    }

    @Override
    public void doABarrelRoll$setRolling(boolean rolling) {
        isRolling = rolling;
    }

    @Override
    public float doABarrelRoll$getRoll() {
        return roll;
    }

    @Override
    public float doABarrelRoll$getRoll(float tickDelta) {
        if (tickDelta == 1.0f) {
            return doABarrelRoll$getRoll();
        }
        return prevRoll + (doABarrelRoll$getRoll() - prevRoll) * tickDelta;
    }

    @Override
    public void doABarrelRoll$setRoll(float roll) {
        if (!Float.isFinite(roll)) {
            DoABarrelRoll.LOGGER.warn("Invalid entity rotation: {}, discarding.", roll);
            return;
        }
        var lastRoll = doABarrelRoll$getRoll();
        this.roll = roll;

        if (roll < -90 && lastRoll > 90) {
            prevRoll -= 360;
        } else if (roll > 90 && lastRoll < -90) {
            prevRoll += 360;
        }
    }
}
