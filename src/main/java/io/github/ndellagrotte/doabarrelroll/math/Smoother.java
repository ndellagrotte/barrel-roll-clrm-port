package io.github.ndellagrotte.doabarrelroll.math;

/**
 * Port of the vanilla 1.21 {@code Smoother} (mojmap {@code net.minecraft.util.SmoothDouble},
 * yarn {@code net.minecraft.util.math.Smoother}), recovered from official 1.21.4 bytecode.
 * Exponentially smooths per-frame input deltas: passes sustained input through
 * and releases residual input with a 50% decay per call.
 */
public class Smoother {
    private double actualSum;
    private double smoothedSum;
    private double movementLatency;

    public double smooth(double original, double smoother) {
        this.actualSum += original;
        double d0 = this.actualSum - this.smoothedSum;
        // Mth.lerp(0.5, movementLatency, d0)
        double d1 = this.movementLatency + (d0 - this.movementLatency) * 0.5;
        double d2 = Math.signum(d0);

        if (d2 * d0 > d2 * this.movementLatency) {
            d0 = d1;
        }

        this.movementLatency = d1;
        this.smoothedSum += d0 * smoother;
        return d0 * smoother;
    }

    public void clear() {
        this.actualSum = 0.0;
        this.smoothedSum = 0.0;
        this.movementLatency = 0.0;
    }
}
