package io.github.ndellagrotte.doabarrelroll.api.event;

import io.github.ndellagrotte.doabarrelroll.config.ModConfigServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class ServerEvents {
    private ServerEvents() {
    }

    public static void updateServerConfig(MinecraftServer server, ModConfigServer config) {
        MinecraftForge.EVENT_BUS.post(new ServerConfigUpdateEvent(server, config));
    }

    public static final class ServerConfigUpdateEvent extends Event {
        private final MinecraftServer server;
        private final ModConfigServer config;

        /**
         * Used by Forge 1.12.2 while discovering subscribers.
         */
        public ServerConfigUpdateEvent() {
            this(null, null);
        }

        public ServerConfigUpdateEvent(MinecraftServer server, ModConfigServer config) {
            this.server = server;
            this.config = config;
        }

        public MinecraftServer getServer() {
            return server;
        }

        public ModConfigServer getConfig() {
            return config;
        }
    }
}
