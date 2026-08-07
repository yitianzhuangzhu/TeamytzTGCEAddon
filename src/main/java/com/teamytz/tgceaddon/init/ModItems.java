package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.item.ItemBlueprint;
import com.teamytz.tgceaddon.item.ItemPureMandate;
import com.teamytz.tgceaddon.item.ItemStormShield;
import com.teamytz.tgceaddon.item.weapon.ItemTestPistol;
import com.teamytz.tgceaddon.item.weapon.ItemChainsword;
import com.teamytz.tgceaddon.item.weapon.ItemBolter;
import com.teamytz.tgceaddon.item.weapon.ItemThunderHammer;
import com.teamytz.tgceaddon.item.weapon.ItemPowerSword;
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
        }
    }

    private static <T extends Item> T registerItem(T item)
    {
        ITEMS.add(item);
        return item;
    }
}