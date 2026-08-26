package techguns.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import techguns.TGPackets;
import techguns.client.ShooterValues;

public class ReloadStartedMessage$Handler implements IMessageHandler {
   public IMessage onMessage(ReloadStartedMessage message, MessageContext ctx) {
      FMLCommonHandler.instance().getWorldThread(ctx.netHandler).func_152344_a(() -> this.handle(message, ctx));
      return null;
   }

   private void handle(ReloadStartedMessage message, MessageContext ctx) {
      EntityPlayer ply = TGPackets.getPlayerFromContext(ctx);
      EntityLivingBase shooter = (EntityLivingBase)ply.field_70170_p.func_73045_a(message.entityID);
      if (shooter != null && shooter != Minecraft.func_71410_x().field_71439_g) {
         ShooterValues.setReloadtime(shooter, message.offHand, System.currentTimeMillis() + (long)message.time, message.time, message.attackType);
      }

   }
}
