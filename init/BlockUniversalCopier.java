package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import com.teamytz.tgceaddon.tileentities.UniversalCopierTileEnt;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;

public class BlockUniversalCopier extends Block {

    // ===== 构造：基础方块属性（材质、硬度、创造模式分组）=====
    public BlockUniversalCopier(String name) {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(TGCEAddon.MODID, name));
        this.setUnlocalizedName(TGCEAddon.MODID + "." + name);
        this.setCreativeTab(ModCreativeTabs.TAB_TGCEADDON);
        this.setSoundType(SoundType.METAL);
        this.setHardness(4.0f);
        this.setResistance(30.0f);
    }

    // ===== TileEntity 绑定：方块放置时创建对应的逻辑实体 =====
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new UniversalCopierTileEnt();
    }

    // ===== 右键交互：打开通用复制机 GUI =====
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(TGCEAddon.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    // ===== 渲染属性：非完整方块，允许看到背面纹理 =====
    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    // ===== 注册辅助：创建对应 ItemBlock 并注册方块本体 =====
    public ItemBlock createItemBlock() {
        return new ItemBlock(this);
    }

    public void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(this);
    }
}
