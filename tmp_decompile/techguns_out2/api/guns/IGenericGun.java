package techguns.api.guns;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGenericGun {
   boolean isShootWithLeftClick();

   boolean isSemiAuto();

   @SideOnly(Side.CLIENT)
   boolean isZooming();

   void shootGunPrimary(ItemStack var1, World var2, EntityPlayer var3, boolean var4, EnumHand var5, Entity var6);

   int getAmmoLeft(ItemStack var1);

   GunHandType getGunHandType();

   boolean isHoldZoom();

   float getZoomMult();

   default boolean canCharge() {
      return false;
   }

   ResourceLocation getCurrentTexture(ItemStack var1);
}
