package io.github.ndellagrotte.doabarrelroll.net;

import net.minecraft.network.NetHandlerPlayServer;

public interface SyncableConfig<T, L> {
    Integer getSyncTimeout();

    String getSyncTimeoutMessage();

    L getLimited(NetHandlerPlayServer handler);
}
