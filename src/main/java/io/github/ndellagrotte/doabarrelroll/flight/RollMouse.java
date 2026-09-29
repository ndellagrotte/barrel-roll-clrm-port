package io.github.ndellagrotte.doabarrelroll.flight;

import io.github.ndellagrotte.doabarrelroll.config.ModConfig;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector2d;

/**
 * Replaces the upstream MouseMixin. Fed once per rendered frame by the loader's
 * {@code InputEvent.MouseTurnEvent} and the idle pump in {@code ClientEventHandler#onRenderTick},
 * both with the loader's frame delta.
 */
@SideOnly(Side.CLIENT)
public final class RollMouse {
    private static final Vector2d mouseTurnVec = new Vector2d();

    private RollMouse() {
    }

    /**
     * Runs the roll flight pipeline for one frame of mouse input.
     *
     * @param mouseDelta time since the previous frame, in <em>seconds</em>. This is the unit every
     *                   delta-scaled modifier is calibrated for: upstream's mouseDelta is vanilla's
     *                   GLFW-based {@code Mouse.updateMouse(double timeDelta)} argument.
     * @return whether the input was consumed, in which case vanilla's turn must be skipped
     */
    public static boolean updateMouse(EntityPlayerSP player, double cursorDeltaX, double cursorDeltaY, double mouseDelta) {
        // 1.12.2 hands us the pitch argument of Entity.turn, which is *subtracted* from
        // rotationPitch and comes from LWJGL's bottom-up Mouse.getDY. Upstream's cursorDeltaY is
        // GLFW's top-down value fed to changeLookDirection, which *adds* it. Flip once here so the
        // whole pipeline below - mouseTurnVec included - runs in upstream's convention.
        double pitchDelta = -cursorDeltaY;

        if (RollPlayer.isRolling()) {

            if (ModConfig.INSTANCE.getMomentumBasedMouse()) {

                // add the mouse movement to the current vector and normalize if needed
                mouseTurnVec.x += cursorDeltaX / 300;
                mouseTurnVec.y += pitchDelta / 300;
                if (mouseTurnVec.lengthSquared() > 1.0) {
                    mouseTurnVec.normalize();
                }
                var readyTurnVec = new Vector2d(mouseTurnVec);

                // check if the vector is within the deadzone
                double deadzone = ModConfig.INSTANCE.getMomentumMouseDeadzone();
                if (readyTurnVec.lengthSquared() < deadzone * deadzone) readyTurnVec.set(0, 0);

                // enlarge the vector and apply it to the camera
                readyTurnVec.scale(1200 * mouseDelta);
                RollPlayer.changeElytraLook(player, readyTurnVec.y, readyTurnVec.x, 0, ModConfig.INSTANCE.getDesktopSensitivity(), mouseDelta);

            } else {

                // if we are not using a momentum based mouse, we can reset it and apply the values directly
                mouseTurnVec.set(0, 0);
                RollPlayer.changeElytraLook(player, pitchDelta, cursorDeltaX, 0, ModConfig.INSTANCE.getDesktopSensitivity(), mouseDelta);
            }

            return true;
        }

        mouseTurnVec.set(0, 0);
        return false;
    }

    public static Vector2d getMouseTurnVec() {
        return mouseTurnVec;
    }
}
