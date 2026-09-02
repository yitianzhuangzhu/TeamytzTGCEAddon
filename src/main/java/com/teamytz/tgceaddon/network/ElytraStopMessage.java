package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.item.ItemElytraJetpack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 结束鞘翅飞行包(客户端 → 服务端)
 *
 * 玩家手动结束(空中潜行)或落地/进水/骑乘停飞时发送:
 * 服务端同步清除背包鞘翅激活状态与鞘翅飞行标志(Entity flag 7),
 * 保证两端同时停飞,避免服务端残留 flag7 导致客户端"停飞后仍滑翔/漂浮"。
 */
public class ElytraStopMessage implements IMessage {

    public ElytraStopMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<ElytraStopMessage, IMessage> {

        @Override
        public IMessage onMessage(ElytraStopMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    // setActive(false) 内部会清除激活状态 + flag 7
                    ItemElytraJetpack.setActive(player, false);
                });
            }
            return null;
        }
    }
}
