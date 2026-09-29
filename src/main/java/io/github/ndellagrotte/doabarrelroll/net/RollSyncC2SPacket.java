package io.github.ndellagrotte.doabarrelroll.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client to server: the sending player's roll. The server applies it with {@code Entity.setRoll}, and
 * the loader syncs it to every tracking client through {@code Entity.ROLL}.
 */
public class RollSyncC2SPacket implements IMessage {
    private boolean rolling;
    private float roll;

    public RollSyncC2SPacket() {
    }

    public RollSyncC2SPacket(boolean rolling, float roll) {
        this.rolling = rolling;
        this.roll = roll;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        rolling = buf.readBoolean();
        roll = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(rolling);
        buf.writeFloat(roll);
    }

    public static class Handler implements IMessageHandler<RollSyncC2SPacket, IMessage> {
        @Override
        public IMessage onMessage(RollSyncC2SPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            // setRoll discards non-finite values and wraps the rest to [-180, 180)
            float roll = message.rolling ? message.roll : 0.0f;
            player.getServerWorld().addScheduledTask(() -> player.setRoll(roll));
            return null;
        }
    }
}
