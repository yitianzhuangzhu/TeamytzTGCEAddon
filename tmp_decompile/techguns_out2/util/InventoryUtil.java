package techguns.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import techguns.capabilities.TGExtendedPlayer;
import techguns.tileentities.operation.ItemStackHandlerPlus;

public class InventoryUtil {
   public static int addAmmoToPlayerInventory(EntityPlayer ply, ItemStack ammo) {
      TGExtendedPlayer props = TGExtendedPlayer.get(ply);
      if (props != null) {
         int amount = addItemToInventory(props.tg_inventory.inventory, ammo, 7, 15);
         return amount > 0 ? addItemToInventory(ply.field_71071_by.field_70462_a, ammo, 0, ply.field_71071_by.field_70462_a.size()) : 0;
      } else {
         return addItemToInventory(ply.field_71071_by.field_70462_a, ammo, 0, ply.field_71071_by.field_70462_a.size());
      }
   }

   public static int addAmmoToAmmoInventory(EntityPlayer ply, ItemStack ammo) {
      TGExtendedPlayer props = TGExtendedPlayer.get(ply);
      return props != null ? addItemToInventory(props.tg_inventory.inventory, ammo, 7, 15) : ammo.func_190916_E();
   }

   public static int addItemToInventory(NonNullList mainInventory, ItemStack item2, int startIndex, int endIndex) {
      ItemStack item = item2.func_77946_l();

      for(int i = startIndex; i < endIndex; ++i) {
         if (OreDictionary.itemMatches((ItemStack)mainInventory.get(i), item, true) && ((ItemStack)mainInventory.get(i)).func_190916_E() < item.func_77976_d()) {
            int diff = ((ItemStack)mainInventory.get(i)).func_190916_E() + item.func_190916_E() - item.func_77976_d();
            if (diff < 0) {
               int c = ((ItemStack)mainInventory.get(i)).func_190916_E();
               ((ItemStack)mainInventory.get(i)).func_190920_e(c + item.func_190916_E());
               item.func_190920_e(0);
               return 0;
            }

            ((ItemStack)mainInventory.get(i)).func_190920_e(item.func_77976_d());
            item.func_190920_e(diff);
         }
      }

      if (item.func_190916_E() > 0) {
         for(int i = startIndex; i < endIndex; ++i) {
            if (((ItemStack)mainInventory.get(i)).func_190926_b()) {
               mainInventory.set(i, item.func_77946_l());
               item.func_190920_e(0);
               return 0;
            }
         }
      }

      return item.func_190916_E();
   }

   public static int addItemToInventory(ItemStackHandlerPlus mainInventory, ItemStack item2, int startIndex, int endIndex) {
      ItemStack item = item2.func_77946_l();

      for(int i = startIndex; i < endIndex; ++i) {
         if (OreDictionary.itemMatches(mainInventory.getStackInSlot(i), item, true) && mainInventory.getStackInSlot(i).func_190916_E() < item.func_77976_d()) {
            int diff = mainInventory.getStackInSlot(i).func_190916_E() + item.func_190916_E() - item.func_77976_d();
            if (diff < 0) {
               mainInventory.insertItemNoCheck(i, item, false);
               return 0;
            }

            item = mainInventory.insertItemNoCheck(i, item, false);
         }
      }

      if (item.func_190916_E() > 0) {
         for(int i = startIndex; i < endIndex; ++i) {
            if (mainInventory.getStackInSlot(i).func_190926_b()) {
               mainInventory.insertItemNoCheck(i, item, false);
               return 0;
            }
         }
      }

      return item.func_190916_E();
   }

   public static boolean consumeAmmoPlayer(EntityPlayer ply, ItemStack[] ammo) {
      if (ammo.length == 1) {
         return consumeAmmoPlayer(ply, ammo[0]);
      } else {
         boolean canconsume = true;

         for(ItemStack itemStack : ammo) {
            if (!canConsumeAmmoPlayer(ply, itemStack)) {
               canconsume = false;
               break;
            }
         }

         if (!canconsume) {
            return false;
         } else {
            for(ItemStack itemStack : ammo) {
               consumeAmmoPlayer(ply, itemStack);
            }

            return true;
         }
      }
   }

