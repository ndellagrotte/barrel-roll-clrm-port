package io.github.ndellagrotte.doabarrelroll.config;

public enum ActivationBehaviour {
    VANILLA,
    TRIPLE_JUMP,
    HYBRID,
    HYBRID_TOGGLE;

    public int getId() {
        return this.ordinal();
    }

    public String getTranslationKey() {
        return "config.do_a_barrel_roll.controls.activation_behaviour." + this.name().toLowerCase();
    }
}
