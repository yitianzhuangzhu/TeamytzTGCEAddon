package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.item.ItemBlueprint;
import com.teamytz.tgceaddon.item.ItemCannonShell;
import com.teamytz.tgceaddon.item.ItemElytraJetpack;
import com.teamytz.tgceaddon.item.ItemFallShield;
import com.teamytz.tgceaddon.item.ItemFlare;
import com.teamytz.tgceaddon.item.ItemPureMandate;
import com.teamytz.tgceaddon.item.ItemStormShield;
import com.teamytz.tgceaddon.item.ItemTurretCard;
import com.teamytz.tgceaddon.item.ItemTurretUpgrade;
import com.teamytz.tgceaddon.item.weapon.ItemTestPistol;
import com.teamytz.tgceaddon.item.weapon.ItemChainsword;
import com.teamytz.tgceaddon.item.weapon.ItemBolter;
import com.teamytz.tgceaddon.item.weapon.ItemThunderHammer;
import com.teamytz.tgceaddon.item.weapon.ItemPowerSword;
import com.teamytz.tgceaddon.item.weapon.ItemOverloadMissileLauncher;
import com.teamytz.tgceaddon.item.weapon.ItemIRLauncher;
import com.teamytz.tgceaddon.item.weapon.ItemCommandLineMissileLauncher;
import com.teamytz.tgceaddon.item.weapon.ItemFlareLauncher;
import com.teamytz.tgceaddon.entities.projectiles.IRMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.OverloadGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectileNuke;
import techguns.items.guns.ChargedProjectileSelector;
import techguns.items.guns.ProjectileSelector;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品注册类
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class ModItems
{
    public static final List<Item> ITEMS = new ArrayList<>();

    // 测试手枪 - 完全复制科技枪的配置
    public static Item testPistol;
    // 链锯剑 - 套皮电锯
    public static Item chainsword;
    // 爆弹枪
    public static Item bolter;
    // 纯洁圣印 - 用于升级爆弹枪
    public static Item pureMandate;
    // 雷霆锤
    public static Item thunderhammer;
    // 动力剑
    public static Item powersword;
    // 风暴盾牌
    public static Item stormshield;
    // 蓝图物品 - 用于通用复制机
    public static Item blueprint;
    // 炮塔卡片 - 放入炮塔控制器生成炮塔实体
    public static Item turretCard;
    // 机炮炮弹 - BMPT 炮塔机炮弹药，在弹药输入槽被消耗
    public static Item cannonShell;
    // 过载制导导弹发射器 - 发射受过载限制、可被躲避的制导导弹
    public static Item overloadMissileLauncher;
    // 鞘翅喷气背包 - 科技枪背部槽,提供鞘翅飞行+加速+热信号
    public static Item elytraJetpack;
    // 坠落护身符 - Baubles 饰品,减免掉落/撞击伤害
    public static Item fallShield;
    // 指令线制导导弹发射器 - 直接发射,导弹跟随鼠标准星指向飞行
    public static Item commandLineMissileLauncher;
    // 炮塔升级卡 - 放入大型炮塔升级槽(原防护板槽位),提供炮塔升级能力
    public static Item turretUpgrade;
    // 红外热追踪导弹发射器 - 两段式:左键索敌锁定热源,再左键发射
    public static Item irMissileLauncher;
    // 热诱弹 - 消耗品,热诱弹发射器使用,诱骗红外热追踪导弹
    public static Item flare;
    // 热诱弹发射器 - 右键生成热诱弹(200 红外热值),消耗热诱弹
    public static Item flareLauncher;

    public static void init()
    {
        // 注册测试手枪 - 使用addToGunList=true的构造函数，自动添加到科技枪全局列表
        testPistol = registerItem(new ItemTestPistol("test_pistol"));
        // 注册链锯剑
        chainsword = registerItem(new ItemChainsword("chainsword"));
        // 注册爆弹枪
        bolter = registerItem(new ItemBolter("bolter"));
        // 注册纯洁圣印
        pureMandate = registerItem(new ItemPureMandate("pure_mandate"));
        ItemPureMandate.INSTANCE = (ItemPureMandate) pureMandate;
        // 注册雷霆锤
        thunderhammer = registerItem(new ItemThunderHammer("thunderhammer"));
        // 注册动力剑
        powersword = registerItem(new ItemPowerSword("powersword"));
        // 注册风暴盾牌
        stormshield = registerItem(new ItemStormShield("storm_shield"));
        // 注册蓝图物品
        blueprint = registerItem(new ItemBlueprint("blueprint"));
        // 注册炮塔卡片
        turretCard = registerItem(new ItemTurretCard("turret_card"));
        // 注册机炮炮弹
        cannonShell = registerItem(new ItemCannonShell("cannon_shell"));
        // 注册过载制导导弹发射器(锁定发射过载限制导弹)
        ChargedProjectileSelector<OverloadGuidedMissileProjectile> overloadSelector =
                new ChargedProjectileSelector<>(ModAmmoTypes.ROCKET_OVERLOAD,
                        new OverloadGuidedMissileProjectile.Factory());
        overloadMissileLauncher = registerItem(
                new ItemOverloadMissileLauncher("overload_missile_launcher", overloadSelector));
        // 注册鞘翅喷气背包(科技枪背部槽)
        elytraJetpack = registerItem(new ItemElytraJetpack("elytra_jetpack"));
        // 注册坠落护身符(Baubles 饰品)
        fallShield = registerItem(new ItemFallShield("fall_shield"));
        // 注册指令线制导导弹发射器(直接发射,跟随准星;支持核弹变体——装上核火箭即发射核导弹)
        @SuppressWarnings({"unchecked", "rawtypes"})
        ProjectileSelector cmdSelector = new ProjectileSelector(ModAmmoTypes.ROCKETS_WITH_NUKE,
                new CommandLineMissileProjectile.Factory(),
                new CommandLineMissileProjectileNuke.Factory());
        commandLineMissileLauncher = registerItem(
                new ItemCommandLineMissileLauncher("command_line_missile_launcher", cmdSelector));
        // 注册炮塔升级卡(运算卡片等,通过 NBT 区分类型)
        turretUpgrade = registerItem(new ItemTurretUpgrade("turret_upgrade"));
        // 注册红外热追踪导弹发射器(两段式锁定发射)
        ProjectileSelector<IRMissileProjectile> irSelector =
                new ProjectileSelector<>(ModAmmoTypes.ROCKET_OVERLOAD,
                        new IRMissileProjectile.Factory());
        irMissileLauncher = registerItem(
                new ItemIRLauncher("ir_missile_launcher", irSelector));
        // 注册热诱弹(消耗品,诱骗红外导弹)
        flare = registerItem(new ItemFlare("flare"));
        // 注册热诱弹发射器(右键生成热诱弹,消耗热诱弹)
        flareLauncher = registerItem(new ItemFlareLauncher("flare_launcher"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (Item item : ITEMS)
        {
            registry.register(item);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels()
    {
        for (Item item : ITEMS)
        {
            if (item instanceof ItemTestPistol)
            {
                ((ItemTestPistol) item).registerModel();
            }
            else if (item instanceof ItemChainsword)
            {
                ((ItemChainsword) item).registerModel();
            }
            else if (item instanceof ItemBolter)
            {
                ((ItemBolter) item).registerModel();
            }
            else if (item instanceof ItemPureMandate)
            {
                ((ItemPureMandate) item).registerModel();
            }
            else if (item instanceof ItemThunderHammer)
            {
                ((ItemThunderHammer) item).registerModel();
            }
            else if (item instanceof ItemPowerSword)
            {
                ((ItemPowerSword) item).registerModel();
            }
            else if (item instanceof ItemStormShield)
            {
                // 盾牌需要调用 initModel() 来设置原版盾牌的 inventory 模型
                // 实际渲染由 ItemRenderHack 注册的 IItemRenderer 处理
                ((ItemStormShield) item).initModel();
            }
            else if (item instanceof ItemBlueprint)
            {
                // 所有蓝图物品使用同一个模型
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":blueprint", "inventory"));
            }
            else if (item instanceof ItemTurretCard)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":turret_card", "inventory"));
            }
            else if (item instanceof ItemCannonShell)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":cannon_shell", "inventory"));
            }
            else if (item instanceof ItemOverloadMissileLauncher)
            {
                ((ItemOverloadMissileLauncher) item).registerModel();
            }
            else if (item instanceof ItemElytraJetpack)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":elytra_jetpack", "inventory"));
            }
            else if (item instanceof ItemFallShield)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":fall_shield", "inventory"));
            }
            else if (item instanceof ItemCommandLineMissileLauncher)
            {
                ((ItemCommandLineMissileLauncher) item).registerModel();
            }
            else if (item instanceof ItemTurretUpgrade)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":turret_upgrade", "inventory"));
            }
            else if (item instanceof ItemIRLauncher)
            {
                ((ItemIRLauncher) item).registerModel();
            }
            else if (item instanceof ItemFlare)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":flare", "inventory"));
            }
            else if (item instanceof ItemFlareLauncher)
            {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(TGCEAddon.MODID + ":flare_launcher", "inventory"));
            }
        }
    }

    private static <T extends Item> T registerItem(T item)
    {
        ITEMS.add(item);
        return item;
    }
}