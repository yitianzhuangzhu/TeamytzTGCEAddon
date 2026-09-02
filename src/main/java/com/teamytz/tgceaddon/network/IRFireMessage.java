package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.item.weapon.ItemLockOnWeapon;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 发射请求包(客户端 → 服务端)
 *
 * 锁定完成后玩家再次左键时发送,服务端消耗弹药并创建对应投射物。
 */
public class IRFireMessage implements IMessage {

    public IRFireMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<IRFireMessage, IMessage> {

        @Override
        public IMessage onMessage(IRFireMessage message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    ItemStack stack = player.getHeldItemMainhand();
                    if (stack.getItem() instanceof ItemLockOnWeapon) {
                        ItemLockOnWeapon weapon = (ItemLockOnWeapon) stack.getItem();
                        weapon.fireRequested(player, stack);
                    }
                });
            }
            return null;
        }
    }
}
