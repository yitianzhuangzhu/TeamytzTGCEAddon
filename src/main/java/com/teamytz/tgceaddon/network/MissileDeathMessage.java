package com.teamytz.tgceaddon.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 导弹死亡同步包
 *
 * 服务端导弹爆炸时发送给追踪玩家,客户端收到后立即将对应实体标记死亡。
 * 解决 1.12.2 实体 destroy 同步不可靠导致的"爆炸动画出现后导弹仍残留渲染"
 * 问题(客户端实体不再依赖服务端 destroy 包,而是主动消失)。
 */
public class MissileDeathMessage implements IMessage {

    private int entityId;

    public MissileDeathMessage() {
    }

    public MissileDeathMessage(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static class Handler implements IMessageHandler<MissileDeathMessage, IMessage> {

        @Override
        public IMessage onMessage(MissileDeathMessage message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    World world = Minecraft.getMinecraft().world;
                    if (world != null) {
                        Entity entity = world.getEntityByID(message.entityId);
                        if (entity != null) {
                            entity.setDead();
                        }
                    }
                });
            }
            return null;
        }
    }
}
