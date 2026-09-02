package com.teamytz.tgceaddon.client;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import techguns.api.damagesystem.DamageType;
import techguns.client.ShooterValues;
import techguns.items.guns.GenericGun;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 科技枪火药武器开火动态光源(客户端)
 *
 * 原理:科技枪每次开火都会在客户端调用 ShooterValues.setMuzzleFlashTime(player, hand, now, duration),
 * 记录"枪口闪光窗口"的结束时间戳;由于时间单调递增,新值恒大于旧值。
 * 本处理器每客户端 tick 轮询 getMuzzleFlashTime,检测到"值上升"即判定该手刚开火,
 * 在枪口(准星方向)生成短促高亮动态光源(走 OptiFine 动态光源)。
 *
 * 过滤:
 * - 仅火药类武器(伤害类型非 ENERGY / RADIATION / POISON / ICE / LIGHTNING / DARK)
 * - 自研武器(爆弹枪已有自己的闪光,导弹发射器走导弹尾焰光)不重复触发
 *
 * 受配置文件"光效.muzzleFlash"开关控制;未装 OptiFine 时无光效,不影响其他功能。
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID, value = Side.CLIENT)
public class MuzzleFlashLightHandler {

    /** 不产生枪口闪光的非火药伤害类型(能量/辐射/异能类) */
    private static final Set<DamageType> EXCLUDED_DAMAGE_TYPES = new HashSet<>(Arrays.asList(
            DamageType.ENERGY, DamageType.RADIATION, DamageType.POISON,
            DamageType.ICE, DamageType.LIGHTNING, DamageType.DARK));

    /** 上一次记录的主手闪光窗口结束时间戳(ms) */
    private static long prevMuzzleTimeMH = 0;
    /** 上一次记录的副手闪光窗口结束时间戳(ms) */
    private static long prevMuzzleTimeOH = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            prevMuzzleTimeMH = 0;
            prevMuzzleTimeOH = 0;
            return;
        }
        checkHand(player, false); // 主手
        checkHand(player, true);  // 副手
    }

    private static void checkHand(EntityPlayerSP player, boolean offhand) {
        long t = ShooterValues.getMuzzleFlashTime(player, offhand);
        long prev = offhand ? prevMuzzleTimeOH : prevMuzzleTimeMH;
        if (offhand) {
            prevMuzzleTimeOH = t;
        } else {
            prevMuzzleTimeMH = t;
        }
        if (t <= prev) {
            return; // 闪光窗口未更新 = 未开火
        }
        ItemStack stack = offhand ? player.getHeldItemOffhand() : player.getHeldItemMainhand();
        if (!isGunpowderWeapon(stack)) {
            return;
        }
        // 枪口位置:准星方向(与自研爆弹枪一致)
        Vec3d look = player.getLookVec();
        Vec3d pos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ)
                .add(look.scale(0.6D));
        EntityMuzzleLight.spawnFlash(player.world, pos.x, pos.y, pos.z, 15, 4);
    }

    /** 是否为火药类武器(科技枪 GenericGun 且伤害类型非能量/辐射类;自研武器除外) */
    private static boolean isGunpowderWeapon(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof GenericGun)) {
            return false;
        }
        // 自研武器自带光效(爆弹枪闪光 / 导弹尾焰),不重复触发
        if (stack.getItem().getClass().getName().startsWith("com.teamytz.")) {
            return false;
        }
        DamageType type = ((GenericGun) stack.getItem()).getDamageType(stack);
        return type != null && !EXCLUDED_DAMAGE_TYPES.contains(type);
    }
}
