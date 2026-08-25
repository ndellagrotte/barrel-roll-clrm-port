package io.github.ndellagrotte.doabarrelroll;

import io.github.ndellagrotte.doabarrelroll.config.LimitedModConfigServer;
import io.github.ndellagrotte.doabarrelroll.config.ModConfig;
import io.github.ndellagrotte.doabarrelroll.net.ClientNetworking;
import io.github.ndellagrotte.doabarrelroll.util.key.FallFlyingKeyConflictContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModKeybindings {
    private static final String CATEGORY_MAIN = "category.do_a_barrel_roll.do_a_barrel_roll";
    private static final String CATEGORY_MOVEMENT = "category.do_a_barrel_roll.do_a_barrel_roll.movement";

    public static final KeyBinding TOGGLE_ENABLED = new KeyBinding(
            "key.do_a_barrel_roll.toggle_enabled",
            KeyConflictContext.IN_GAME,
            Keyboard.KEY_I,
            CATEGORY_MAIN
    );
    public static final KeyBinding TOGGLE_THRUST = new KeyBinding(
            "key.do_a_barrel_roll.toggle_thrust",
            KeyConflictContext.IN_GAME,
            Keyboard.KEY_NONE,
            CATEGORY_MAIN
    );
    public static final KeyBinding OPEN_CONFIG = new KeyBinding(
            "key.do_a_barrel_roll.open_config",
            KeyConflictContext.IN_GAME,
            Keyboard.KEY_NONE,
            CATEGORY_MAIN
    );

    public static final KeyBinding PITCH_UP = new KeyBinding(
            "key.do_a_barrel_roll.pitch_up",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_NONE,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding PITCH_DOWN = new KeyBinding(
            "key.do_a_barrel_roll.pitch_down",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_NONE,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding YAW_LEFT = new KeyBinding(
            "key.do_a_barrel_roll.yaw_left",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_A,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding YAW_RIGHT = new KeyBinding(
            "key.do_a_barrel_roll.yaw_right",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_D,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding ROLL_LEFT = new KeyBinding(
            "key.do_a_barrel_roll.roll_left",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_NONE,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding ROLL_RIGHT = new KeyBinding(
            "key.do_a_barrel_roll.roll_right",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_NONE,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding THRUST_FORWARD = new KeyBinding(
            "key.do_a_barrel_roll.thrust_forward",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_W,
            CATEGORY_MOVEMENT
    );
    public static final KeyBinding THRUST_BACKWARD = new KeyBinding(
            "key.do_a_barrel_roll.thrust_backward",
            FallFlyingKeyConflictContext.INSTANCE,
            Keyboard.KEY_NONE,
            CATEGORY_MOVEMENT
    );

    public static final List<KeyBinding> ALL = Collections.unmodifiableList(Arrays.asList(
            TOGGLE_ENABLED,
            TOGGLE_THRUST,
            OPEN_CONFIG,
            PITCH_UP,
            PITCH_DOWN,
            YAW_LEFT,
            YAW_RIGHT,
            ROLL_LEFT,
            ROLL_RIGHT,
            THRUST_FORWARD,
            THRUST_BACKWARD
    ));

    public static void clientTick(Minecraft client) {
        while (TOGGLE_ENABLED.isPressed()) {
            if (!ClientNetworking.HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::forceEnabled).orElse(false)) {
                ModConfig.INSTANCE.setModEnabled(!ModConfig.INSTANCE.getModEnabled());
                ModConfig.INSTANCE.save();

                if (client.player != null) {
                    client.player.sendStatusMessage(new TextComponentTranslation(
                            "key.do_a_barrel_roll." +
                                    (ModConfig.INSTANCE.getModEnabled() ? "toggle_enabled.enable" : "toggle_enabled.disable")
                    ), true);
                }
            } else {
                if (client.player != null) {
                    client.player.sendStatusMessage(
                            new TextComponentTranslation("key.do_a_barrel_roll.toggle_enabled.disallowed"),
                            true
                    );
                }
            }
        }
        while (TOGGLE_THRUST.isPressed()) {
            if (ClientNetworking.HANDSHAKE_CLIENT.getConfig().map(LimitedModConfigServer::allowThrusting).orElse(false)) {
                ModConfig.INSTANCE.setEnableThrust(!ModConfig.INSTANCE.getEnableThrust());
                ModConfig.INSTANCE.save();

                if (client.player != null) {
                    client.player.sendStatusMessage(new TextComponentTranslation(
                            "key.do_a_barrel_roll." +
                                    (ModConfig.INSTANCE.getEnableThrust() ? "toggle_thrust.enable" : "toggle_thrust.disable")
                    ), true);
                }
            } else {
                if (client.player != null) {
                    client.player.sendStatusMessage(
                            new TextComponentTranslation("key.do_a_barrel_roll.toggle_thrust.disallowed"),
                            true
                    );
                }
            }
        }
        while (OPEN_CONFIG.isPressed()) {
            // This port has no in-game config screen; point the user to the config file instead.
            if (client.player != null) {
                client.player.sendStatusMessage(new TextComponentTranslation(
                        "config.do_a_barrel_roll.no_gui_notice",
                        ModConfig.CONFIG_FILE.getName()
                ), false);
            }
        }
    }
}
