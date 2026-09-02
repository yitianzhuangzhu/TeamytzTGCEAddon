package com.teamytz.tgceaddon.util;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.WeakHashMap;

/**
 * 玩家速度追踪器(服务端)
 *
 * 用"多 tick 指数平滑位移"估算玩家真实速度,供投射物初速继承使用。
 * 为什么不用 player.motion / 单 tick 位移:
 * - 服务端 EntityPlayerMP 的 motion 字段对玩家不可靠(实测水平方向几乎为 0)
 * - 单 tick 位移(pos - lastTickPos)受网络包时序影响,发射瞬间可能读到 0
 * 指数平滑后,单个异常 tick 只占一半权重,反映的是玩家最近的真实移动速度。
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public final class PlayerVelocityTracker {

    /** 指数平滑系数:新采样权重(越大响应越快,越小越平滑) */
    private static final double SMOOTH_FACTOR = 0.5;

    /** 玩家 -> 平滑速度(格/tick) */
    private static final WeakHashMap<EntityPlayer, Vec3d> VELOCITIES = new WeakHashMap<>();

    private PlayerVelocityTracker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.isDead) {
            VELOCITIES.remove(player);
            return;
        }
        double vx = player.posX - player.lastTickPosX;
        double vy = player.posY - player.lastTickPosY;
        double vz = player.posZ - player.lastTickPosZ;
        Vec3d prev = VELOCITIES.get(player);
        if (prev == null) {
            VELOCITIES.put(player, new Vec3d(vx, vy, vz));
        } else {
            VELOCITIES.put(player, new Vec3d(
                    prev.x + (vx - prev.x) * SMOOTH_FACTOR,
                    prev.y + (vy - prev.y) * SMOOTH_FACTOR,
                    prev.z + (vz - prev.z) * SMOOTH_FACTOR));
        }
    }

    /** 获取玩家平滑速度(格/tick);无记录返回 0 */
    public static Vec3d getVelocity(EntityPlayer player) {
        Vec3d v = VELOCITIES.get(player);
        return v != null ? v : Vec3d.ZERO;
    }
}
