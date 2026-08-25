package io.github.ndellagrotte.doabarrelroll.mixin;

import io.github.ndellagrotte.doabarrelroll.api.RollEntity;
import io.github.ndellagrotte.doabarrelroll.api.RollMouse;
import io.github.ndellagrotte.doabarrelroll.config.ModConfig;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.vecmath.Vector2d;

/**
 * Replaces the upstream MouseMixin: in 1.12.2 the mouse look is applied by
 * {@code EntityRenderer.updateCameraAndRender} calling {@code EntityPlayerSP.turn}.
 * Redirecting that call lets us substitute the roll flight pipeline while gliding.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin implements RollMouse {
    @Unique
    private final Vector2d mouseTurnVec = new Vector2d();
    @Unique
    private long lastFrameNanos = -1;
    @Unique
    private double doABarrelRoll$frameDelta = 1.0 / 60.0;

    @Redirect(
            method = "updateCameraAndRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"
            )
    )
    private void doABarrelRoll$redirectPlayerTurn(EntityPlayerSP player, float yaw, float pitch) {
        // Reuses the delta computed by beginFrame() earlier this frame rather than advancing the
        // clock here, so the idle pump and the mouse input for a frame share one time slice -
        // exactly how upstream's Mouse.tick hands the same GLFW delta to both of its calls.
        if (!doABarrelRoll$updateMouse(player, yaw, pitch, doABarrelRoll$frameDelta)) {
            player.turn(yaw, pitch);
        }
    }

    /**
     * Time since the previous frame, in <em>seconds</em>. This is the unit every delta-scaled
     * modifier is calibrated for: upstream's mouseDelta is vanilla's GLFW-based
     * {@code Mouse.updateMouse(double timeDelta)} argument, which is a {@code glfwGetTime}
     * difference. Driven once per frame from {@code ClientEventHandler#onRenderTick}; the clamp
     * covers hitches and the stall across GUI/pause screens.
     */
    @Override
    public double doABarrelRoll$beginFrame() {
        long now = System.nanoTime();
        double delta = lastFrameNanos < 0 ? 1.0 / 60.0 : (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;
        doABarrelRoll$frameDelta = Math.max(0.0, Math.min(delta, 0.1));
        return doABarrelRoll$frameDelta;
    }

    @Override
    public boolean doABarrelRoll$updateMouse(EntityPlayerSP player, double cursorDeltaX, double cursorDeltaY, double mouseDelta) {
        RollEntity rollPlayer = (RollEntity) player;

        // 1.12.2 hands us the pitch argument of Entity.turn, which is *subtracted* from
        // rotationPitch and comes from LWJGL's bottom-up Mouse.getDY. Upstream's cursorDeltaY is
        // GLFW's top-down value fed to changeLookDirection, which *adds* it. Flip once here so the
        // whole pipeline below - mouseTurnVec included - runs in upstream's convention.
        double pitchDelta = -cursorDeltaY;

        if (rollPlayer.doABarrelRoll$isRolling()) {

            if (ModConfig.INSTANCE.getMomentumBasedMouse()) {

                // add the mouse movement to the current vector and normalize if needed
                mouseTurnVec.x += cursorDeltaX / 300;
                mouseTurnVec.y += pitchDelta / 300;
                if (mouseTurnVec.lengthSquared() > 1.0) {
                    mouseTurnVec.normalize();
                }
                Vector2d readyTurnVec = new Vector2d(mouseTurnVec);

                // check if the vector is within the deadzone
                double deadzone = ModConfig.INSTANCE.getMomentumMouseDeadzone();
                if (readyTurnVec.lengthSquared() < deadzone * deadzone) readyTurnVec.set(0, 0);

                // enlarge the vector and apply it to the camera
                readyTurnVec.scale(1200 * mouseDelta);
                rollPlayer.doABarrelRoll$changeElytraLook(readyTurnVec.y, readyTurnVec.x, 0, ModConfig.INSTANCE.getDesktopSensitivity(), mouseDelta);

            } else {

                // if we are not using a momentum based mouse, we can reset it and apply the values directly
                mouseTurnVec.set(0, 0);
                rollPlayer.doABarrelRoll$changeElytraLook(pitchDelta, cursorDeltaX, 0, ModConfig.INSTANCE.getDesktopSensitivity(), mouseDelta);
            }

            return true;
        }

        mouseTurnVec.set(0, 0);
        return false;
    }

    @Override
    public Vector2d doABarrelRoll$getMouseTurnVec() {
        return mouseTurnVec;
    }
}
