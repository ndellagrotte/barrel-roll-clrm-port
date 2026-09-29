package io.github.ndellagrotte.doabarrelroll.flight;

import io.github.ndellagrotte.doabarrelroll.api.event.RollContext;
import io.github.ndellagrotte.doabarrelroll.api.event.RollEvents;
import io.github.ndellagrotte.doabarrelroll.api.rotation.RotationInstant;
import io.github.ndellagrotte.doabarrelroll.config.Sensitivity;
import io.github.ndellagrotte.doabarrelroll.math.MagicNumbers;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Roll flight state and look pipeline for the local player. The roll itself lives on the loader's
 * {@code Entity.roll}/{@code prevRoll}; the loader snapshots {@code prevRoll} each tick and syncs the
 * server-side value to other clients.
 */
@SideOnly(Side.CLIENT)
public final class RollPlayer {
    private static boolean rolling;

    private RollPlayer() {
    }

    public static boolean isRolling() {
        return rolling;
    }

    /**
     * Per-tick roll bookkeeping, called at the end of the local player's tick. The loader has
     * already snapshotted {@code prevRoll} at tick start, and nothing moves the roll in between.
     */
    public static void tick(EntityPlayerSP player) {
        rolling = RollEvents.shouldRoll();

        if (!rolling) {
            player.setRoll(0.0f);
        }
    }

    public static void changeElytraLook(EntityPlayerSP player, double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta) {
        var rotDelta = RotationInstant.of(pitch, yaw, roll);
        var currentRotation = RotationInstant.of(
                player.rotationPitch,
                player.rotationYaw,
                player.getRoll()
        );
        var context = RollContext.of(currentRotation, rotDelta, mouseDelta);

        context.useModifier(RotationModifiers.fixNaN("INPUT"));
        RollEvents.earlyCameraModifiers(context);
        context.useModifier(RotationModifiers.fixNaN("EARLY_CAMERA_MODIFIERS"));
        context.useModifier((rotation, ctx) -> rotation.applySensitivity(sensitivity));
        context.useModifier(RotationModifiers.fixNaN("SENSITIVITY"));
        RollEvents.lateCameraModifiers(context);
        context.useModifier(RotationModifiers.fixNaN("LATE_CAMERA_MODIFIERS"));

        rotDelta = context.getRotationDelta();

        changeElytraLook(player, (float) rotDelta.pitch(), (float) rotDelta.yaw(), (float) rotDelta.roll());
    }

    public static void changeElytraLook(EntityPlayerSP player, float pitch, float yaw, float roll) {
        var currentPitch = player.rotationPitch;
        var currentYaw = player.rotationYaw;
        var currentRoll = player.getRoll();

        // Convert pitch, yaw, and roll to a facing and left vector
        // getLook(1.0F) == getVectorForRotation(rotationPitch, rotationYaw)
        Vec3d lookVec = player.getLook(1.0F);
        var facing = new Vector3d(lookVec.x, lookVec.y, lookVec.z);
        var left = new Vector3d(1, 0, 0);
        rotate(left, 0, 0, 1, -currentRoll * MagicNumbers.TORAD);
        rotate(left, 1, 0, 0, -currentPitch * MagicNumbers.TORAD);
        rotate(left, 0, 1, 0, -(currentYaw + 180) * MagicNumbers.TORAD);

        // Apply pitch
        rotate(facing, left.x, left.y, left.z, -0.15 * pitch * MagicNumbers.TORAD);

        // Apply yaw
        var up = new Vector3d();
        up.cross(facing, left);
        rotate(facing, up.x, up.y, up.z, 0.15 * yaw * MagicNumbers.TORAD);
        rotate(left, up.x, up.y, up.z, 0.15 * yaw * MagicNumbers.TORAD);

        // Apply roll
        rotate(left, facing.x, facing.y, facing.z, 0.15 * roll * MagicNumbers.TORAD);

        // Extract new pitch, yaw, and roll
        double newPitch = -Math.asin(facing.y) * MagicNumbers.TODEG;
        double newYaw = -Math.atan2(facing.x, facing.z) * MagicNumbers.TODEG;

        var normalLeft = new Vector3d(1, 0, 0);
        rotate(normalLeft, 0, 1, 0, -(newYaw + 180) * MagicNumbers.TORAD);
        var leftCrossNormalLeft = new Vector3d();
        leftCrossNormalLeft.cross(left, normalLeft);
        double newRoll = -Math.atan2(leftCrossNormalLeft.dot(facing), left.dot(normalLeft)) * MagicNumbers.TODEG;

        // Calculate deltas
        double deltaY = newPitch - currentPitch;
        double deltaX = newYaw - currentYaw;
        double deltaRoll = newRoll - currentRoll;

        // Apply vanilla pitch and yaw. Upstream calls changeLookDirection, which *adds*
        // deltaY * 0.15 to the pitch; 1.12.2's turn *subtracts* it, so the pitch argument is
        // negated to apply the same rotation the vector math above computed.
        player.turn((float) (deltaX / 0.15), (float) (-deltaY / 0.15));

        // Apply roll. Per-frame writers advance roll and prevRoll together so interpolation stays
        // continuous; setRoll would re-wrap and move prevRoll across the seam.
        player.roll += (float) deltaRoll;
        player.prevRoll += (float) deltaRoll;
    }

    /**
     * Rotates a vector around an axis by the given angle (radians), matching JOML's
     * counter-clockwise axis rotation semantics (javax.vecmath is bundled with 1.12.2).
     */
    private static void rotate(Vector3d vec, double axisX, double axisY, double axisZ, double angle) {
        var mat = new Matrix4d();
        mat.set(new AxisAngle4d(axisX, axisY, axisZ, angle));
        mat.transform(vec);
    }
}
