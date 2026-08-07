package com.teamytz.tgceaddon.proxy;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.armor.ModelStormShield;
import com.teamytz.tgceaddon.client.render.entities.projectiles.RenderBoltProjectile;
import com.teamytz.tgceaddon.client.render.item.RenderBolter;
import com.teamytz.tgceaddon.client.render.item.RenderChainsword;
import com.teamytz.tgceaddon.client.render.item.RenderThunderHammer;
import com.teamytz.tgceaddon.client.render.item.RenderPowerSword;
import com.teamytz.tgceaddon.client.render.item.RenderStormShield;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import com.teamytz.tgceaddon.init.ModItems;
import com.teamytz.tgceaddon.item.ItemStormShield;
import com.teamytz.tgceaddon.item.weapon.ItemChainsword;
import com.teamytz.tgceaddon.item.weapon.ItemThunderHammer;
import com.teamytz.tgceaddon.item.weapon.ItemPowerSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.client.render.ItemRenderHack;
import techguns.client.render.item.RenderItemBase;
import techguns.client.render.fx.ScreenEffect;

@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        // registerEntityRenderers() 在父类中被调用
    }
    
    @Override
    protected void registerEntityRenderers() {
        TGCEAddon.getLogger().info("注册实体渲染器...");
        // 注册爆弹投射物渲染器
        RenderingRegistry.registerEntityRenderingHandler(BoltProjectile.class, RenderBoltProjectile::new);
        TGCEAddon.getLogger().info("实体渲染器注册完成");
    }
    
    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);  // 调用父类的 init，它会调用 registerItemRenderers()
    }
    
    /**
     * 在 ModelRegistryEvent 期间注册所有物品模型
     * ModelLoader.setCustomModelResourceLocation 必须在此事件期间调用
     */
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModItems.registerModels();
    }
    
    @Override
    protected void registerItemRenderers() {
        ItemRenderHack.registerItemRenderer(ItemChainsword.INSTANCE,
                new RenderChainsword().setBaseTranslation(0.0f, 0.1f, RenderItemBase.SCALE - 0.09f)
                        .setBaseScale(0.95f).setGUIScale(0.45f).setTransformTranslations(new float[][]{
                                {0.2f, 0.1f, 0.05f}, //第三人称的参数
                                {0.1f, 0.1f, 0.04f},
                                {0.03f, 0.01f, 0f},
                                {0f, 0f, 0f},
                                {-0.07f, -0.03f, -0.11f}
                        }));
        
        ItemRenderHack.registerItemRenderer(ModItems.bolter,
                new RenderBolter().setBaseTranslation(RenderItemBase.SCALE * 0.5f, -0.1f, 0)
                        .setBaseScale(1.0f).setGUIScale(0.45f).setTransformTranslations(new float[][]{
                                {0f, -0.08f, 0f}, //第一人称的参数
                                {0f, 0f, 0.04f},
                                {0.03f, 0.01f, 0f},
                                {0f, 0f, 0f},
                                {-0.07f, -0.03f, -0.11f}
                        })
                        .setScope(ScreenEffect.sniperScope));
        
        // 注册雷霆锤渲染器
        ItemRenderHack.registerItemRenderer(ItemThunderHammer.INSTANCE,
                new RenderThunderHammer().setBaseTranslation(0.0f, 0.1f, RenderItemBase.SCALE - 0.09f)
                        .setBaseScale(0.95f).setGUIScale(0.45f).setTransformTranslations(new float[][]{
                                {0.2f, 0.1f, 0.05f}, //第三人称的参数
                                {0.1f, 0.1f, 0.04f},
                                {0.03f, 0.01f, 0f},
                                {0f, 0f, 0f},
                                {-0.07f, -0.03f, -0.11f}
                        }));
        
        // 注册动力剑渲染器（单手武器，第一人称位置上移）
        ItemRenderHack.registerItemRenderer(ItemPowerSword.INSTANCE,
                new RenderPowerSword().setBaseTranslation(0.0f, 0.1f, RenderItemBase.SCALE - 0.09f)
                        .setBaseScale(0.95f).setGUIScale(0.45f).setTransformTranslations(new float[][]{
                                {0.1f, 0.4f, 0.04f}, //第一人称参数（index 0），Y值增大使模型往上移
                                {0.05f, 0.1f, 0.05f}, //第三人称参数（index 1），0.2f和-0.1f的中间值
                                {0.03f, 0.01f, 0f},
                                {0f, 0f, 0f},
                                {-0.07f, -0.03f, -0.11f}
                        }));

        // 注册风暴盾牌渲染器（使用 IItemRenderer 接口，支持举盾时显示 forcefield）
        ItemRenderHack.registerItemRenderer(ItemStormShield.INSTANCE,
            new RenderStormShield(new ModelStormShield(),
                new ResourceLocation(TGCEAddon.MODID, "textures/armors/storm_shield.png")));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }
}
