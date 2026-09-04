package com.teamytz.tgceaddon.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * tgceaddon 配置文件(config/tgceaddon.cfg)
 *
 * 目前用于:
 * - 罗兰防空炮塔的"空中目标"判定名单:
 *   - airborneEntityIds:强制视为空中目标的实体 ID(即使通用判定没认出也锁定)
 *   - groundEntityIds:  强制视为地面目标的实体 ID(即使通用判定认为在空中也排除)
 *   - genericAirborneCheck:是否启用通用空中判定(EntityFlying 子类 / 离地高度检测)
 * - 光效设置(枪口闪烁 / 导弹持续光效,走 OptiFine 动态光源;可分别开关)
 * - 红外设置(实体红外热信号数值,红外导弹可锁定的目标)
 */
public class ConfigHandler {

    private static final String CATEGORY_LIGHT = "光效";
    private static final String CATEGORY_IR = "红外";
    private static final String CATEGORY_GUN_ALERT = "枪声警觉";

    private static Configuration config;

    /** 强制视为空中目标的实体 ID 列表(格式 modid:entityname) */
    public static String[] airborneEntityIds = new String[0];
    /** 强制视为地面目标的实体 ID 列表 */
    public static String[] groundEntityIds = new String[0];
    /** 是否启用通用空中判定 */
    public static boolean genericAirborneCheck = true;
    /** 枪口闪烁光效(爆弹枪/BMPT 机炮/导弹炮口闪光);默认关闭 */
    public static boolean muzzleFlashEnabled = false;
    /** 导弹持续光效(动力段尾焰光) */
    public static boolean missileLightEnabled = true;
    /** 实体红外热信号数值列表(格式 modid:entityname:热值) */
    public static String[] entityHeatValues = new String[0];
    /** 未在列表中的实体的默认红外热值 */
    public static float defaultEntityHeat = 0.0f;
    /** 开枪惊动敌对生物的默认警示半径(格) */
    public static float gunshotAlertRadius = 32.0f;
    /** 按枪械单独配置警示半径(格式 modid:item:半径) */
    public static String[] gunshotAlertRadii = new String[0];
    /** 连续射击时同一玩家两次惊动的最小间隔(tick) */
    public static int gunshotRateLimit = 20;
    /** 开枪惊动生物时施加的威胁量 */
    public static float gunshotAlertThreat = 50.0f;

    /** 按枪械警示半径表(懒加载;小写,支持省略命名空间) */
    private static java.util.Map<String, Float> gunRadiusMap = null;

