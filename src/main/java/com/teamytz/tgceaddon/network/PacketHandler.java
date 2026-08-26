package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.TGCEAddon;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * 网络包处理器
 */
public class PacketHandler
{
    private static SimpleNetworkWrapper network;

    public static void init()
    {
        network = new SimpleNetworkWrapper(TGCEAddon.MODID);

        // 当前没有需要注册的网络包
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