package io.github.ndellagrotte.doabarrelroll.flight;

import java.util.HashMap;
import java.util.Map;

import io.github.ndellagrotte.doabarrelroll.DoABarrelRoll;
import io.github.ndellagrotte.doabarrelroll.ModKeybindings;
import io.github.ndellagrotte.doabarrelroll.api.event.RollContext;
import io.github.ndellagrotte.doabarrelroll.api.rotation.RotationInstant;
import io.github.ndellagrotte.doabarrelroll.config.ModConfig;
import io.github.ndellagrotte.doabarrelroll.config.Sensitivity;
import io.github.ndellagrotte.doabarrelroll.math.Expression;
import io.github.ndellagrotte.doabarrelroll.math.MagicNumbers;
import io.github.ndellagrotte.doabarrelroll.math.Smoother;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;

public class RotationModifiers {
    public static final double ROLL_REORIENT_CUTOFF = Math.sqrt(10.0 / 3.0);

    public static RollContext.ConfiguresRotation buttonControls(double power) {
        return (rotationInstant, context) -> {
            double delta = power * context.getRenderDelta();
            double pitch = 0.0;
            double yaw = 0.0;
            double roll = 0.0;

            if (ModKeybindings.PITCH_UP.isKeyDown()) {
                pitch -= delta;
            }
            if (ModKeybindings.PITCH_DOWN.isKeyDown()) {
                pitch += delta;
            }
            if (ModKeybindings.YAW_LEFT.isKeyDown()) {
                yaw -= delta;
            }
            if (ModKeybindings.YAW_RIGHT.isKeyDown()) {
                yaw += delta;
            }
            if (ModKeybindings.ROLL_LEFT.isKeyDown()) {
                roll -= delta;
            }
            if (ModKeybindings.ROLL_RIGHT.isKeyDown()) {
                roll += delta;
            }

            // Putting this in the roll value, since it'll be swapped later
            return rotationInstant.add(pitch, yaw, roll);
        };
    }

    public static RollContext.ConfiguresRotation smoothing(Smoother pitchSmoother, Smoother yawSmoother, Smoother rollSmoother, Sensitivity smoothness) {
        return (rotationInstant, context) -> {
            double delta = context.getRenderDelta();
            return RotationInstant.of(
                    smoothness.pitch == 0 ? rotationInstant.pitch() : pitchSmoother.smooth(rotationInstant.pitch(), 1 / smoothness.pitch * delta, delta),
                    smoothness.yaw == 0 ? rotationInstant.yaw() : yawSmoother.smooth(rotationInstant.yaw(), 1 / smoothness.yaw * delta, delta),
                    smoothness.roll == 0 ? rotationInstant.roll() : rollSmoother.smooth(rotationInstant.roll(), 1 / smoothness.roll * delta, delta)
            );
        };
    }

    public static RotationInstant banking(RotationInstant rotationInstant, RollContext context) {
        double delta = context.getRenderDelta();
        RotationInstant currentRotation = context.getCurrentRotation();
        double currentRoll = currentRotation.roll() * MagicNumbers.TORAD;

        Expression xExpression = ModConfig.INSTANCE.getBankingXFormula().getCompiledOrDefaulting(0);
        Expression yExpression = ModConfig.INSTANCE.getBankingYFormula().getCompiledOrDefaulting(0);

        Map<String, Double> vars = getVars(context);
        vars.put("banking_strength", ModConfig.INSTANCE.getBankingStrength());

        double dX = xExpression.eval(vars);
        double dY = yExpression.eval(vars);

        // check if we accidentally got NaN, for some reason this happens sometimes
        if (Double.isNaN(dX)) dX = 0;
        if (Double.isNaN(dY)) dY = 0;

        return rotationInstant.addAbsolute(dX * delta, dY * delta, currentRoll);
    }

    public static RotationInstant reorient(RotationInstant rotationInstant, RollContext context) {
        double delta = context.getRenderDelta();
        double currentRoll = context.getCurrentRotation().roll() * MagicNumbers.TORAD;
        double strength = 10 * ModConfig.INSTANCE.getRightingStrength();

        double cutoff = ROLL_REORIENT_CUTOFF;
        double rollDelta = 0;
        if (-cutoff < currentRoll && currentRoll < cutoff) {
            rollDelta = -Math.pow(currentRoll, 3) / 3.0 + currentRoll; //0.1 * Math.pow(currentRoll, 5);
        }

        return rotationInstant.add(0, 0, -rollDelta * strength * delta);
    }

    public static RollContext.ConfiguresRotation fixNaN(String name) {
        return (rotationInstant, context) -> {
            if (Double.isNaN(rotationInstant.pitch())) {
                rotationInstant = RotationInstant.of(0, rotationInstant.yaw(), rotationInstant.roll());
                DoABarrelRoll.LOGGER.warn("NaN found in pitch for {}, setting to 0 as fallback", name);
            }
            if (Double.isNaN(rotationInstant.yaw())) {
                rotationInstant = RotationInstant.of(rotationInstant.pitch(), 0, rotationInstant.roll());
                DoABarrelRoll.LOGGER.warn("NaN found in yaw for {}, setting to 0 as fallback", name);
            }
            if (Double.isNaN(rotationInstant.roll())) {
                rotationInstant = RotationInstant.of(rotationInstant.pitch(), rotationInstant.yaw(), 0);
                DoABarrelRoll.LOGGER.warn("NaN found in roll for {}, setting to 0 as fallback", name);
            }
            return rotationInstant;
        };
    }

    public static RotationInstant applyControlSurfaceEfficacy(RotationInstant rotationInstant, RollContext context) {
        Expression elevatorExpression = ModConfig.INSTANCE.getElevatorEfficacyFormula().getCompiledOrDefaulting(1);
        Expression aileronExpression = ModConfig.INSTANCE.getAileronEfficacyFormula().getCompiledOrDefaulting(1);
        Expression rudderExpression = ModConfig.INSTANCE.getRudderEfficacyFormula().getCompiledOrDefaulting(1);

        Map<String, Double> vars = getVars(context);
        return rotationInstant.multiply(elevatorExpression.eval(vars), rudderExpression.eval(vars), aileronExpression.eval(vars));
    }

    private static Map<String, Double> getVars(RollContext context) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        assert player != null;

        RotationInstant currentRotation = context.getCurrentRotation();
        Vec3d rotationVector = player.getLook(1.0F);
        Vec3d velocity = new Vec3d(player.motionX, player.motionY, player.motionZ);
        return new HashMap<String, Double>() {{
            put("pitch", currentRotation.pitch());
            put("yaw", currentRotation.yaw());
            put("roll", currentRotation.roll());
            put("velocity_length", velocity.length());
            put("velocity_x", velocity.x);
            put("velocity_y", velocity.y);
            put("velocity_z", velocity.z);
            put("look_x", rotationVector.x);
            put("look_y", rotationVector.y);
            put("look_z", rotationVector.z);
        }};
    }
}
