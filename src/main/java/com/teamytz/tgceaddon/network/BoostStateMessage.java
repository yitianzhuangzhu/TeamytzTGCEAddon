package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.item.ItemElytraJetpack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 加速状态包(客户端 → 服务端)
 *
 * 玩家按下/释放空格时发送,服务端维护"加速中"状态,
 * 用于鞘翅喷气背包的烟花式加速。不依赖科技枪的按键状态同步。
 */
public class BoostStateMessage implements IMessage {

    private boolean boosting;

    public BoostStateMessage() {
    }

    public BoostStateMessage(boolean boosting) {
        this.boosting = boosting;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.boosting = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.boosting);
    }

    public static class Handler implements IMessageHandler<BoostStateMessage, IMessage> {

        @Override
        public IMessage onMessage(BoostStateMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    ItemElytraJetpack.setBoosting(player, message.boosting);
                });
            }
            return null;
        }
    }
}