    public static void init(FMLPreInitializationEvent event) {
        File file = new File(event.getModConfigurationDirectory(), "tgceaddon.cfg");
        config = new Configuration(file);
        config.load();

        airborneEntityIds = config.getStringList("airborneEntityIds", Configuration.CATEGORY_GENERAL,
                new String[]{"minecraft:ender_dragon", "minecraft:wither"},
                "强制视为空中目标的实体 ID 列表(modid:entityname)");
        groundEntityIds = config.getStringList("groundEntityIds", Configuration.CATEGORY_GENERAL,
                new String[0],
                "强制视为地面目标的实体 ID 列表(modid:entityname)");
        genericAirborneCheck = config.getBoolean("genericAirborneCheck", Configuration.CATEGORY_GENERAL,
                true,
                "启用通用空中目标判定");

        // ===== 光效设置 =====
        // 动态光源走 OptiFine 动态光源(需在 OptiFine 视频设置中开启"动态光源");
        // 未装 OptiFine 或关闭对应开关时无光效,不影响其他功能。
        muzzleFlashEnabled = config.getBoolean("muzzleFlash", CATEGORY_LIGHT,
                false,
                "开启火药武器的枪口闪光");
        missileLightEnabled = config.getBoolean("missileLight", CATEGORY_LIGHT,
                true,
                "开启导弹的持续光效");

        // ===== 红外设置 =====
        entityHeatValues = config.getStringList("entityHeatValues", CATEGORY_IR,
                new String[]{"minecraft:ender_dragon:150", "minecraft:wither:120", "techguns:AttackHelicopter:80"},
                "实体红外热值列表(格式 modid:entityname:热值)");
        defaultEntityHeat = config.getFloat("defaultEntityHeat", CATEGORY_IR,
                0.0f, 0.0f, 1000.0f,
                "未列表实体的默认红外热值(0=无红外信号)");

        // ===== 枪声警觉设置(DynamicStealth 适配) =====
        gunshotAlertRadius = config.getFloat("alertRadius", CATEGORY_GUN_ALERT,
                32.0f, 0.0f, 500.0f,
                "开枪惊动敌对生物的默认警示半径(格)");
        gunshotAlertRadii = config.getStringList("alertRadiusPerGun", CATEGORY_GUN_ALERT,
                new String[]{"techguns:m4_infiltrator:4"},
                "按枪械单独配置警示半径(格式 注册名:半径,如 techguns:m4_infiltrator:4;0=不惊动)");
        gunshotRateLimit = config.getInt("rateLimit", CATEGORY_GUN_ALERT,
                20, 1, 200,
                "连续射击时同一玩家两次惊动的最小间隔(tick)");
        gunshotAlertThreat = config.getFloat("alertThreat", CATEGORY_GUN_ALERT,
                50.0f, 0.0f, 200.0f,
                "开枪惊动生物时施加的威胁量");

        config.save();

        // 加载实体红外热值表(供 HeatSourceManager 查询)
        com.teamytz.tgceaddon.tracking.HeatSourceManager.loadConfigEntityHeat();
        gunRadiusMap = null;
    }

    /** 某枪械(item 注册名)的开枪警示半径:先查单独配置,否则用默认 */
    public static float getGunAlertRadius(String itemRegistryName) {
        if (gunRadiusMap == null) {
            gunRadiusMap = new java.util.HashMap<>();
            for (String entry : gunshotAlertRadii) {
                if (entry == null) {
                    continue;
                }
                String entryStr = entry.trim();
                // 条目格式 "item注册名:半径",例如 "techguns:m4_infiltrator:4"。
                // 注意枪械注册名本身带冒号(modid:item),因此"半径"取最后一个冒号之后、
                // 冒号之前(整段,含 modid)才是注册名。
                int lastColon = entryStr.lastIndexOf(':');
                if (lastColon > 0 && lastColon < entryStr.length() - 1) {
                    try {
                        String gunKey = entryStr.substring(0, lastColon).trim().toLowerCase();
                        float radiusVal = Float.parseFloat(entryStr.substring(lastColon + 1).trim());
                        gunRadiusMap.put(gunKey, radiusVal);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (itemRegistryName == null) {
            return gunshotAlertRadius;
        }
        String lower = itemRegistryName.toLowerCase();
        Float v = gunRadiusMap.get(lower);
        if (v != null) {
            return v;
        }
        int idx = lower.indexOf(':');
        if (idx >= 0) {
            v = gunRadiusMap.get(lower.substring(idx + 1));
            if (v != null) {
                return v;
            }
        }
        return gunshotAlertRadius;
    }

    /** 实体 ID 是否在"强制空中"名单中 */
    public static boolean isForceAirborne(String entityId) {
        return contains(airborneEntityIds, entityId);
    }

    /** 实体 ID 是否在"强制地面"名单中 */
    public static boolean isForceGround(String entityId) {
        return contains(groundEntityIds, entityId);
    }

    /** 匹配实体 ID(允许省略命名空间:ender_dragon 可匹配 minecraft:ender_dragon) */
    private static boolean contains(String[] list, String id) {
        for (String s : list) {
            if (s == null || s.isEmpty()) {
                continue;
            }
            String entry = s.trim();
            if (entry.equalsIgnoreCase(id)) {
                return true;
            }
            int idx = id.indexOf(':');
            if (idx >= 0 && entry.equalsIgnoreCase(id.substring(idx + 1))) {
                return true;
            }
        }
        return false;
    }
}
