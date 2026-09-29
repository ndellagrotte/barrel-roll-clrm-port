package io.github.ndellagrotte.doabarrelroll.util;

import io.github.ndellagrotte.doabarrelroll.config.ActivationBehaviour;
import io.github.ndellagrotte.doabarrelroll.config.ModConfig;

/**
 * Jump tracking for the TRIPLE_JUMP and HYBRID activation behaviours. Client-side only.
 */
public class ElytraActivation {
    /**
     * Used for both TRIPLE_JUMP and HYBRID activation behaviours.
     */
    public static boolean secondJump = false;
    public static boolean thirdJump = false;
    public static boolean wasJumping = false;

    /**
     * Decides whether the client should cancel an elytra deploy attempt. Called from
     * {@code LivingElytraDeployEvent}, which vanilla only reaches on a fresh jump press while
     * airborne, so {@code !wasJumping} marks a new press.
     */
    public static boolean shouldSuppressDeploy() {
        var behaviour = ModConfig.INSTANCE.getActivationBehaviour();
        if (behaviour == ActivationBehaviour.VANILLA) {
            return false;
        }

        var shouldCancel = behaviour == ActivationBehaviour.TRIPLE_JUMP;
        if (!wasJumping) {
            wasJumping = true;
            if (!secondJump) {
                secondJump = true;
                return shouldCancel;
            } else {
                // Set thirdJump to true in HYBRID mode, but toggle it in HYBRID_TOGGLE mode.
                thirdJump = behaviour != ActivationBehaviour.HYBRID_TOGGLE || !thirdJump;
            }
            return false;
        }
        return shouldCancel;
    }
}
