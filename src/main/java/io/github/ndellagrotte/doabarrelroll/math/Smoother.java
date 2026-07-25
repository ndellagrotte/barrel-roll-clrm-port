package io.github.ndellagrotte.doabarrelroll.math;

/**
 * Port of the vanilla 1.21 {@code Smoother} (mojmap {@code net.minecraft.util.SmoothDouble},
 * yarn {@code net.minecraft.util.math.Smoother}), recovered from official 1.21.4 bytecode.
 * Exponentially smooths per-frame input deltas: passes sustained input through
 * and releases residual input with a decay that this port has made time-based rather
 * than per-call - see {@link #ATTACK_TAU_SECONDS}.
 */
public class Smoother {
    /**
     * Reference wall-clock time constant for the attack limiter, in seconds.
     *
     * <p>{@code movementLatency} is an attack limiter: it caps how fast the emitted value may
     * ramp toward a growing backlog. Vanilla lerps it 50% toward the backlog on every
     * <em>call</em>:
     *
     * <pre>d1 = Mth.lerp(0.5, movementLatency, d0);</pre>
     *
     * <p>Nothing scales that 0.5 by elapsed time, so the limiter's real time constant is
     * {@code 1 / (callsPerSecond * ln2)}. With this pipeline's two calls per frame that is
     * 12.0 ms at 60 fps but only 1.4 ms at 500 fps - the limiter stops filtering as framerate
     * rises. Measured against this exact algorithm, 2.8x more 40 Hz and 1.5x more 20 Hz input
     * jitter (hand tremor, mouse sensor noise, integer mouse-count granularity, frame-time
     * variance) reaches the camera at 500 fps than at 90 fps, while sub-2 Hz response - the
     * actual flight model - is identical. That is why the mod feels correct when the framerate
     * is capped and subtly wrong when it is not.
     *
     * <p>Replacing the constant 0.5 with {@code 1 - exp(-renderDelta / ATTACK_TAU_SECONDS)}
     * holds the limiter at a fixed 12.0 ms regardless of framerate. The exponential also
     * composes correctly across uneven frames ({@code exp(-a/t)*exp(-b/t) == exp(-(a+b)/t)}),
     * which the fixed 0.5 did not.
     *
     * <p>Anchored so the factor is exactly 0.5 at 60 fps: 60 fps stays bit-identical to
     * vanilla and every other framerate is normalised to it. This is a deliberate divergence
     * from vanilla and from upstream DABR, both of which have this bug.
     */
    private static final double ATTACK_TAU_SECONDS = (1.0 / 60.0) / 0.6931471805599453; // 24.0449 ms

    private double actualSum;
    private double smoothedSum;
    private double movementLatency;

    /**
     * @param original     the new input to fold in
     * @param smoother     emission weight, {@code (1 / smoothness) * renderDelta}
     * @param renderDelta  seconds since the previous frame; drives the attack limiter's decay
     */
    public double smooth(double original, double smoother, double renderDelta) {
        this.actualSum += original;
        double d0 = this.actualSum - this.smoothedSum;
        // Vanilla is Mth.lerp(0.5, movementLatency, d0); see ATTACK_TAU_SECONDS for why the
        // 0.5 is time-normalised here. renderDelta arrives clamped to [0, 0.1] by
        // EntityRendererMixin#doABarrelRoll$beginFrame, so this stays within [0, 0.984];
        // the max() only guards callers that bypass beginFrame.
        double attack = 1.0 - Math.exp(-Math.max(0.0, renderDelta) / ATTACK_TAU_SECONDS);
        double d1 = this.movementLatency + (d0 - this.movementLatency) * attack;
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
