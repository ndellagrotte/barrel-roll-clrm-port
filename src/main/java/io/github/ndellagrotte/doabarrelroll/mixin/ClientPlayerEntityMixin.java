package io.github.ndellagrotte.doabarrelroll.mixin;

import io.github.ndellagrotte.doabarrelroll.api.event.RollContext;
import io.github.ndellagrotte.doabarrelroll.api.event.RollEvents;
import io.github.ndellagrotte.doabarrelroll.api.rotation.RotationInstant;
import io.github.ndellagrotte.doabarrelroll.config.ActivationBehaviour;
import io.github.ndellagrotte.doabarrelroll.config.ModConfig;
import io.github.ndellagrotte.doabarrelroll.config.Sensitivity;
import io.github.ndellagrotte.doabarrelroll.flight.RotationModifiers;
import io.github.ndellagrotte.doabarrelroll.math.MagicNumbers;
import io.github.ndellagrotte.doabarrelroll.net.ClientNetworking;
import io.github.ndellagrotte.doabarrelroll.util.MixinHooks;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

@Mixin(EntityPlayerSP.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntityMixin {
    @Unique
    private boolean lastSentIsRolling;
    @Unique
    private float lastSentRoll;

    /**
     * Jump tracking for the triple-jump/hybrid activation behaviours (upstream tickMovement RETURN hook),
     * plus the roll sync packet (upstream sendMovementPackets TAIL hook).
     * Both upstream methods map to {@code onUpdateWalkingPlayer} in 1.12.2.
     */
    @Inject(
            method = "onUpdateWalkingPlayer",
            at = @At("TAIL")
    )
    private void doABarrelRoll$onUpdateWalkingPlayerTail(CallbackInfo ci) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;

        if (self.onGround) {
            MixinHooks.secondJump = false;
            MixinHooks.thirdJump = false;
        }

        MixinHooks.wasJumping = self.movementInput.jump;

        var isRolling = doABarrelRoll$isRolling();
        var rollDiff = doABarrelRoll$getRoll() - lastSentRoll;
        if (isRolling != lastSentIsRolling || rollDiff != 0.0f) {
            ClientNetworking.sendRollUpdate(this);

            lastSentIsRolling = isRolling;
            lastSentRoll = doABarrelRoll$getRoll();
        }
    }

    /**
     * Intercepts the elytra deploy packet for the TRIPLE_JUMP/HYBRID activation behaviours.
     * 1.12.2 has no client-side checkGliding equivalent; the deploy attempt is exactly this
     * {@code START_FALL_FLYING} packet send inside {@code onLivingUpdate}.
     */
    @Redirect(
            method = "onLivingUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V"
            )
    )
    private void doABarrelRoll$redirectDeployPacket(NetHandlerPlayClient connection, Packet<?> packet) {
        if (packet instanceof CPacketEntityAction
                && ((CPacketEntityAction) packet).getAction() == CPacketEntityAction.Action.START_FALL_FLYING
                && doABarrelRoll$shouldSuppressElytraDeploy()) {
            return;
        }
        connection.sendPacket(packet);
    }

    @Unique
    private boolean doABarrelRoll$shouldSuppressElytraDeploy() {
        var behaviour = ModConfig.INSTANCE.getActivationBehaviour();
        if (behaviour == ActivationBehaviour.VANILLA) {
            return false;
        }

        // This method is only reached while airborne with jump held, so !wasJumping marks a new press.
        var shouldCancel = behaviour == ActivationBehaviour.TRIPLE_JUMP;
        if (!MixinHooks.wasJumping) {
            MixinHooks.wasJumping = true;
            if (!MixinHooks.secondJump) {
                MixinHooks.secondJump = true;
                return shouldCancel;
            } else {
                // Set thirdJump to true in HYBRID mode, but toggle it in HYBRID_TOGGLE mode.
                MixinHooks.thirdJump = behaviour != ActivationBehaviour.HYBRID_TOGGLE || !MixinHooks.thirdJump;
            }
            return false;
        }
        return shouldCancel;
    }

    @Override
    @Unique
    protected void doABarrelRoll$tickRollState() {
        // Update rolling status
        doABarrelRoll$setRolling(RollEvents.shouldRoll());
    }

    @Override
    public void doABarrelRoll$changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;

        var rotDelta = RotationInstant.of(pitch, yaw, roll);
        var currentRoll = doABarrelRoll$getRoll();
        var currentRotation = RotationInstant.of(
                self.rotationPitch,
                self.rotationYaw,
                currentRoll
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

        doABarrelRoll$changeElytraLook((float) rotDelta.pitch(), (float) rotDelta.yaw(), (float) rotDelta.roll());
    }

    @Override
    public void doABarrelRoll$changeElytraLook(float pitch, float yaw, float roll) {
        EntityPlayerSP self = (EntityPlayerSP) (Object) this;

        var currentPitch = self.rotationPitch;
        var currentYaw = self.rotationYaw;
        var currentRoll = doABarrelRoll$getRoll();

        // Convert pitch, yaw, and roll to a facing and left vector
        // getLook(1.0F) == getVectorForRotation(rotationPitch, rotationYaw)
        Vec3d lookVec = self.getLook(1.0F);
        var facing = new Vector3d(lookVec.x, lookVec.y, lookVec.z);
        var left = new Vector3d(1, 0, 0);
        doABarrelRoll$rotate(left, 0, 0, 1, -currentRoll * MagicNumbers.TORAD);
        doABarrelRoll$rotate(left, 1, 0, 0, -currentPitch * MagicNumbers.TORAD);
        doABarrelRoll$rotate(left, 0, 1, 0, -(currentYaw + 180) * MagicNumbers.TORAD);

        // Apply pitch
        doABarrelRoll$rotate(facing, left.x, left.y, left.z, -0.15 * pitch * MagicNumbers.TORAD);

        // Apply yaw
        var up = new Vector3d();
        up.cross(facing, left);
        doABarrelRoll$rotate(facing, up.x, up.y, up.z, 0.15 * yaw * MagicNumbers.TORAD);
        doABarrelRoll$rotate(left, up.x, up.y, up.z, 0.15 * yaw * MagicNumbers.TORAD);

        // Apply roll
        doABarrelRoll$rotate(left, facing.x, facing.y, facing.z, 0.15 * roll * MagicNumbers.TORAD);

        // Extract new pitch, yaw, and roll
        double newPitch = -Math.asin(facing.y) * MagicNumbers.TODEG;
        double newYaw = -Math.atan2(facing.x, facing.z) * MagicNumbers.TODEG;

        var normalLeft = new Vector3d(1, 0, 0);
        doABarrelRoll$rotate(normalLeft, 0, 1, 0, -(newYaw + 180) * MagicNumbers.TORAD);
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
        self.turn((float) (deltaX / 0.15), (float) (-deltaY / 0.15));

        // Apply roll
        this.roll += (float) deltaRoll;
        this.prevRoll += (float) deltaRoll;
    }

    /**
     * Rotates a vector around an axis by the given angle (radians), matching JOML's
     * counter-clockwise axis rotation semantics (javax.vecmath is bundled with 1.12.2).
     */
    @Unique
    private static void doABarrelRoll$rotate(Vector3d vec, double axisX, double axisY, double axisZ, double angle) {
        var mat = new Matrix4d();
        mat.set(new AxisAngle4d(axisX, axisY, axisZ, angle));
        mat.transform(vec);
    }
}
