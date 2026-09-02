package com.teamytz.tgceaddon.network;

import com.teamytz.tgceaddon.item.weapon.ItemLockOnWeapon;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 锁定状态同步包(服务端 → 客户端)
 *
 * 携带完整的锁定状态(searching/locked/目标类型),客户端据此渲染瞄准框:
 * - searching=true  locked=false  → 白色呼吸框(索敌中)
 * - searching=false locked=true   → 红色框(已锁定)
 * - searching=false locked=false  → 无框(发射后/未索敌)
 *
 * 注意:searching 由服务端显式同步,不能用 locked 取反推断
 * (发射后 searching=false 且 locked=false,取反会错误地显示白框)。
 * 支持三种锁定目标:实体 / 太阳(无坐标) / 方块热源坐标。
 */
public class LockStateMessage implements IMessage {

    private boolean searching;
    private boolean locked;
    private boolean lockedSun;
    private int entityId;
    private boolean lockedPos;
    private double lockedX;
    private double lockedY;
    private double lockedZ;

    public LockStateMessage() {
    }

    public LockStateMessage(boolean searching, boolean locked, boolean lockedSun, int entityId,
            boolean lockedPos, double lockedX, double lockedY, double lockedZ) {
        this.searching = searching;
        this.locked = locked;
        this.lockedSun = lockedSun;
        this.entityId = entityId;
        this.lockedPos = lockedPos;
        this.lockedX = lockedX;
        this.lockedY = lockedY;
        this.lockedZ = lockedZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.searching = buf.readBoolean();
        this.locked = buf.readBoolean();
        this.lockedSun = buf.readBoolean();
        this.entityId = buf.readInt();
        this.lockedPos = buf.readBoolean();
        this.lockedX = buf.readDouble();
        this.lockedY = buf.readDouble();
        this.lockedZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.searching);
        buf.writeBoolean(this.locked);
        buf.writeBoolean(this.lockedSun);
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.lockedPos);
        buf.writeDouble(this.lockedX);
        buf.writeDouble(this.lockedY);
        buf.writeDouble(this.lockedZ);
    }

    public static class Handler implements IMessageHandler<LockStateMessage, IMessage> {

        @Override
        public IMessage onMessage(LockStateMessage message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    World world = Minecraft.getMinecraft().world;
                    EntityPlayer self = Minecraft.getMinecraft().player;
                    if (world == null || self == null) {
                        return;
                    }
                    ItemStack stack = self.getHeldItemMainhand();
                    if (stack.getItem() instanceof ItemLockOnWeapon) {
                        ItemLockOnWeapon.LockState state = ItemLockOnWeapon.getState(self);
                        state.searching = message.searching;
                        state.locked = message.locked;
                        state.lockedSun = message.lockedSun;
                        state.lockedEntity = message.entityId != -1 ? world.getEntityByID(message.entityId) : null;
                        state.lockedPos = message.lockedPos;
                        state.lockedX = message.lockedX;
                        state.lockedY = message.lockedY;
                        state.lockedZ = message.lockedZ;
                    }
                });
            }
            return null;
        }
    }
}
