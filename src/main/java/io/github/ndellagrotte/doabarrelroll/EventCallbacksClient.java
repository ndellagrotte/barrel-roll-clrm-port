package io.github.ndellagrotte.doabarrelroll;

import net.minecraft.client.Minecraft;

public class EventCallbacksClient {
    public static void clientTick(Minecraft client) {
        if (!DoABarrelRollClient.isFallFlying()) {
            DoABarrelRollClient.clearValues();
        }

        ModKeybindings.clientTick(client);

        // Phase 3: StarFoxUtil.clientTick(client);
    }
}
