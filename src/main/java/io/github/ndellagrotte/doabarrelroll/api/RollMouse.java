package io.github.ndellagrotte.doabarrelroll.api;

import net.minecraft.client.entity.EntityPlayerSP;

import javax.vecmath.Vector2d;

public interface RollMouse {
    /**
     * Advances the per-frame clock and returns the elapsed time in seconds. Must be called exactly
     * once per rendered frame, before the mouse look for that frame is applied.
     */
    double doABarrelRoll$beginFrame();

    boolean doABarrelRoll$updateMouse(EntityPlayerSP player, double cursorDeltaX, double cursorDeltaY, double mouseDelta);

    Vector2d doABarrelRoll$getMouseTurnVec();
}
