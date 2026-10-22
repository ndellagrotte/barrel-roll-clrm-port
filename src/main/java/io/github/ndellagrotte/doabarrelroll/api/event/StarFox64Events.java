package io.github.ndellagrotte.doabarrelroll.api.event;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class StarFox64Events {
    private StarFox64Events() {
    }

    public static void doesABarrelRoll(EntityPlayerSP player) {
        MinecraftForge.EVENT_BUS.post(new DoesABarrelRollEvent(player));
    }

    public static final class DoesABarrelRollEvent extends Event {
        private final EntityPlayerSP player;

        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public DoesABarrelRollEvent() {
            this(null);
        }

        public DoesABarrelRollEvent(EntityPlayerSP player) {
            this.player = player;
        }

        public EntityPlayerSP getPlayer() {
            return player;
        }
    }
}
