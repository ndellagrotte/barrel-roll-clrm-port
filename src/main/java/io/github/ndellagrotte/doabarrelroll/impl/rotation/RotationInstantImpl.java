package io.github.ndellagrotte.doabarrelroll.impl.rotation;

import io.github.ndellagrotte.doabarrelroll.api.rotation.RotationInstant;
import io.github.ndellagrotte.doabarrelroll.config.Sensitivity;

import java.util.function.BooleanSupplier;

/**
 * Hand-written stand-in for a record: this targets Java 8 bytecode so it runs on MinecraftForge
 * 1.12.2 as well as Cleanroom. The accessor names ({@code pitch()}/{@code yaw()}/{@code roll()})
 * match the record shape that {@link RotationInstant} declares, so callers are unaffected.
 */
public final class RotationInstantImpl implements RotationInstant {
    private final double pitch;
    private final double yaw;
    private final double roll;

    public RotationInstantImpl(double pitch, double yaw, double roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    @Override
    public double pitch() {
        return pitch;
    }

    @Override
    public double yaw() {
        return yaw;
    }

    @Override
    public double roll() {
        return roll;
    }

    @Override
    public RotationInstant add(double pitch, double yaw, double roll) {
        return new RotationInstantImpl(this.pitch + pitch, this.yaw + yaw, this.roll + roll);
    }

    @Override
    public RotationInstant multiply(double pitch, double yaw, double roll) {
        return new RotationInstantImpl(this.pitch * pitch, this.yaw * yaw, this.roll * roll);
    }

    @Override
    public RotationInstant addAbsolute(double x, double y, double currentRoll) {
        double cos = Math.cos(currentRoll);
        double sin = Math.sin(currentRoll);
        return new RotationInstantImpl(this.pitch - y * cos - x * sin, this.yaw - y * sin + x * cos, this.roll);
    }

    @Override
    public RotationInstant applySensitivity(Sensitivity sensitivity) {
        return new RotationInstantImpl(
                pitch * sensitivity.pitch,
                yaw * sensitivity.yaw,
                roll * sensitivity.roll
        );
    }

    @Override
    public RotationInstant useModifier(ConfiguresRotation modifier, BooleanSupplier condition) {
        return condition.getAsBoolean() ? modifier.apply(this) : this;
    }

    @Override
    public RotationInstant useModifier(ConfiguresRotation modifier) {
        return useModifier(modifier, () -> true);
    }
}
