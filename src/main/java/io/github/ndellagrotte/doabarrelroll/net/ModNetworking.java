package io.github.ndellagrotte.doabarrelroll.net;

import io.github.ndellagrotte.doabarrelroll.DoABarrelRoll;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Common networking entry point. Roll only needs a client to server feed: the loader syncs
 * {@code Entity.roll} from the server to tracking clients on its own.
 */
public class ModNetworking {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(DoABarrelRoll.MODID);

    private static int nextId = 0;

    public static void init() {
        CHANNEL.registerMessage(RollSyncC2SPacket.Handler.class, RollSyncC2SPacket.class, nextId++, Side.SERVER);
    }
}
