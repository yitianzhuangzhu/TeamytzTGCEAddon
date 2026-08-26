package com.teamytz.tgceaddon.init;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = com.teamytz.tgceaddon.TGCEAddon.MODID)
public class ModBlocks {

    public static final List<Block> BLOCKLIST = new ArrayList<>();
    public static final List<ItemBlock> ITEMBLOCKLIST = new ArrayList<>();

    public static BlockUniversalCopier UNIVERSAL_COPIER;
    public static BlockTurretBase TURRET_BASE;
    public static BlockTurretBaseSlave TURRET_BASE_SLAVE;

    public static void init() {
        UNIVERSAL_COPIER = new BlockUniversalCopier("universal_copier");
        BLOCKLIST.add(UNIVERSAL_COPIER);

        TURRET_BASE = new BlockTurretBase("turret_controller");
        BLOCKLIST.add(TURRET_BASE);

        TURRET_BASE_SLAVE = new BlockTurretBaseSlave("turret_plate");
        BLOCKLIST.add(TURRET_BASE_SLAVE);
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
            }
            if (itemBlock != null) {
                itemBlock.setRegistryName(block.getRegistryName());
                event.getRegistry().register(itemBlock);
                ITEMBLOCKLIST.add(itemBlock);
            }
        }
    }
}
