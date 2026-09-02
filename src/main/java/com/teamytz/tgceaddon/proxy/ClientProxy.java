package com.teamytz.tgceaddon.proxy;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.client.models.armor.ModelStormShield;
import com.teamytz.tgceaddon.client.render.entities.RenderBMPTTurret;
import com.teamytz.tgceaddon.client.render.entities.RenderFlare;
import com.teamytz.tgceaddon.client.render.entities.RenderMuzzleLight;
import com.teamytz.tgceaddon.client.render.entities.RenderRolandTurret;
import com.teamytz.tgceaddon.client.render.entities.projectiles.RenderBoltProjectile;
import com.teamytz.tgceaddon.client.render.entities.projectiles.RenderCannonShellProjectile;
import com.teamytz.tgceaddon.client.render.entities.projectiles.RenderRolandNukeMissile;
import com.teamytz.tgceaddon.client.render.item.RenderBolter;
import com.teamytz.tgceaddon.client.render.item.RenderChainsword;
import com.teamytz.tgceaddon.client.render.item.RenderThunderHammer;
import com.teamytz.tgceaddon.client.render.item.RenderPowerSword;
import com.teamytz.tgceaddon.client.render.item.RenderStormShield;
import com.teamytz.tgceaddon.entities.EntityBMPTTurret;
import com.teamytz.tgceaddon.entities.EntityFlare;
import com.teamytz.tgceaddon.entities.EntityMuzzleLight;
import com.teamytz.tgceaddon.entities.EntityRolandTurret;
import com.teamytz.tgceaddon.entities.projectiles.BMPTGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.BoltProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CannonShellProjectile;
import com.teamytz.tgceaddon.entities.projectiles.OverloadGuidedMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.IRMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectile;
import com.teamytz.tgceaddon.entities.projectiles.RolandMissileProjectileNuke;
import com.teamytz.tgceaddon.entities.projectiles.CommandLineMissileProjectileNuke;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import com.teamytz.tgceaddon.item.weapon.ItemLockOnWeapon;
import techguns.client.render.ItemRenderHack;
import techguns.client.render.item.GunAnimation;
import techguns.client.render.item.RenderItemBase;
import techguns.client.render.item.RenderGunBase90;
import techguns.client.render.fx.ScreenEffect;
import techguns.client.render.entities.projectiles.RenderRocketProjectile;
import techguns.client.models.guns.ModelGuidedMissileLauncher;
import techguns.client.render.AdditionalSlotRenderRegistry;
import techguns.client.render.RenderAdditionalSlotItem;
import com.teamytz.tgceaddon.client.models.armor.ModelElytraJetpack;

