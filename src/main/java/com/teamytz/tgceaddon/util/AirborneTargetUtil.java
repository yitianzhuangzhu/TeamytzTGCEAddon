package com.teamytz.tgceaddon.util;

import com.teamytz.tgceaddon.config.ConfigHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.EntityRegistry;

/**
 * 空中目标通用判定(罗兰防空炮塔索敌用)
 *
 * 判定顺序:
 * 1. 配置文件 airborneEntityIds 名单 → 强制空中
 * 2. 配置文件 groundEntityIds 名单 → 强制地面(排除)
 * 3. 通用判定(可配置开关):
 *    - EntityFlying 子类(原版恶魂 / 科技枪直升机 GenericFlyingMob / 任意模组飞行生物)
 *    - 原版经典飞行者:末影龙、凋零
 *    - isFlying 标志(flag 16,部分模组使用)
 *    - 离地高度检测(空中且离地超过阈值,覆盖任意模组会飞的生物)
 */
public final class AirborneTargetUtil {

    /** 离地多少格以上视为"空中目标"(格) */
    public static final double AIRBORNE_HEIGHT = 5.0D;
    /** 向下探测地面最大深度(格) */
    private static final int GROUND_SEARCH_DEPTH = 48;

    private AirborneTargetUtil() {
    }

    /** 判断实体是否为空中目标(供罗兰防空炮塔索敌/开火判定) */
    public static boolean isAirborne(EntityLivingBase target) {
        String id = getEntityId(target);
        // 1) 配置文件名单优先
        if (ConfigHandler.isForceAirborne(id)) {
            return true;
        }
        if (ConfigHandler.isForceGround(id)) {
            return false;
        }
        // 2) 通用判定(可配置关闭)
        if (ConfigHandler.genericAirborneCheck) {
            // EntityFlying 子类:原版恶魂 / 科技枪直升机 / 模组飞行生物
            if (target instanceof EntityFlying) {
                return true;
            }
            // 原版经典飞行者
            if (target instanceof EntityDragon || target instanceof EntityWither || target instanceof EntityGhast) {
                return true;
            }
            // 通用离地高度检测:不在水面/不骑乘/不着地,且离地超过阈值
            if (!target.onGround && !target.isInWater() && !target.isRiding()) {
                return heightAboveGround(target) >= AIRBORNE_HEIGHT;
            }
        }
        return false;
    }

    /** 实体下方最近地面到实体的距离(格);下方 GROUND_SEARCH_DEPTH 内无地面返回大值(视为高空) */
    private static double heightAboveGround(Entity target) {
        BlockPos pos = new BlockPos(target.posX, target.posY, target.posZ);
        int startY = MathHelper.floor(target.posY) - 1;
        for (int y = startY; y > startY - GROUND_SEARCH_DEPTH; y--) {
            IBlockState state = target.world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
            if (state.isFullBlock()) {
                return target.posY - (y + 1);
            }
        }
        return GROUND_SEARCH_DEPTH;
    }

    /** 实体注册 ID(modid:entityname);未注册返回空字符串 */
    private static String getEntityId(Entity entity) {
        ResourceLocation key = net.minecraft.entity.EntityList.getKey(entity.getClass());
        return key != null ? key.toString() : "";
    }
}
