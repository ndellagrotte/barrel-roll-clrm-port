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
        var json = new JsonObject();
        json.addProperty("allowThrusting", allowThrusting());
        json.addProperty("forceEnabled", forceEnabled());
        return json;
    }

    boolean allowThrusting();

    boolean forceEnabled();

    record Impl(boolean allowThrusting, boolean forceEnabled) implements LimitedModConfigServer {
    }
}