@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        // 注册炮塔基座 TESR（master 形成后渲染炮塔底座大模型）
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.teamytz.tgceaddon.tileentities.TurretBaseTileEntMaster.class,
                new com.teamytz.tgceaddon.client.render.tileentities.RenderTurretBase());
        // registerEntityRenderers() 在父类中被调用
    }
    
    @Override
    protected void registerEntityRenderers() {
        TGCEAddon.getLogger().info("注册实体渲染器...");
        // 注册爆弹投射物渲染器
        RenderingRegistry.registerEntityRenderingHandler(BoltProjectile.class, RenderBoltProjectile::new);
        // 注册机炮炮弹投射物渲染器
        RenderingRegistry.registerEntityRenderingHandler(CannonShellProjectile.class, RenderCannonShellProjectile::new);
        // 注册 BMPT 炮塔实体渲染器
        RenderingRegistry.registerEntityRenderingHandler(EntityBMPTTurret.class, RenderBMPTTurret::new);
        // 注册过载制导导弹渲染器(复用科技枪火箭渲染)
        RenderingRegistry.registerEntityRenderingHandler(OverloadGuidedMissileProjectile.class, RenderRocketProjectile::new);
        // 注册指令线制导导弹渲染器(复用科技枪火箭渲染)
        RenderingRegistry.registerEntityRenderingHandler(CommandLineMissileProjectile.class, RenderRocketProjectile::new);
        // 注册红外热追踪导弹渲染器(复用科技枪火箭渲染)
        RenderingRegistry.registerEntityRenderingHandler(IRMissileProjectile.class, RenderRocketProjectile::new);
        // 注册罗兰防空炮塔实体渲染器
        RenderingRegistry.registerEntityRenderingHandler(EntityRolandTurret.class, RenderRolandTurret::new);
        // 注册罗兰指令线导弹渲染器(复用科技枪火箭渲染)
        RenderingRegistry.registerEntityRenderingHandler(RolandMissileProjectile.class, RenderRocketProjectile::new);
        // 注册罗兰核导弹渲染器(科技枪核火箭贴图)
        RenderingRegistry.registerEntityRenderingHandler(RolandMissileProjectileNuke.class, RenderRolandNukeMissile::new);
        // 注册玩家指令线核导弹渲染器(科技枪核火箭贴图)
        RenderingRegistry.registerEntityRenderingHandler(CommandLineMissileProjectileNuke.class, RenderRolandNukeMissile::new);
        // 注册 BMPT 制导导弹渲染器(复用科技枪火箭渲染)
        RenderingRegistry.registerEntityRenderingHandler(BMPTGuidedMissileProjectile.class, RenderRocketProjectile::new);
        // 注册动态光源实体渲染器(不渲染任何东西,只作为 OptiFine 动态光源载体)
        RenderingRegistry.registerEntityRenderingHandler(EntityMuzzleLight.class, RenderMuzzleLight::new);
        // 注册热诱弹渲染器(火焰贴图十字四边形)
        RenderingRegistry.registerEntityRenderingHandler(EntityFlare.class, RenderFlare::new);
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
                        .setScope(ScreenEffect.sniperScope)
                        .setScopeRecoilAnim(GunAnimation.scopeRecoil, 0.10f, 1.5f));
        
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

        // 注册过载制导导弹发射器渲染器（复用科技枪导弹发射器模型与配置）
        ItemRenderHack.registerItemRenderer(ModItems.overloadMissileLauncher,
                new RenderGunBase90(new ModelGuidedMissileLauncher(), 1)
                        .setBaseTranslation(-0.4f, -0.2f, RenderItemBase.SCALE * 0.5f)
                        .setGUIScale(0.35f).setChargeTranslationAmount(0)
                        .setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.39f, -0.6f, 0.87f, 0)
                        .setTransformTranslations(new float[][]{
                                {-0.13f, 0.3f, 0.62f}, //第一人称的参数
                                {0f, 0.09f, 0.28f},
                                {0.0f, 0.03f, 0.0f},
                                {0.0f, 0.0f, 0f},
                                {0f, 0f, -0.04f}
                        })
                        .setMuzzleFXPos3P(0.09f, -0.26f));

        // 注册指令线制导导弹发射器渲染器(复用科技枪导弹发射器模型与配置)
        ItemRenderHack.registerItemRenderer(ModItems.commandLineMissileLauncher,
                new RenderGunBase90(new ModelGuidedMissileLauncher(), 1)
                        .setBaseTranslation(-0.4f, -0.2f, RenderItemBase.SCALE * 0.5f)
                        .setGUIScale(0.35f).setChargeTranslationAmount(0)
                        .setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.39f, -0.6f, 0.87f, 0)
                        .setTransformTranslations(new float[][]{
                                {-0.13f, 0.3f, 0.62f}, //第一人称的参数
                                {0f, 0.09f, 0.28f},
                                {0.0f, 0.03f, 0.0f},
                                {0.0f, 0.0f, 0f},
                                {0f, 0f, -0.04f}
                        })
                        .setMuzzleFXPos3P(0.09f, -0.26f));

        // 注册红外热追踪导弹发射器渲染器(复用科技枪导弹发射器模型与配置)
        ItemRenderHack.registerItemRenderer(ModItems.irMissileLauncher,
                new RenderGunBase90(new ModelGuidedMissileLauncher(), 1)
                        .setBaseTranslation(-0.4f, -0.2f, RenderItemBase.SCALE * 0.5f)
                        .setGUIScale(0.35f).setChargeTranslationAmount(0)
                        .setMuzzleFx(ScreenEffect.muzzleFlash_gun, 0, 0.39f, -0.6f, 0.87f, 0)
                        .setTransformTranslations(new float[][]{
                                {-0.13f, 0.3f, 0.62f}, //第一人称的参数
                                {0f, 0.09f, 0.28f},
                                {0.0f, 0.03f, 0.0f},
                                {0.0f, 0.0f, 0f},
                                {0f, 0f, -0.04f}
                        })
                        .setMuzzleFXPos3P(0.09f, -0.26f));

        // 注册鞘翅喷气背包背部槽渲染(自定义模型,粒子喷口位置针对鞘翅飞行姿态修正)
        AdditionalSlotRenderRegistry.register(ModItems.elytraJetpack,
                new RenderAdditionalSlotItem(new ModelElytraJetpack(0), "techguns", "textures/armors/jetpack", 4));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    /**
     * 锁定武器瞄准框(屏幕中央方框):
     * - 索敌中:白色方框闪烁
     * - 已锁定:红色方框(不再闪烁)
     */
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getHeldItemMainhand();
        if (!(stack.getItem() instanceof ItemLockOnWeapon)) {
            return;
        }
        ItemLockOnWeapon.LockState state = ItemLockOnWeapon.getState(player);
        if (!state.searching && !state.locked) {
            return;
        }
        ScaledResolution res = event.getResolution();
        int cx = res.getScaledWidth() / 2;
        int cy = res.getScaledHeight() / 2;
        int size = 18;

        int color;
        if (state.locked) {
            color = 0xFFFF3B30; // 红:已锁定
        } else {
            // 白框透明度渐变闪烁:sin 波形在 35%~100% 之间平滑呼吸(不会完全消失)
            double phase = (player.world.getTotalWorldTime() % 40) / 40.0D * Math.PI * 2.0D;
            float alpha = 0.35f + 0.65f * (float) ((Math.sin(phase) + 1.0D) * 0.5D);
            int a = (int) (alpha * 255.0F) & 0xFF;
            color = (a << 24) | 0xFFFFFF;
        }
        int thick = 2;
        // 四条边
        Gui.drawRect(cx - size, cy - size, cx + size, cy - size + thick, color);
        Gui.drawRect(cx - size, cy + size - thick, cx + size, cy + size, color);
        Gui.drawRect(cx - size, cy - size, cx - size + thick, cy + size, color);
        Gui.drawRect(cx + size - thick, cy - size, cx + size, cy + size, color);
    }
}
