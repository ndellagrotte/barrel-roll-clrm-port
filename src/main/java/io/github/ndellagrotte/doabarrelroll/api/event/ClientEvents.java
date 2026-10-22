package io.github.ndellagrotte.doabarrelroll.api.event;

import io.github.ndellagrotte.doabarrelroll.config.LimitedModConfigServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static void updateServerConfig(LimitedModConfigServer config) {
        MinecraftForge.EVENT_BUS.post(new ServerConfigUpdateEvent(config));
    }

    public static final class ServerConfigUpdateEvent extends Event {
        private final LimitedModConfigServer config;

        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public ServerConfigUpdateEvent() {
            this(null);
        }

        public ServerConfigUpdateEvent(LimitedModConfigServer config) {
            this.config = config;
        }

        public LimitedModConfigServer getConfig() {
            return config;
        }
    }
}
