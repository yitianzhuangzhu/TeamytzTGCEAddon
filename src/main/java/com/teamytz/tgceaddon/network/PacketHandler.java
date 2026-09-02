package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.TGCEAddon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * 网络包处理器
 */
public class PacketHandler
{
    private static SimpleNetworkWrapper network;
    private static int discriminator = 0;

    public static void init()
    {
        network = new SimpleNetworkWrapper(TGCEAddon.MODID);

        // 导弹死亡同步包(服务端 → 客户端)
        network.registerMessage(MissileDeathMessage.Handler.class, MissileDeathMessage.class, discriminator++, Side.CLIENT);
        // 启动鞘翅飞行包(客户端 → 服务端)
        network.registerMessage(ElytraStartMessage.Handler.class, ElytraStartMessage.class, discriminator++, Side.SERVER);
        // 结束鞘翅飞行包(客户端 → 服务端)
        network.registerMessage(ElytraStopMessage.Handler.class, ElytraStopMessage.class, discriminator++, Side.SERVER);
        // 加速状态包(客户端 → 服务端)
        network.registerMessage(BoostStateMessage.Handler.class, BoostStateMessage.class, discriminator++, Side.SERVER);
        // 锁定武器:索敌开始(客户端 → 服务端)
        network.registerMessage(LockOnStartMessage.Handler.class, LockOnStartMessage.class, discriminator++, Side.SERVER);
        // 锁定武器:锁定状态同步(服务端 → 客户端)
        network.registerMessage(LockStateMessage.Handler.class, LockStateMessage.class, discriminator++, Side.CLIENT);
        // 锁定武器:发射请求(客户端 → 服务端)
        network.registerMessage(IRFireMessage.Handler.class, IRFireMessage.class, discriminator++, Side.SERVER);
    }

    public static void sendToServer(IMessage message)
    {
        network.sendToServer(message);
    }

    public static void sendTo(IMessage message, EntityPlayerMP player)
    {
        network.sendTo(message, player);
    }

    public static void sendToAll(IMessage message)
    {
        network.sendToAll(message);
    }

    public static void sendToAllAround(IMessage message, net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint point)
    {
        network.sendToAllAround(message, point);
    }

    public static void sendToDimension(IMessage message, int dimensionId)
    {
        network.sendToDimension(message, dimensionId);
    }
}