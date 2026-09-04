package com.teamytz.tgceaddon.compat;

import com.fantasticsource.dynamicstealth.server.HelperSystem;
import com.fantasticsource.dynamicstealth.server.ai.AIDynamicStealth;
import com.fantasticsource.dynamicstealth.server.senses.sight.Sight;
import com.fantasticsource.dynamicstealth.server.threat.EntityThreatData;
import com.fantasticsource.dynamicstealth.server.threat.Threat;
import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.config.ConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.guns.GenericGun;

import java.util.HashMap;
import java.util.Map;

/**
 * DynamicStealth 适配:开枪惊动敌对生物
 *
 * 问题:DynamicStealth 的听觉(Hearing.canHear)只是距离/听力属性检查,不响应任意播放的
 * 枪声——怪物警觉是威胁事件驱动(被攻击/同伴报警/目击)。因此玩家开枪时,科技枪/本模组
 * 发射的火药弹丸本不会惊动任何怪物。
 *
 * 本适配器:服务器上检测到"玩家刚发射的火药弹丸"(techguns GenericProjectile 生成)即视为
 * 一次开枪,对听觉半径内、与该玩家敌对(非友军、非纯被动)的怪物施加 DynamicStealth 威胁:
 * - 能看见开枪者的怪物 → 被锁定为攻击目标(直接警戒攻击)
 * - 看不见的怪物 → 威胁等级提升 + 循声走向开枪位置查看
 *
 * 需安装 DynamicStealth(含依赖 FantasticLib);未安装时本功能静默失效。
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class GunshotAlertHandler {

    /** 玩家 UUID -> 上次开枪惊动 tick(连续射击限频用) */
    private static final Map<String, Long> LAST_ALERT = new HashMap<>();

    /** 近身持枪者判定范围平方(弹丸从枪口生成,距射手约 1-2 格) */
    private static final double SHOOTER_RANGE_SQ = 4.0 * 4.0;

    @SubscribeEvent
    public static void onProjectileSpawn(EntityJoinWorldEvent event) {
        if (!Loader.isModLoaded("dynamicstealth")) {
            return;
        }
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof GenericProjectile)) {
            return;
        }
        // 仅"刚发射"的弹丸(排除区块重载时旧弹丸重新加入世界)
        if (entity.ticksExisted > 1) {
            return;
        }

        EntityPlayer shooter = findGunShooter(world, entity);
        if (shooter == null || shooter.isDead) {
            return;
        }

        // 连续射击限频:同一玩家间隔 rateLimit tick 才再次惊动
        long now = world.getTotalWorldTime();
        String key = shooter.getCachedUniqueIdString();
        Long last = LAST_ALERT.get(key);
        if (last != null && now - last < ConfigHandler.gunshotRateLimit) {
            return;
        }
        LAST_ALERT.put(key, now);

        double radius = ConfigHandler.getGunAlertRadius(gunName(shooter));
        if (radius <= 0) {
            return; // 该枪警示半径 0 = 消音/不惊动
        }
        alertHostiles(shooter, radius);
    }

    /** 找发射者:弹丸附近最近、且手持火药枪械(GenericGun)的玩家 */
    private static EntityPlayer findGunShooter(World world, Entity projectile) {
        EntityPlayer best = null;
        double bestD = SHOOTER_RANGE_SQ;
        for (EntityPlayer p : world.playerEntities) {
            if (p.isDead || !isHoldingGun(p)) {
                continue;
            }
            double d = p.getDistanceSq(projectile);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    private static boolean isHoldingGun(EntityPlayer p) {
        return isGun(p.getHeldItemMainhand()) || isGun(p.getHeldItemOffhand());
    }

    private static boolean isGun(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof GenericGun;
    }

    /** 射手手持的枪械注册名(用于查该枪警示半径) */
    private static String gunName(EntityPlayer p) {
        if (isGun(p.getHeldItemMainhand())) {
            return p.getHeldItemMainhand().getItem().getRegistryName().toString();
        }
        if (isGun(p.getHeldItemOffhand())) {
            return p.getHeldItemOffhand().getItem().getRegistryName().toString();
        }
        return null;
    }

    /**
     * 惊动听觉半径内的敌对生物:
     * - 能看见开枪者 → Threat.apply(GEN_WARNED, 看见=true):未在战斗中的会锁定开枪者为目标
     * - 看不见 → 威胁等级提升 + AIDynamicStealth 循声走向开枪位置
     */
    private static void alertHostiles(EntityPlayer shooter, double radius) {
        World world = shooter.world;
        double r2 = radius * radius;
        BlockPos shotPos = new BlockPos(shooter.posX, shooter.posY, shooter.posZ);
        for (Entity e : world.loadedEntityList) {
            if (!(e instanceof EntityLiving) || e == shooter || !e.isEntityAlive()) {
                continue;
            }
            EntityLiving mob = (EntityLiving) e;
            try {
                if (EntityThreatData.bypassesThreat(mob)) {
                    continue;
                }
                if (EntityThreatData.isPassive(mob)) {
                    continue; // 无攻击能力的纯被动生物不惊动
                }
                if (HelperSystem.isAlly(mob, shooter)) {
                    continue; // 友军/宠物不惊动
                }
                if (mob.getDistanceSq(shooter) > r2) {
                    continue; // 超出听觉半径
                }
                boolean sees = Sight.canSee(mob, shooter, true);
                Threat.apply(mob, shooter, ConfigHandler.gunshotAlertThreat,
                        Threat.THREAT_TYPE.GEN_WARNED, sees);
                // 循声查看:让 DS AI 走向开枪位置(看见则直接锁定攻击)
                AIDynamicStealth ai = AIDynamicStealth.getStealthAI(mob);
                if (ai != null && !ai.isFleeing()) {
                    ai.lastKnownPosition = shotPos;
                    ai.restart(shotPos);
                }
            } catch (Throwable t) {
                // 个别自定义生物异常不中断整次扫描
            }
        }
    }
}
