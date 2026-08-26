package techguns.capabilities;

import java.util.BitSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import techguns.TGItems;
import techguns.TGRadiationSystem;
import techguns.Techguns;
import techguns.api.capabilities.AttackTime;
import techguns.api.capabilities.ITGExtendedPlayer;
import techguns.capabilities.TGExtendedPlayer.1;
import techguns.gui.player.TGPlayerInventory;
import techguns.util.DataUtil;

public class TGExtendedPlayer implements ITGExtendedPlayer {
   public static final int MAX_RADIATION = 1000;
   public static final DataParameter DATA_FACE_SLOT = EntityDataManager.func_187226_a(EntityPlayer.class, DataSerializers.field_187196_f);
   public static final DataParameter DATA_BACK_SLOT = EntityDataManager.func_187226_a(EntityPlayer.class, DataSerializers.field_187196_f);
   public static final DataParameter DATA_HAND_SLOT = EntityDataManager.func_187226_a(EntityPlayer.class, DataSerializers.field_187196_f);
   public static final DataParameter DATA_FLAG_CHARGING_WEAPON = EntityDataManager.func_187226_a(EntityPlayer.class, DataSerializers.field_187198_h);
   public int fireDelayMainhand = 0;
   public int fireDelayOffhand = 0;
   public int loopSoundDelayMainhand = 0;
   public int loopSoundDelayOffhand = 0;
   public EntityPlayer entity;
   protected AttackTime[] attackTimes = new AttackTime[]{new AttackTime(), new AttackTime()};
   public int swingSoundDelay = 0;
   public boolean gotCreativeFlightLastTick = false;
   public boolean wasFlying = false;
   public ItemStack gunMainHand = ItemStack.field_190927_a;
   public ItemStack gunOffHand = ItemStack.field_190927_a;
   public Entity lockOnEntity;
   public int lockOnTicks;
   protected boolean isJumpkeyPressed = false;
   public boolean isForwardKeyPressed = false;
   public boolean isGliding = false;
   public TGPlayerInventory tg_inventory;
   public boolean enableJetpack = true;
   public boolean enableStepAssist = true;
   public boolean enableNightVision = false;
   public boolean enableSafemode = true;
   public static final DataParameter DATA_UNLOCK_CYBERNETIC_PARTS = EntityDataManager.func_187226_a(EntityPlayer.class, DataSerializers.field_187198_h);
   public boolean enableHovermode = false;
   public int radlevel = 0;
   public short foodleft = 0;
   public float lastSaturation = 1.0F;
   public boolean showTGHudElements = true;

   public TGExtendedPlayer(EntityPlayer entity) {
      this.entity = entity;
      this.tg_inventory = new TGPlayerInventory(entity);
      if (entity != null) {
         entity.func_184212_Q().func_187214_a(DATA_FACE_SLOT, ItemStack.field_190927_a);
         entity.func_184212_Q().func_187214_a(DATA_BACK_SLOT, ItemStack.field_190927_a);
         entity.func_184212_Q().func_187214_a(DATA_HAND_SLOT, ItemStack.field_190927_a);
         entity.func_184212_Q().func_187214_a(DATA_FLAG_CHARGING_WEAPON, false);
         entity.func_184212_Q().func_187214_a(DATA_UNLOCK_CYBERNETIC_PARTS, false);
      }

   }

   public EntityPlayer getEntity() {
      return this.entity;
   }

   public void copyFrom(TGExtendedPlayer other) {
      this.entity = other.getEntity();
      this.fireDelayMainhand = other.fireDelayMainhand;
      this.fireDelayOffhand = other.fireDelayOffhand;
      this.loopSoundDelayMainhand = other.loopSoundDelayMainhand;
      this.loopSoundDelayOffhand = other.loopSoundDelayOffhand;
      this.attackTimes = other.attackTimes;
      this.swingSoundDelay = other.swingSoundDelay;
      this.gotCreativeFlightLastTick = other.gotCreativeFlightLastTick;
      this.wasFlying = other.wasFlying;
      this.gunMainHand = other.gunMainHand;
      this.gunOffHand = other.gunOffHand;
      this.isJumpkeyPressed = other.isJumpkeyPressed;
      this.isForwardKeyPressed = other.isForwardKeyPressed;
      this.isGliding = other.isGliding;
      this.tg_inventory = other.tg_inventory;
      this.enableJetpack = other.enableJetpack;
      this.enableStepAssist = other.enableStepAssist;
      this.enableNightVision = other.enableNightVision;
      this.enableSafemode = other.enableSafemode;
      this.enableHovermode = other.enableHovermode;
      this.radlevel = other.radlevel;
      this.foodleft = other.foodleft;
      this.lastSaturation = other.lastSaturation;
      this.showTGHudElements = other.showTGHudElements;
   }

   public static TGExtendedPlayer get(EntityPlayer ply) {
      return (TGExtendedPlayer)ply.getCapability(TGExtendedPlayerCapProvider.TG_EXTENDED_PLAYER, (EnumFacing)null);
   }

   public static boolean isCyberneticPartsOutput(ItemStack stack) {
      return !stack.func_190926_b() && ItemStack.func_179545_c(stack, TGItems.CYBERNETIC_PARTS) && ItemStack.func_77970_a(stack, TGItems.CYBERNETIC_PARTS);
   }

   public AttackTime getAttackTime(boolean offHand) {
      return this.attackTimes[offHand ? 1 : 0];
   }

   public int getFireDelay(EnumHand hand) {
      switch (1.$SwitchMap$net$minecraft$util$EnumHand[hand.ordinal()]) {
         case 1:
            return this.fireDelayMainhand;
         case 2:
            return this.fireDelayOffhand;
         default:
            return 0;
      }
   }

