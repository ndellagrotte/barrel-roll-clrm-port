package io.github.ndellagrotte.doabarrelroll.api.event;

import net.minecraft.client.entity.EntityPlayerSP;
import io.github.ndellagrotte.doabarrelroll.impl.event.EventImpl;

public interface StarFox64Events {
    Event<DoesABarrelRollEvent> DOES_A_BARREL_ROLL = new EventImpl<>();

    static void doesABarrelRoll(EntityPlayerSP player) {
        for (var listener : DOES_A_BARREL_ROLL.getListeners()) {
            listener.onBarrelRoll(player);
        }
    }

    interface DoesABarrelRollEvent {
        void onBarrelRoll(EntityPlayerSP player);
    }
}
