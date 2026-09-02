package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.item.ItemElytraJetpack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Method;

/**
 * 启动鞘翅飞行包(客户端 → 服务端)
 *
 * 玩家佩戴鞘翅喷气背包时,在空中按下空格键发送本包:
 * 服务端标记背包鞘翅激活,并设置鞘翅飞行标志(Entity flag 7)启动原版鞘翅飞行。
 * 注:Entity.setFlag 用反射 + 运行时 SRG 名(func_70052_a)调用,不依赖 AT。
 */
public class ElytraStartMessage implements IMessage {

    public ElytraStartMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<ElytraStartMessage, IMessage> {

        @Override
        public IMessage onMessage(ElytraStartMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    // 空中且未飞行时才启动
                    if (!player.onGround && !player.isElytraFlying()) {
                        // 标记背包鞘翅激活(每 tick 保持 flag 7)
                        ItemElytraJetpack.setActive(player, true);
                        try {
                            Method setFlag = Entity.class.getDeclaredMethod("func_70052_a", int.class, boolean.class);
                            setFlag.setAccessible(true);
                            setFlag.invoke(player, 7, true);
                        } catch (Exception e) {
                            com.teamytz.tgceaddon.TGCEAddon.getLogger().error("鞘翅喷气背包:启动鞘翅飞行失败", e);
                        }
                    }
                });
            }
            return null;
        }
    }
}

