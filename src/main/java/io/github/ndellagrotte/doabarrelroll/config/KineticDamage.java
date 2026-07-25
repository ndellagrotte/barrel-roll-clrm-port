package io.github.ndellagrotte.doabarrelroll.config;

public enum KineticDamage {
    VANILLA,
    HIGH_SPEED,
    NONE,
    INSTANT_KILL;

    public static KineticDamage byName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return VANILLA;
        }
    }
}
