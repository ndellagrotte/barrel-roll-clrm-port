package io.github.ndellagrotte.doabarrelroll.net;

import io.github.ndellagrotte.doabarrelroll.api.RollEntity;

/**
 * Client networking entry point. Phase 1 only holds the handshake state;
 * packet registration/handlers are added in Phase 2.
 */
public class ClientNetworking {
    public static final HandshakeClient HANDSHAKE_CLIENT = new HandshakeClient();

    public static void init() {
        // Phase 2: register S2C packet handlers, logout reset listener.
    }

    public static void sendRollUpdate(RollEntity rollEntity) {
        // Phase 2: send RollSyncC2SPacket to the server when connected.
    }
}
