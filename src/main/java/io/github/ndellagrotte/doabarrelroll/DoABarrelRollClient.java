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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DoABarrelRollClient {
    public static final Smoother PITCH_SMOOTHER = new Smoother();
    public static final Smoother YAW_SMOOTHER = new Smoother();
    public static final Smoother ROLL_SMOOTHER = new Smoother();
    public static final RollGroup FALL_FLYING_GROUP = RollGroup.of(DoABarrelRoll.id("fall_flying"));

    public static void init() {
        FALL_FLYING_GROUP.trueIf(DoABarrelRollClient::isFallFlying);
        MinecraftForge.EVENT_BUS.register(new DoABarrelRollClient());

        ModConfig.touch();

        ClientNetworking.init();
    }

    /**
     * The old event implementation sorted lower numeric priorities first, so the mouse modifier
     * runs before the keyboard modifier. Forge priorities preserve that ordering explicitly.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEarlyMouseModifiers(RollEvents.EarlyCameraModifiersEvent event) {
        if (FALL_FLYING_GROUP.get()) {
            event.getContext().useModifier(ModConfig.INSTANCE::configureRotation);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEarlyKeyboardModifiers(RollEvents.EarlyCameraModifiersEvent event) {
        if (FALL_FLYING_GROUP.get()) {
            event.getContext().useModifier(RotationModifiers.buttonControls(1800));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLateCameraModifiers(RollEvents.LateCameraModifiersEvent event) {
        if (FALL_FLYING_GROUP.get()) {
            event.getContext()
                    .useModifier(RotationModifiers::applyControlSurfaceEfficacy,
                            ModConfig.INSTANCE::getSimulateControlSurfaceEfficacy)
                    .useModifier(RotationModifiers.smoothing(
                            PITCH_SMOOTHER, YAW_SMOOTHER, ROLL_SMOOTHER,
                            ModConfig.INSTANCE.getSmoothing()
                    ))
                    .useModifier(RotationModifiers::banking, ModConfig.INSTANCE::getEnableBanking)
                    .useModifier(RotationModifiers::reorient, ModConfig.INSTANCE::getAutomaticRighting);
        }
    }

    @SubscribeEvent
    public void onServerConfigUpdate(ClientEvents.ServerConfigUpdateEvent event) {
        ModConfig.INSTANCE.notifyPlayerOfServerConfig(event.getConfig());
    }

    public static void clearValues() {
        PITCH_SMOOTHER.clear();
        YAW_SMOOTHER.clear();
        ROLL_SMOOTHER.clear();
    }

    public static boolean isFallFlying() {
        if (!ClientNetworking.HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::forceEnabled).orElse(false)) {
            boolean hybrid = ModConfig.INSTANCE.getActivationBehaviour() == ActivationBehaviour.HYBRID ||
                    ModConfig.INSTANCE.getActivationBehaviour() == ActivationBehaviour.HYBRID_TOGGLE;
            if (hybrid && !MixinHooks.thirdJump) {
                return false;
            }

            if (!ModConfig.INSTANCE.getModEnabled()) {
                return false;
            }
        }

        EntityPlayerSP player = Minecraft.getMinecraft().player;
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