   public static boolean consumeAmmoPlayer(EntityPlayer ply, ItemStack ammo) {
      TGExtendedPlayer props = TGExtendedPlayer.get(ply);
      if (props != null) {
         int amount = ammo.func_190916_E();
         if (amount == 1) {
            return consumeAmmo(props.tg_inventory.inventory, ammo, 7, 15) ? true : consumeAmmo(ply.field_71071_by.field_70462_a, ammo, 0, ply.field_71071_by.field_70462_a.size());
         } else {
            int needed = canConsumeItem(props.tg_inventory.inventory, ammo, 7, 15);
            int needed2 = canConsumeItem(ply.field_71071_by.field_70462_a, ammo, 0, ply.field_71071_by.field_70462_a.size());
            if (needed + needed2 <= amount) {
               int missing = consumeItem(props.tg_inventory.inventory, ammo, 7, 15);
               if (missing > 0) {
                  return consumeItem(ply.field_71071_by.field_70462_a, new ItemStack(ammo.func_77973_b(), missing, ammo.func_77952_i()), 0, ply.field_71071_by.field_70462_a.size()) <= 0;
               } else {
                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean canConsumeAmmoPlayer(EntityPlayer ply, ItemStack ammo) {
      TGExtendedPlayer props = TGExtendedPlayer.get(ply);
      if (props != null) {
         int amount = ammo.func_190916_E();
         if (amount == 1) {
            if (canConsumeItem(props.tg_inventory.inventory, ammo, 7, 15) <= 0) {
               return true;
            } else {
               return canConsumeItem(ply.field_71071_by.field_70462_a, ammo, 0, ply.field_71071_by.field_70462_a.size()) <= 0;
            }
         } else {
            int needed = canConsumeItem(props.tg_inventory.inventory, ammo, 7, 15);
            int needed2 = canConsumeItem(ply.field_71071_by.field_70462_a, ammo, 0, ply.field_71071_by.field_70462_a.size());
            return needed + needed2 <= amount;
         }
      } else {
         return false;
      }
   }

   public static int canConsumeItem(NonNullList inv, ItemStack item, int startIndex, int endIndex) {
      int needed = item.func_190916_E();

      for(int i = startIndex; i < endIndex; ++i) {
         if (!((ItemStack)inv.get(i)).func_190926_b() && ((ItemStack)inv.get(i)).func_77973_b() == item.func_77973_b() && ((ItemStack)inv.get(i)).func_77952_i() == item.func_77952_i()) {
            needed -= ((ItemStack)inv.get(i)).func_190916_E();
            if (needed <= 0) {
               return 0;
            }
         }
      }

      return needed;
   }

   public static int canConsumeItem(ItemStackHandler inv, ItemStack item, int startIndex, int endIndex) {
      int needed = item.func_190916_E();

      for(int i = startIndex; i < endIndex; ++i) {
         if (!inv.getStackInSlot(i).func_190926_b() && inv.getStackInSlot(i).func_77973_b() == item.func_77973_b() && inv.getStackInSlot(i).func_77952_i() == item.func_77952_i()) {
            needed -= inv.getStackInSlot(i).func_190916_E();
            if (needed <= 0) {
               return 0;
            }
         }
      }

      return needed;
   }

   public static int consumeItem(NonNullList inv, ItemStack item, int startIndex, int endIndex) {
      int needed = item.func_190916_E();

      for(int i = startIndex; i < endIndex; ++i) {
         if (!((ItemStack)inv.get(i)).func_190926_b() && ((ItemStack)inv.get(i)).func_77973_b() == item.func_77973_b() && ((ItemStack)inv.get(i)).func_77952_i() == item.func_77952_i()) {
            if (((ItemStack)inv.get(i)).func_190916_E() > needed) {
               ((ItemStack)inv.get(i)).func_190920_e(((ItemStack)inv.get(i)).func_190916_E() - needed);
               return 0;
            }

            needed -= ((ItemStack)inv.get(i)).func_190916_E();
            inv.set(i, ItemStack.field_190927_a);
         }
      }

      return needed;
   }

   public static void consumeItem(ItemStackHandler inv, ItemStack item, int startIndex, int endIndex) {
      int needed = item.func_190916_E();

      for(int i = startIndex; i < endIndex; ++i) {
         if (!inv.getStackInSlot(i).func_190926_b() && inv.getStackInSlot(i).func_77973_b() == item.func_77973_b() && inv.getStackInSlot(i).func_77952_i() == item.func_77952_i()) {
            if (inv.getStackInSlot(i).func_190916_E() > needed) {
               inv.getStackInSlot(i).func_190920_e(inv.getStackInSlot(i).func_190916_E() - needed);
               return;
            }

            needed -= inv.getStackInSlot(i).func_190916_E();
            inv.setStackInSlot(i, ItemStack.field_190927_a);
         }
      }

   }

   private static int searchItem(NonNullList inv, ItemStack stack, int startIndex, int endIndex) {
      for(int i = startIndex; i < endIndex; ++i) {
         if (!((ItemStack)inv.get(i)).func_190926_b() && ((ItemStack)inv.get(i)).func_77973_b() == stack.func_77973_b() && ((ItemStack)inv.get(i)).func_77952_i() == stack.func_77952_i()) {
            return i;
         }
      }

      return -1;
   }

   private static int searchItem(ItemStackHandler inv, ItemStack stack, int startIndex, int endIndex) {
      for(int i = startIndex; i < endIndex; ++i) {
         if (!inv.getStackInSlot(i).func_190926_b() && inv.getStackInSlot(i).func_77973_b() == stack.func_77973_b() && inv.getStackInSlot(i).func_77952_i() == stack.func_77952_i()) {
            return i;
         }
      }

      return -1;
   }

   public static boolean consumeAmmo(NonNullList inv, ItemStack ammo, int startIndex, int endIndex) {
      int i = searchItem(inv, ammo, startIndex, endIndex);
      if (i < 0) {
         return false;
      } else {
         ((ItemStack)inv.get(i)).func_190920_e(((ItemStack)inv.get(i)).func_190916_E() - 1);
         if (((ItemStack)inv.get(i)).func_190916_E() <= 0) {
            inv.set(i, ItemStack.field_190927_a);
         }

         return true;
      }
   }

   public static boolean consumeAmmo(ItemStackHandlerPlus inv, ItemStack ammo, int startIndex, int endIndex) {
      int i = searchItem(inv, ammo, startIndex, endIndex);
      if (i < 0) {
         return false;
      } else {
         inv.extractWithoutCheck(i, 1, false);
         return true;
      }
   }

   public static ItemStack consumeFood(NonNullList inv, int startIndex, int endIndex) {
      for(int i = startIndex; i < endIndex; ++i) {
         if (!((ItemStack)inv.get(i)).func_190926_b() && ((ItemStack)inv.get(i)).func_77973_b() instanceof ItemFood) {
            ItemStack food = new ItemStack(((ItemStack)inv.get(i)).func_77973_b(), 1, ((ItemStack)inv.get(i)).func_77952_i());
            ((ItemStack)inv.get(i)).func_190920_e(((ItemStack)inv.get(i)).func_190916_E() - 1);
            if (((ItemStack)inv.get(i)).func_190916_E() <= 0) {
               inv.set(i, ItemStack.field_190927_a);
            }

            return food;
         }
      }

      return ItemStack.field_190927_a;
   }

   public static int countItemInInv(NonNullList inv, ItemStack stackToSearchFor, int startIndex, int endIndex) {
      int count = 0;

      for(int i = startIndex; i < endIndex; ++i) {
         if (!((ItemStack)inv.get(i)).func_190926_b() && ((ItemStack)inv.get(i)).func_77973_b() == stackToSearchFor.func_77973_b() && ((ItemStack)inv.get(i)).func_77952_i() == stackToSearchFor.func_77952_i()) {
            count += ((ItemStack)inv.get(i)).func_190916_E();
         }
      }

      return count;
   }

   public static void dropInventoryItems(World worldIn, BlockPos pos, ItemStackHandler inventory) {
      double x = (double)pos.func_177958_n() + 0.5D;
      double y = (double)pos.func_177956_o() + 0.5D;
      double z = (double)pos.func_177952_p() + 0.5D;

      for(int i = 0; i < inventory.getSlots(); ++i) {
         ItemStack itemstack = inventory.getStackInSlot(i);
         if (!itemstack.func_190926_b()) {
            InventoryHelper.func_180173_a(worldIn, x, y, z, itemstack);
         }
      }

   }
}
