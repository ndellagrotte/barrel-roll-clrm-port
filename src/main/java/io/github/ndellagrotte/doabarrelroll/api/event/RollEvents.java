package io.github.ndellagrotte.doabarrelroll.api.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class RollEvents {
    private RollEvents() {
    }

    /**
     * Posts a roll check. If any subscriber unlocks the event, roll will be enabled.
     */
    public static boolean shouldRoll() {
        ShouldRollCheckEvent event = new ShouldRollCheckEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.shouldRoll();
    }

    /**
     * Posts camera modifiers that run <b>before</b> sensitivity and are therefore affected by it.
     */
    public static void earlyCameraModifiers(RollContext context) {
        MinecraftForge.EVENT_BUS.post(new EarlyCameraModifiersEvent(context));
    }

    /**
     * Posts camera modifiers that run <b>after</b> sensitivity and are therefore not affected by it.
     */
    public static void lateCameraModifiers(RollContext context) {
        MinecraftForge.EVENT_BUS.post(new LateCameraModifiersEvent(context));
    }

    /**
     * Forge event used to determine whether the local player's camera may roll.
     *
     * <p>Subscribers should call {@link #unlock()} when their condition is met. Unlocking is
     * intentionally one-way so a later subscriber cannot override an earlier positive result.
     */
    public static final class ShouldRollCheckEvent extends Event {
        private boolean shouldRoll;

        public boolean shouldRoll() {
            return shouldRoll;
        }

        public void unlock() {
            shouldRoll = true;
        }
    }

    public static class CameraModifiersEvent extends Event {
        private final RollContext context;

        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public CameraModifiersEvent() {
            this(null);
        }

        protected CameraModifiersEvent(RollContext context) {
            this.context = context;
        }

        public RollContext getContext() {
            return context;
        }
    }

    public static final class EarlyCameraModifiersEvent extends CameraModifiersEvent {
        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public EarlyCameraModifiersEvent() {
            super();
        }

        public EarlyCameraModifiersEvent(RollContext context) {
            super(context);
        }
    }

    public static final class LateCameraModifiersEvent extends CameraModifiersEvent {
        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public LateCameraModifiersEvent() {
            super();
        }

        public LateCameraModifiersEvent(RollContext context) {
            super(context);
        }
    }
}
