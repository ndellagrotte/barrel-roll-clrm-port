package io.github.ndellagrotte.doabarrelroll.api.event;

import io.github.ndellagrotte.doabarrelroll.api.rotation.RotationInstant;
import io.github.ndellagrotte.doabarrelroll.impl.event.RollContextImpl;

import java.util.function.BooleanSupplier;

public interface RollContext {
    static RollContext of(RotationInstant currentRotation, RotationInstant rotationDelta, double delta) {
        return new RollContextImpl(currentRotation, rotationDelta, delta);
    }

    RotationInstant getCurrentRotation();

    RotationInstant getRotationDelta();

    double getRenderDelta();

    RollContext useModifier(ConfiguresRotation modifier, BooleanSupplier condition);

    RollContext useModifier(ConfiguresRotation modifier);

    @FunctionalInterface
    interface ConfiguresRotation {
        RotationInstant apply(RotationInstant rotationInstant, RollContext context);
    }
}
