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
     * than carried by an overridden {@code @Inject} handler, on purpose.
     *
     * <p>Mixin mangles injector-handler names to {@code handler$<seq>$<owner>$<name>} so handlers
     * from different mods cannot collide. sponge-mixin (Cleanroom 0.5.x) applies that mangling to
     * the parent handler <em>and</em> to a child mixin's override of it, keeping them a valid
     * override pair. CleanMix (Cleanroom 0.6.x) renames only the method carrying the annotation and
     * leaves the child's {@code @Override} under its original name, so the two become unrelated
     * methods and the injected call site always reaches the parent. Verified by reflecting over the
     * merged classes on both loaders:
     *
     * <pre>
     * 0.5.17  EntityLivingBase: handler$zza000$..$doABarrelRoll$probeHandler
     *         EntityPlayer:     handler$zza000$..$doABarrelRoll$probeHandler   -> child runs
     * 0.6.6   EntityLivingBase: handler$zzb000$..$doABarrelRoll$probeHandler
     *         EntityPlayer:     doABarrelRoll$probeHandler                     -> parent runs
     * </pre>
     *
     * <p>The failure is silent - both methods exist and nothing throws. Ordinary (non-handler)
     * method overrides across mixins are unaffected. Routing through the interface forces
     * INVOKEINTERFACE, which dispatches correctly on both loaders.
     */
    void doABarrelRoll$tickRoll();

    /** Recomputes whether this entity should currently be rolling. Only meaningful client-side. */
    void doABarrelRoll$tickRollState();
}
