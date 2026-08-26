package com.teamytz.tgceaddon.tileentities;

import com.teamytz.tgceaddon.init.BlockTurretBaseSlave;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * 炮塔基座 - 从方块 TileEntity
 * 记录所属 master 的位置：
 * - 被破坏时通知 master 解散整个多方块结构
 * - 与 master 水平直连（非角落）的 4 个从方块额外提供 RF 能量输入能力，
 *   收到的能量直接进入 master 的能量存储
 */
public class TurretBaseTileEntSlave extends TileEntity {

    protected boolean hasMaster = false;
    protected int masterX;
    protected int masterY;
    protected int masterZ;

    public void form(BlockPos masterPos) {
        this.hasMaster = true;
        this.masterX = masterPos.getX();
        this.masterY = masterPos.getY();
        this.masterZ = masterPos.getZ();
        this.needUpdate();
    }

    public void unform() {
        this.hasMaster = false;
        this.needUpdate();
    }

    public boolean hasMaster() {
        return hasMaster;
    }

    public BlockPos getMasterPos() {
        return new BlockPos(masterX, masterY, masterZ);
    }

    // ===== 能力暴露 =====
    // 物品：所有从方块均可作为管道/漏斗接入点，直接读写 master 的物品栏（弹药输入/输出槽）
    // 能量：仅与 master 水平直连（非角落）的从方块可作为能量输入端
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getMaster() != null) {
            return true;
        }
        if (capability == CapabilityEnergy.ENERGY && isDirectNeighbor() && getMaster() != null) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        TurretBaseTileEntMaster master = getMaster();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && master != null) {
            return master.getCapability(capability, facing);
        }
        if (capability == CapabilityEnergy.ENERGY && isDirectNeighbor() && master != null) {
            return CapabilityEnergy.ENERGY.cast(master.getEnergyStorage());
        }
        return super.getCapability(capability, facing);
    }

    /**
     * 是否为与 master 水平直连（上下左右 4 个方向，而非角落）的从方块
     */
    private boolean isDirectNeighbor() {
        if (!this.hasMaster) {
            return false;
        }
        int dx = this.pos.getX() - this.masterX;
        int dz = this.pos.getZ() - this.masterZ;
        return Math.abs(dx) + Math.abs(dz) == 1;
    }

    private TurretBaseTileEntMaster getMaster() {
        if (this.hasMaster && this.world != null) {
            TileEntity tile = this.world.getTileEntity(getMasterPos());
            if (tile instanceof TurretBaseTileEntMaster) {
                return (TurretBaseTileEntMaster) tile;
            }
        }
        return null;
    }

    /**
     * 从方块被破坏时，通知 master 解散结构
     */
    public void onBlockBreak() {
        if (this.hasMaster && this.world != null && !this.world.isRemote) {
            TileEntity tile = this.world.getTileEntity(getMasterPos());
            if (tile instanceof TurretBaseTileEntMaster) {
                ((TurretBaseTileEntMaster) tile).onMultiBlockBreak();
            }
        }
    }

    // ===== 网络同步 =====
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tags = new NBTTagCompound();
        this.writeClientDataToNBT(tags);
        return new SPacketUpdateTileEntity(pos, 1, tags);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readClientDataFromNBT(packet.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tags = super.getUpdateTag();
        this.writeClientDataToNBT(tags);
        return tags;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.readClientDataFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        this.writeClientDataToNBT(compound);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.readClientDataFromNBT(compound);
        super.readFromNBT(compound);
    }

    private void writeClientDataToNBT(NBTTagCompound tags) {
        tags.setBoolean("hasMaster", this.hasMaster);
        if (this.hasMaster) {
            tags.setInteger("masterX", this.masterX);
            tags.setInteger("masterY", this.masterY);
            tags.setInteger("masterZ", this.masterZ);
        }
    }

    private void readClientDataFromNBT(NBTTagCompound tags) {
        this.hasMaster = tags.getBoolean("hasMaster");
        if (this.hasMaster) {
            this.masterX = tags.getInteger("masterX");
            this.masterY = tags.getInteger("masterY");
            this.masterZ = tags.getInteger("masterZ");
        }
    }

    private void needUpdate() {
        if (this.world != null && !this.world.isRemote) {
            // 将 FORMED 状态直接写入实际 blockstate(meta)：setBlockState 会发送
            // SPacketBlockUpdate，客户端收到后原子地刷新方块状态与渲染。
            // 之前仅通知 TE(hasMaster) 或发送相同状态的 notifyBlockUpdate 时，
            // 客户端渲染依赖的 getActualState 不会及时重算，导致拆散后模型残留透明。
            IBlockState state = this.world.getBlockState(this.pos);
            // 正在被破坏的 slave 的 blockstate 已是空气，不能写入 FORMED 属性，需跳过
            if (state.getBlock() instanceof BlockTurretBaseSlave) {
                this.world.setBlockState(this.pos, state.withProperty(BlockTurretBaseSlave.FORMED, this.hasMaster), 3);
            }
        }
        this.markDirty();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, net.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