   public void setFireDelay(EnumHand hand, int delay) {
      switch (1.$SwitchMap$net$minecraft$util$EnumHand[hand.ordinal()]) {
         case 1:
            this.fireDelayMainhand = delay;
            break;
         case 2:
            this.fireDelayOffhand = delay;
      }

   }

   public int getLoopSoundDelay(EnumHand hand) {
      switch (1.$SwitchMap$net$minecraft$util$EnumHand[hand.ordinal()]) {
         case 1:
            return this.loopSoundDelayMainhand;
         case 2:
            return this.loopSoundDelayOffhand;
         default:
            return 0;
      }
   }

   public void setLoopSoundDelay(EnumHand hand, int delay) {
      switch (1.$SwitchMap$net$minecraft$util$EnumHand[hand.ordinal()]) {
         case 1:
            this.loopSoundDelayMainhand = delay;
            break;
         case 2:
            this.loopSoundDelayOffhand = delay;
      }

   }

   public boolean isRecoiling(boolean offHand) {
      return this.getAttackTime(offHand).isRecoiling();
   }

   public boolean isReloading(boolean offHand) {
      return this.getAttackTime(offHand).isReloading();
   }

   public boolean hasFabricatorRecipeUnlocked(ItemStack output) {
      if (!isCyberneticPartsOutput(output)) {
         return true;
      } else {
         return this.entity != null && this.entity.func_184212_Q().func_187225_a(DATA_UNLOCK_CYBERNETIC_PARTS);
      }
   }

   public boolean unlockFabricatorRecipe(ItemStack output) {
      if (isCyberneticPartsOutput(output) && this.entity != null) {
         boolean had = this.entity.func_184212_Q().func_187225_a(DATA_UNLOCK_CYBERNETIC_PARTS);
         if (!had) {
            this.entity.func_184212_Q().func_187227_b(DATA_UNLOCK_CYBERNETIC_PARTS, true);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void swapAttackTimes() {
      AttackTime a = this.attackTimes[0];
      this.attackTimes[0] = this.attackTimes[1];
      this.attackTimes[1] = a;
      int i = this.fireDelayMainhand;
      this.fireDelayMainhand = this.fireDelayOffhand;
      this.fireDelayOffhand = i;
   }

   public IInventory getTGInventory() {
      return this.tg_inventory;
   }

   public void saveToNBT(NBTTagCompound tags) {
      this.tg_inventory.saveNBTData(tags);
      byte data = DataUtil.compress(new boolean[]{this.enableJetpack, this.enableNightVision, this.enableSafemode, this.enableStepAssist, this.showTGHudElements, this.enableHovermode});
      tags.func_74774_a("states", data);
      tags.func_74777_a("foodLeft", this.foodleft);
      tags.func_74776_a("lastSaturation", this.lastSaturation);
      tags.func_74768_a("radlevel", this.radlevel);
      tags.func_74757_a("unlockCyberParts", this.entity != null && this.entity.func_184212_Q().func_187225_a(DATA_UNLOCK_CYBERNETIC_PARTS));
   }

   public void loadFromNBT(NBTTagCompound tags) {
      this.tg_inventory.loadNBTData(tags);
      BitSet states = DataUtil.uncompress(tags.func_74771_c("states"));
      this.enableJetpack = states.get(0);
      this.enableNightVision = states.get(1);
      this.enableSafemode = states.get(2);
      if (this.entity != null && Techguns.instance.permissions.canUseUnsafeMode(this.entity)) {
         this.enableSafemode = true;
      }

      this.enableStepAssist = states.get(3);
      this.showTGHudElements = states.get(4);
      this.enableHovermode = states.get(5);
      this.foodleft = tags.func_74765_d("foodLeft");
      this.lastSaturation = tags.func_74760_g("lastSaturation");
      this.radlevel = tags.func_74762_e("radlevel");
      if (this.entity != null) {
         this.entity.func_184212_Q().func_187227_b(DATA_UNLOCK_CYBERNETIC_PARTS, tags.func_74767_n("unlockCyberParts"));
      }

   }

   public boolean isJumpkeyPressed() {
      return this.isJumpkeyPressed;
   }

   public void setJumpkeyPressed(boolean isJumpkeyPressed) {
      this.isJumpkeyPressed = isJumpkeyPressed;
   }

   public void dropInventory(EntityPlayer player) {
      if (!player.field_70170_p.field_72995_K && !player.field_70170_p.func_82736_K().func_82766_b("keepInventory")) {
         player.captureDrops = true;

         for(int i = 0; i < this.tg_inventory.inventory.size(); ++i) {
            if (!((ItemStack)this.tg_inventory.inventory.get(i)).func_190926_b()) {
               player.func_146097_a((ItemStack)this.tg_inventory.inventory.get(i), true, false);
               this.tg_inventory.inventory.set(i, ItemStack.field_190927_a);
            }
         }

         player.captureDrops = false;
      }

   }

   public boolean isChargingWeapon() {
      return this.entity.func_184212_Q().func_187225_a(DATA_FLAG_CHARGING_WEAPON);
   }

   public void setChargingWeapon(boolean charging) {
      this.entity.func_184212_Q().func_187227_b(DATA_FLAG_CHARGING_WEAPON, charging);
   }

   public void addRadiation(int amount) {
      if (!TGRadiationSystem.isEnabled()) {
         amount = 0;
      }

      this.radlevel += amount;
      if (this.radlevel < 0) {
         this.radlevel = 0;
      } else if (this.radlevel > 1000) {
         this.radlevel = 1000;
      }

   }
}
