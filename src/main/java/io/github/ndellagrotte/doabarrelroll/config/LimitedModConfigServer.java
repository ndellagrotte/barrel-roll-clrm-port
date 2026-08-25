package io.github.ndellagrotte.doabarrelroll.config;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;

public interface LimitedModConfigServer {
    LimitedModConfigServer OPERATOR = new Impl(true, false);

    static LimitedModConfigServer fromJson(JsonObject json) {
        return new Impl(
                json.has("allowThrusting") ? json.get("allowThrusting").getAsBoolean() : ModConfigServer.DEFAULT.allowThrusting(),
                json.has("forceEnabled") ? json.get("forceEnabled").getAsBoolean() : ModConfigServer.DEFAULT.forceEnabled()
        );
    }

    static void toBytes(ByteBuf buf, LimitedModConfigServer config) {
        buf.writeBoolean(config.allowThrusting());
        buf.writeBoolean(config.forceEnabled());
    }

    static LimitedModConfigServer fromBytes(ByteBuf buf) {
        return new Impl(buf.readBoolean(), buf.readBoolean());
    }

    default JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("allowThrusting", allowThrusting());
        json.addProperty("forceEnabled", forceEnabled());
        return json;
    }

    boolean allowThrusting();

    boolean forceEnabled();

    /** Hand-written stand-in for a record; the mod targets Java 8 bytecode for MinecraftForge. */
    final class Impl implements LimitedModConfigServer {
        private final boolean allowThrusting;
        private final boolean forceEnabled;

        public Impl(boolean allowThrusting, boolean forceEnabled) {
            this.allowThrusting = allowThrusting;
            this.forceEnabled = forceEnabled;
        }

        @Override
        public boolean allowThrusting() {
            return allowThrusting;
        }

        @Override
        public boolean forceEnabled() {
            return forceEnabled;
        }
    }
}
