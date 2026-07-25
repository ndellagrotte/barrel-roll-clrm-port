package io.github.ndellagrotte.doabarrelroll.config;

import com.google.gson.JsonObject;
import io.github.ndellagrotte.doabarrelroll.DoABarrelRoll;
import io.github.ndellagrotte.doabarrelroll.net.SyncableConfig;
import io.github.ndellagrotte.doabarrelroll.net.ValidatableConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

public record ModConfigServer(boolean allowThrusting,
                              boolean forceEnabled,
                              boolean forceInstalled,
                              int installedTimeout,
                              KineticDamage kineticDamage) implements SyncableConfig<ModConfigServer, LimitedModConfigServer>, LimitedModConfigServer, ValidatableConfig {
    public static final ModConfigServer DEFAULT = new ModConfigServer(
            false, false, false, 40, KineticDamage.VANILLA);

    public static ModConfigServer fromJson(JsonObject json) {
        return new ModConfigServer(
                json.has("allowThrusting") ? json.get("allowThrusting").getAsBoolean() : DEFAULT.allowThrusting(),
                json.has("forceEnabled") ? json.get("forceEnabled").getAsBoolean() : DEFAULT.forceEnabled(),
                json.has("forceInstalled") ? json.get("forceInstalled").getAsBoolean() : DEFAULT.forceInstalled(),
                json.has("installedTimeout") ? json.get("installedTimeout").getAsInt() : DEFAULT.installedTimeout(),
                json.has("kineticDamage") ? KineticDamage.byName(json.get("kineticDamage").getAsString()) : DEFAULT.kineticDamage()
        );
    }

    public JsonObject toJson() {
        var json = new JsonObject();
        json.addProperty("allowThrusting", allowThrusting);
        json.addProperty("forceEnabled", forceEnabled);
        json.addProperty("forceInstalled", forceInstalled);
        json.addProperty("installedTimeout", installedTimeout);
        json.addProperty("kineticDamage", kineticDamage.name());
        return json;
    }

    public static void toBytes(ByteBuf buf, ModConfigServer config) {
        buf.writeBoolean(config.allowThrusting());
        buf.writeBoolean(config.forceEnabled());
        buf.writeBoolean(config.forceInstalled());
        buf.writeInt(config.installedTimeout());
        new PacketBuffer(buf).writeString(config.kineticDamage().name());
    }

    public static ModConfigServer fromBytes(ByteBuf buf) {
        return new ModConfigServer(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readInt(),
                KineticDamage.byName(new PacketBuffer(buf).readString(64))
        );
    }

    @Override
    public Integer getSyncTimeout() {
        return forceInstalled ? installedTimeout : null;
    }

    @Override
    public String getSyncTimeoutMessage() {
        return "Please install Do a Barrel Roll 2.4.0 or later to play on this server.";
    }

    @Override
    public LimitedModConfigServer getLimited(NetHandlerPlayServer handler) {
        return DoABarrelRoll.checkPermission(handler, DoABarrelRoll.MODID + ".ignore_config", 2)
                ? LimitedModConfigServer.OPERATOR : this;
    }

    public static boolean canModify(NetHandlerPlayServer net) {
        return DoABarrelRoll.checkPermission(net, DoABarrelRoll.MODID + ".configure", 3);
    }

    public static boolean canModify(EntityPlayerMP player) {
        return DoABarrelRoll.checkPermission(player, DoABarrelRoll.MODID + ".configure", 3);
    }

    @Override
    public boolean isValid() {
        return installedTimeout > 0;
    }
}
