package io.github.ndellagrotte.doabarrelroll.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Client networking entry point. Holds the handshake state and sends the local player's roll;
 * config handshake packets are not implemented yet.
 */
public class ClientNetworking {
    public static final HandshakeClient HANDSHAKE_CLIENT = new HandshakeClient();

    public static void init() {
    }

    public static void sendRollUpdate(EntityPlayerSP player, boolean rolling) {
        if (Minecraft.getMinecraft().getConnection() == null) {
            return;
        }
        ModNetworking.CHANNEL.sendToServer(new RollSyncC2SPacket(rolling, player.getRoll()));
    }
}
