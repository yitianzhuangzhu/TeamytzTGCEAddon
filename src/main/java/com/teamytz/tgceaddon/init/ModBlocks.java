package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = com.teamytz.tgceaddon.TGCEAddon.MODID)
public class ModBlocks {

    public static final List<Block> BLOCKLIST = new ArrayList<>();
    public static final List<ItemBlock> ITEMBLOCKLIST = new ArrayList<>();

    public static BlockUniversalCopier UNIVERSAL_COPIER;
    public static BlockTurretBase TURRET_BASE;
    public static BlockTurretBaseSlave TURRET_BASE_SLAVE;
    public static BlockHeatSource HEAT_SOURCE;

    public static void init() {
        UNIVERSAL_COPIER = new BlockUniversalCopier("universal_copier");
        BLOCKLIST.add(UNIVERSAL_COPIER);

        TURRET_BASE = new BlockTurretBase("turret_controller");
        BLOCKLIST.add(TURRET_BASE);

        TURRET_BASE_SLAVE = new BlockTurretBaseSlave("turret_plate");
        BLOCKLIST.add(TURRET_BASE_SLAVE);

        // 热源测试方块(红外系统测试用)
        HEAT_SOURCE = new BlockHeatSource("heat_source");
        BLOCKLIST.add(HEAT_SOURCE);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : BLOCKLIST) {
            if (block instanceof BlockUniversalCopier) {
                ((BlockUniversalCopier) block).registerBlock(event);
            } else if (block instanceof BlockTurretBase) {
                ((BlockTurretBase) block).registerBlock(event);
            } else if (block instanceof BlockTurretBaseSlave) {
                ((BlockTurretBaseSlave) block).registerBlock(event);
            } else if (block instanceof BlockHeatSource) {
                ((BlockHeatSource) block).registerBlock(event);
            } else {
                event.getRegistry().register(block);
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<net.minecraft.item.Item> event) {
        for (Block block : BLOCKLIST) {
            ItemBlock itemBlock = null;
            if (block instanceof BlockUniversalCopier) {
                itemBlock = ((BlockUniversalCopier) block).createItemBlock();
            } else if (block instanceof BlockTurretBase) {
                itemBlock = ((BlockTurretBase) block).createItemBlock();
            } else if (block instanceof BlockTurretBaseSlave) {
                itemBlock = ((BlockTurretBaseSlave) block).createItemBlock();
            } else if (block instanceof BlockHeatSource) {
                itemBlock = ((BlockHeatSource) block).createItemBlock();
            } else {
                itemBlock = new ItemBlock(block);
            }
            if (itemBlock != null) {
                itemBlock.setRegistryName(block.getRegistryName());
                event.getRegistry().register(itemBlock);
                ITEMBLOCKLIST.add(itemBlock);
            }
        }
    }

    // ===== 物品模型注册：所有方块物品统一注册 <name>#inventory =====
    // 1.12.2 对 #inventory variant 会自动补全到 models/item/<name>.json（VanillaLoader 兜底），
    // 而 blockstate 的 inventory variant（如有）会优先命中方块模型。
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (ItemBlock itemBlock : ITEMBLOCKLIST) {
            String path = itemBlock.getRegistryName().getResourcePath();
            ModelLoader.setCustomModelResourceLocation(itemBlock, 0,
                    new ModelResourceLocation(new ResourceLocation(TGCEAddon.MODID, path), "inventory"));
        }
    }
}
