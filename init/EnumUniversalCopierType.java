package com.teamytz.tgceaddon.init;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.api.machines.IMachineType;
import com.teamytz.tgceaddon.tileentities.UniversalCopierTileEnt;

public enum EnumUniversalCopierType implements IStringSerializable, IMachineType {
    UNIVERSAL_COPIER(0, UniversalCopierTileEnt.class, true, EnumBlockRenderType.MODEL);

    private final int id;
    private final String name;
    private final Class<? extends TileEntity> tile;
    private final boolean isFullCube;
    private final EnumBlockRenderType renderType;
    private final BlockRenderLayer renderLayer;

    EnumUniversalCopierType(int id, Class<? extends TileEntity> tile, boolean isFullCube, EnumBlockRenderType renderType) {
        this(id, tile, isFullCube, renderType, BlockRenderLayer.SOLID);
    }

    EnumUniversalCopierType(int id, Class<? extends TileEntity> tile, boolean isFullCube, EnumBlockRenderType renderType, BlockRenderLayer layer) {
        this.id = id;
        this.name = this.name().toLowerCase();
        this.tile = tile;
        this.isFullCube = isFullCube;
        this.renderType = renderType;
        this.renderLayer = layer;
    }

    @Override
    public int getIndex() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public int getMaxMachineIndex() { return EnumUniversalCopierType.values().length; }

    @Override
    public boolean isFullCube() { return isFullCube; }

    @Override
    public EnumBlockRenderType getRenderType() { return renderType; }

    @Override
    public BlockRenderLayer getBlockRenderLayer() { return renderLayer; }

    @Override
    public boolean debugOnly() { return false; }

    @Override
    public TileEntity getTile() {
        try {
            return this.tile.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<? extends TileEntity> getTileClass() { return this.tile; }

    @Override
    public boolean hasCustomModelLocation() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void setCustomModelLocation(Item itemblock, int meta, ResourceLocation registryName, IBlockState state) {
        ModelLoader.setCustomModelResourceLocation(itemblock, meta,
            new ModelResourceLocation("tgceaddon:block/universal_copier", "inventory"));
    }
}
