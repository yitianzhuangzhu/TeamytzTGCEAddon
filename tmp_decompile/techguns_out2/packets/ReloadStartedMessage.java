package techguns.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ReloadStartedMessage implements IMessage {
   protected int entityID;
   protected int time;
   protected byte attackType;
   protected boolean offHand;

   public ReloadStartedMessage() {
   }

   public ReloadStartedMessage(EntityLivingBase shooter, EnumHand hand, int firetime, int attackType) {
      this.entityID = shooter.func_145782_y();
      this.time = firetime;
      this.attackType = (byte)attackType;
      this.offHand = hand == EnumHand.OFF_HAND;
   }

   public void fromBytes(ByteBuf buf) {
      this.entityID = buf.readInt();
      this.time = buf.readInt();
      this.attackType = buf.readByte();
      this.offHand = buf.readBoolean();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.entityID);
      buf.writeInt(this.time);
      buf.writeByte(this.attackType);
      buf.writeBoolean(this.offHand);
   }
}
