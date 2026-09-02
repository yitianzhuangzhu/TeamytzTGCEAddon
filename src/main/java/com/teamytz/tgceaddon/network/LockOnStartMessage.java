package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.item.weapon.ItemLockOnWeapon;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 索敌开始包(客户端 → 服务端)
 *
 * 玩家第一次左键按下时发送,服务端将手持武器置为"索敌中"状态并开始扫描目标。
 */
public class LockOnStartMessage implements IMessage {

    public LockOnStartMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<LockOnStartMessage, IMessage> {

        @Override
        public IMessage onMessage(LockOnStartMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    ItemStack stack = player.getHeldItemMainhand();
                    if (stack.getItem() instanceof ItemLockOnWeapon) {
                        ((ItemLockOnWeapon) stack.getItem()).startSearch(player);
                    }
                });
            }
            return null;
        }
    }
}
