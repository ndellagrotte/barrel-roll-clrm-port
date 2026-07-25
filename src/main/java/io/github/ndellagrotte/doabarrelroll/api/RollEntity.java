package io.github.ndellagrotte.doabarrelroll.api;

import io.github.ndellagrotte.doabarrelroll.config.Sensitivity;

public interface RollEntity {
    void doABarrelRoll$changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta);

    void doABarrelRoll$changeElytraLook(float pitch, float yaw, float roll);

    boolean doABarrelRoll$isRolling();

    void doABarrelRoll$setRolling(boolean rolling);

    float doABarrelRoll$getRoll();

    float doABarrelRoll$getRoll(float tickDelta);

    void doABarrelRoll$setRoll(float roll);

    /**
     * Per-tick roll bookkeeping, invoked from the {@code EntityLivingBase.onUpdate} TAIL hook.
     *
     * <p>This and {@link #doABarrelRoll$tickRollState()} are declared on the duck interface rather
     * than as plain overridable methods on the mixin classes on purpose. Mixin inheritance
     * ({@code EntityMixin} -> {@code LivingEntityMixin} -> {@code PlayerEntityMixin} ->
     * {@code ClientPlayerEntityMixin}) does <em>not</em> reliably preserve virtual dispatch for
     * merged methods: under Cleanroom 0.6.x's CleanMix the base implementation runs even for an
     * {@code EntityPlayerSP}, so the override never fires and the mod silently does nothing.
     * Going through the interface forces INVOKEINTERFACE, which dispatches correctly on both
     * Cleanroom 0.5.x (sponge-mixin) and 0.6.x (CleanMix).
     */
    void doABarrelRoll$tickRoll();

    /** Recomputes whether this entity should currently be rolling. Only meaningful client-side. */
    void doABarrelRoll$tickRollState();
}
