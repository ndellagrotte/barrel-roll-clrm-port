package io.github.ndellagrotte.doabarrelroll.util.key;

import io.github.ndellagrotte.doabarrelroll.DoABarrelRollClient;
import net.minecraftforge.client.settings.IKeyConflictContext;

/**
 * Key conflict context that is only active while the player is elytra flying with the mod enabled.
 * Replaces the upstream InputContext system: movement keybinds using this context don't conflict
 * with normal gameplay binds (e.g. W for thrust vs W for walking) and only fire while gliding.
 */
public class FallFlyingKeyConflictContext implements IKeyConflictContext {
    public static final FallFlyingKeyConflictContext INSTANCE = new FallFlyingKeyConflictContext();

    @Override
    public boolean isActive() {
        return DoABarrelRollClient.isFallFlying();
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return other == this;
    }
}
