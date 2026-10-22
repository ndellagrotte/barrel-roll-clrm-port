package io.github.ndellagrotte.doabarrelroll.api.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class ThrustEvents {
    private ThrustEvents() {
    }

    /**
     * Posts a mutable thrust input event and returns the value left by subscribers.
     */
    public static double modifyThrustInput(double input) {
        ModifyThrustInputEvent event = new ModifyThrustInputEvent(input);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getInput();
    }

    public static final class ModifyThrustInputEvent extends Event {
        private double input;

        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public ModifyThrustInputEvent() {
            this(0.0);
        }

        public ModifyThrustInputEvent(double input) {
            this.input = input;
        }

        public double getInput() {
            return input;
        }

        public void setInput(double input) {
            this.input = input;
        }
    }
}
