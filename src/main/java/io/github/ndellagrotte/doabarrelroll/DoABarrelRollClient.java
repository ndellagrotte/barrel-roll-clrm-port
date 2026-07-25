package io.github.ndellagrotte.doabarrelroll;

import io.github.ndellagrotte.doabarrelroll.api.event.ClientEvents;
import io.github.ndellagrotte.doabarrelroll.api.event.RollEvents;
import io.github.ndellagrotte.doabarrelroll.api.event.RollGroup;
import io.github.ndellagrotte.doabarrelroll.config.ActivationBehaviour;
import io.github.ndellagrotte.doabarrelroll.config.LimitedModConfigServer;
import io.github.ndellagrotte.doabarrelroll.config.ModConfig;
import io.github.ndellagrotte.doabarrelroll.flight.RotationModifiers;
import io.github.ndellagrotte.doabarrelroll.math.Smoother;
import io.github.ndellagrotte.doabarrelroll.net.ClientNetworking;
import io.github.ndellagrotte.doabarrelroll.util.MixinHooks;
import net.minecraft.client.Minecraft;

public class DoABarrelRollClient {
    public static final Smoother PITCH_SMOOTHER = new Smoother();
    public static final Smoother YAW_SMOOTHER = new Smoother();
    public static final Smoother ROLL_SMOOTHER = new Smoother();
    public static final RollGroup FALL_FLYING_GROUP = RollGroup.of(DoABarrelRoll.id("fall_flying"));

    public static void init() {
        FALL_FLYING_GROUP.trueIf(DoABarrelRollClient::isFallFlying);

        // Keyboard modifiers
        RollEvents.EARLY_CAMERA_MODIFIERS.register(context -> context
                        .useModifier(RotationModifiers.buttonControls(1800)),
                2000, FALL_FLYING_GROUP);

        // Mouse modifiers, including swapping axes
        RollEvents.EARLY_CAMERA_MODIFIERS.register(context -> context
                        .useModifier(ModConfig.INSTANCE::configureRotation),
                1000, FALL_FLYING_GROUP);

        // Generic movement modifiers, banking and such
        RollEvents.LATE_CAMERA_MODIFIERS.register(context -> context
                        .useModifier(RotationModifiers::applyControlSurfaceEfficacy, ModConfig.INSTANCE::getSimulateControlSurfaceEfficacy)
                        .useModifier(RotationModifiers.smoothing(
                                PITCH_SMOOTHER, YAW_SMOOTHER, ROLL_SMOOTHER,
                                ModConfig.INSTANCE.getSmoothing()
                        ))
                        .useModifier(RotationModifiers::banking, ModConfig.INSTANCE::getEnableBanking)
                        .useModifier(RotationModifiers::reorient, ModConfig.INSTANCE::getAutomaticRighting),
                1000, FALL_FLYING_GROUP);

        ClientEvents.SERVER_CONFIG_UPDATE.register(ModConfig.INSTANCE::notifyPlayerOfServerConfig);

        ModConfig.touch();

        ClientNetworking.init();
    }

    public static void clearValues() {
        PITCH_SMOOTHER.clear();
        YAW_SMOOTHER.clear();
        ROLL_SMOOTHER.clear();
    }

    public static boolean isFallFlying() {
        if (!ClientNetworking.HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::forceEnabled).orElse(false)) {
            var hybrid = ModConfig.INSTANCE.getActivationBehaviour() == ActivationBehaviour.HYBRID ||
                    ModConfig.INSTANCE.getActivationBehaviour() == ActivationBehaviour.HYBRID_TOGGLE;
            if (hybrid && !MixinHooks.thirdJump) {
                return false;
            }

            if (!ModConfig.INSTANCE.getModEnabled()) {
                return false;
            }
        }

        var player = Minecraft.getMinecraft().player;
        if (player == null) {
            return false;
        }
        if (ModConfig.INSTANCE.getDisableWhenSubmerged() && player.isInWater()) {
            return false;
        }
        return player.isElytraFlying();
    }

    public static boolean isConnectedToRealms() {
        return false; // We are not connected to realms.
    }
}
