package io.github.ndellagrotte.doabarrelroll.net;

import io.github.ndellagrotte.doabarrelroll.DoABarrelRoll;
import io.github.ndellagrotte.doabarrelroll.api.event.ClientEvents;
import io.github.ndellagrotte.doabarrelroll.config.LimitedModConfigServer;
import io.github.ndellagrotte.doabarrelroll.config.ModConfigServer;

import java.util.Optional;

/**
 * Client side of the config handshake. Stores the config received from the server.
 * The packet exchange itself is implemented in Phase 2 ({@link ClientNetworking}).
 */
public class HandshakeClient {
    public static final int PROTOCOL_VERSION = 4;

    private LimitedModConfigServer serverConfig;
    private ModConfigServer fullServerConfig;
    private boolean hasConnected = false;

    /**
     * Called when a config sync packet arrives. Returns whether the config was accepted.
     */
    public boolean handleConfigSync(int protocolVersion, LimitedModConfigServer config, boolean isLimited, ModConfigServer fullConfig) {
        if (protocolVersion < PROTOCOL_VERSION) {
            DoABarrelRoll.LOGGER.error("Server is using an outdated version of Do a Barrel Roll, please update!");
            return false;
        } else if (protocolVersion > PROTOCOL_VERSION) {
            DoABarrelRoll.LOGGER.warn("Server is using a newer version of Do a Barrel Roll, this may cause issues.");
        }

        this.serverConfig = config;
        if (!isLimited) {
            this.fullServerConfig = fullConfig;
        } else {
            this.fullServerConfig = null;
        }
        this.hasConnected = true;

        ClientEvents.updateServerConfig(config);
        return true;
    }

    public Optional<LimitedModConfigServer> getConfig() {
        return Optional.ofNullable(serverConfig);
    }

    public Optional<ModConfigServer> getFullConfig() {
        return Optional.ofNullable(fullServerConfig);
    }

    public boolean hasConnected() {
        return hasConnected;
    }

    public void reset() {
        serverConfig = null;
        fullServerConfig = null;
        hasConnected = false;
    }
}
