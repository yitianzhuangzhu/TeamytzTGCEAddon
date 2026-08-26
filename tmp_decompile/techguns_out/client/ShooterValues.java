package techguns.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import techguns.api.capabilities.AttackTime;
import techguns.api.npc.INPCTechgunsShooter;
import techguns.capabilities.TGExtendedPlayer;
import techguns.capabilities.TGShooterValues;

public class ShooterValues {
   protected static AttackTime getAttackTimes(EntityLivingBase ent, boolean offHand) {
      if (ent instanceof EntityPlayer) {
         TGExtendedPlayer caps = TGExtendedPlayer.get((EntityPlayer)ent);
         if (caps != null) {
            return caps.getAttackTime(offHand);
         }
      } else if (ent instanceof INPCTechgunsShooter) {
         TGShooterValues caps = TGShooterValues.get(ent);
         if (caps != null) {
            return caps.getAttackTime(offHand);
         }
      }

      return null;
   }

   public static long getRecoiltime(EntityLivingBase ent, boolean offHand) {
      AttackTime attack = getAttackTimes(ent, offHand);
      return attack != null ? attack.getRecoilTime() : 0L;
   }

   public static boolean isStillRecoiling(EntityLivingBase ent, boolean offHand, byte attacktype) {
      if (ent != ClientProxy.get().getPlayerClient()) {
         return false;
      } else if (Minecraft.func_71410_x().field_71474_y.field_74320_O != 0) {
         return false;
      } else {
         AttackTime attack = getAttackTimes(ent, offHand);
         if (attack == null) {
            return false;
         } else if (attack.getAttackType() != attacktype) {
            return false;
         } else if (attack.getRecoilTime() <= 0L) {
            return false;
         } else {
            return System.currentTimeMillis() - attack.getRecoilTime() < 0L;
         }
      }
   }

   public static void setRecoiltime(EntityLivingBase ent, boolean offHand, long time, int total, byte attacktype) {
      setRecoiltime(ent, offHand, time, total, attacktype, 0.0F);
   }

   public static void setRecoiltime(EntityLivingBase ent, boolean offHand, long time, int total, byte attacktype, float chargeProgress) {
      AttackTime attack = getAttackTimes(ent, offHand);
      if (attack != null) {
         attack.setRecoilTime(time);
         attack.setRecoilTimeTotal(total);
         attack.setAttackType(attacktype);
         attack.setRecoilChargeProgress(chargeProgress);
      }

   }

   public static long getReloadtime(EntityLivingBase ent, boolean offHand) {
      AttackTime attack = getAttackTimes(ent, offHand);
      return attack != null ? attack.getReloadTime() : 0L;
   }

   public static void setReloadtime(EntityLivingBase ent, boolean offHand, long time, int total, byte attackType) {
      AttackTime attack = getAttackTimes(ent, offHand);
      if (attack != null) {
         attack.setReloadTime(time);
         attack.setReloadTimeTotal(total);
         attack.setAttackType(attackType);
      }

   }

   public static boolean getIsCurrentlyUsingGun(EntityLivingBase ent, boolean offHand) {
      AttackTime attack = getAttackTimes(ent, offHand);
      return attack.isReloading() || attack.isRecoiling();
   }

   public static boolean getPlayerIsReloading(EntityLivingBase ent, boolean offHand) {
      AttackTime attack = getAttackTimes(ent, offHand);
      return attack.isReloading();
   }

   public static void setMuzzleFlashTime(EntityLivingBase ent, boolean offHand, long time, int total) {
      AttackTime attack = getAttackTimes(ent, offHand);
      if (attack != null) {
         attack.setMuzzleFlashTime(time);
         attack.setMuzzleFlashTimeTotal(total);
      }

   }

   public static long getMuzzleFlashTime(EntityLivingBase ent, boolean offHand) {
      AttackTime attack = getAttackTimes(ent, offHand);
      return attack != null ? attack.getMuzzleFlashTime() : 0L;
   }
}
