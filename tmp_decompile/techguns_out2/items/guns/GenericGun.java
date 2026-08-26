package techguns.items.guns;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import techguns.TGPackets;
import techguns.TGSounds;
import techguns.TGuns;
import techguns.Techguns;
import techguns.api.damagesystem.DamageType;
import techguns.api.guns.GunHandType;
import techguns.api.guns.IGenericGun;
import techguns.api.render.IItemTGRenderer;
import techguns.capabilities.TGExtendedPlayer;
import techguns.client.ClientProxy;
import techguns.client.ShooterValues;
import techguns.client.audio.TGSoundCategory;
import techguns.damagesystem.TGDamageSource;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.ai.EntityAIRangedAttack;
import techguns.entities.npcs.NPCTurret;
import techguns.entities.projectiles.EnumBulletFirePos;
import techguns.entities.projectiles.GenericProjectile;
import techguns.items.GenericItem;
import techguns.items.armors.GenericArmor;
import techguns.items.armors.ICamoChangeable;
import techguns.items.armors.TGArmorBonus;
import techguns.items.guns.GenericGun.1;
import techguns.items.guns.ammo.AmmoType;
import techguns.items.guns.ammo.AmmoTypes;
import techguns.items.guns.ammo.AmmoVariant;
import techguns.items.guns.ammo.DamageModifier;
import techguns.packets.GunFiredMessage;
import techguns.packets.ReloadStartedMessage;
import techguns.plugins.crafttweaker.EnumGunStat;
import techguns.util.InventoryUtil;
import techguns.util.MathUtil;
import techguns.util.SoundUtil;
import techguns.util.TextUtil;

public class GenericGun extends GenericItem implements IGenericGun, IItemTGRenderer, ICamoChangeable {
   public static final float SOUND_DISTANCE = 4.0F;
   boolean semiAuto = false;
   int minFiretime = 4;
   int clipsize = 10;
   int reloadtime = 40;
   float damage = 2.0F;
   SoundEvent firesound = TGSounds.M4_FIRE;
   SoundEvent reloadsound = TGSounds.M4_RELOAD;
   SoundEvent firesoundStart = TGSounds.M4_FIRE;
   SoundEvent rechamberSound = null;
   int ammoCount;
   float zoomMult = 1.0F;
   boolean canZoom = false;
   boolean toggleZoom = false;
   float Xzoom = -0.4F;
   float Yzoom = 0.08F;
   float Zzoom = 0.02F;
   boolean fireCenteredZoomed = false;
   int ticksToLive = 40;
   float speed = 2.0F;
   float damageMin = 1.0F;
   float damageDropStart = 20.0F;
   float damageDropEnd = 40.0F;
   float penetration = 0.0F;
   AmmoType ammoType = AmmoTypes.PISTOL_ROUNDS;
   boolean shotgun = false;
   boolean burst = false;
   float spread = 0.015F;
   int bulletcount = 7;
   float accuracy = 0.0F;
   float projectileForwardOffset = 0.0F;
   int maxLoopDelay = 0;
   int recoiltime = 5;
   int muzzleFlashtime = 5;
   boolean silenced = false;
   boolean checkRecoil = false;
   boolean checkMuzzleFlash = false;
   float AI_attackRange = 15.0F;
   int AI_attackTime = 60;
   int AI_burstCount = 0;
   int AI_burstAttackTime = 0;
   int camoCount = 1;
   int lockOnTicks = 0;
   int lockOnPersistTicks = 0;
   EnumCrosshairStyle crossHairStyle = EnumCrosshairStyle.GUN_DYNAMIC;
   public ArrayList textures;
   protected ProjectileSelector projectile_selector;
   GunHandType handType = GunHandType.TWO_HANDED;
   int miningAmmoConsumption = 1;
   float meleeDamagePwr = 6.0F;
   float meleeDamageEmpty = 2.0F;
   float digSpeed = 1.0F;
   float zoombonus = 1.0F;
   float radius = 1.0F;
   double gravity = 0.0D;
   boolean shootWithLeftClick = true;
   public float turretPosOffsetX = 0.0F;
   public float turretPosOffsetY = 0.0F;
   public float turretPosOffsetZ = 0.0F;
   public static ArrayList guns = new ArrayList();
   public int light_lifetime = 2;
   public float light_radius_start = 3.0F;
   public float light_radius_end = 3.0F;
   public float light_r = 1.0F;
   public float light_g = 0.9F;
   public float light_b = 0.2F;
   protected boolean muzzelight = true;
   boolean hasAimedBowAnim = true;
   boolean hasAmbientEffect = false;
   public boolean hasCustomTexture = true;
   protected RangeTooltipType rangeTooltipType = RangeTooltipType.DROP;

   private GenericGun(String name) {
      super(name, false);
      this.func_77625_d(1);
      this.setNoRepair();
   }

   public GenericGun(String name, ProjectileSelector projectileSelector, boolean semiAuto, int minFiretime, int clipsize, int reloadtime, float damage, SoundEvent firesound, SoundEvent reloadsound, int TTL, float accuracy) {
      this(true, name, projectileSelector, semiAuto, minFiretime, clipsize, reloadtime, damage, firesound, reloadsound, TTL, accuracy);
   }

   public GenericGun setMuzzleLight(float r, float g, float b) {
      this.light_r = r;
      this.light_g = g;
      this.light_b = b;
      return this;
   }

   public GenericGun setNoMuzzleLight() {
      this.muzzelight = false;
      return this;
   }

   public GenericGun setRangeTooltipType(RangeTooltipType type) {
      this.rangeTooltipType = type;
      return this;
   }

   public GenericGun setHasAmbient() {
      this.hasAmbientEffect = true;
      return this;
   }

   public boolean hasAmbientEffect() {
      return this.hasAmbientEffect;
   }

   public void setNoBowAnim() {
      this.hasAimedBowAnim = false;
   }

   public boolean hasBowAnim() {
      return this.hasAimedBowAnim;
   }

   public GenericGun(boolean addToGunList, String name, ProjectileSelector projectile_selector, boolean semiAuto, int minFiretime, int clipsize, int reloadtime, float damage, SoundEvent firesound, SoundEvent reloadsound, int TTL, float accuracy) {
      this(name);
      this.func_77656_e(clipsize);
      this.ammoType = projectile_selector.ammoType;
      this.semiAuto = semiAuto;
      this.minFiretime = minFiretime;
      this.clipsize = clipsize;
      this.reloadtime = reloadtime;
      this.damage = damage;
      this.firesound = firesound;
      this.reloadsound = reloadsound;
      this.ammoCount = 1;
      this.ticksToLive = TTL;
      this.accuracy = accuracy;
      this.damageDropStart = (float)TTL;
      this.damageDropEnd = (float)TTL;
      this.damageMin = damage;
      this.projectile_selector = projectile_selector;
      if (addToGunList) {
         guns.add(this);
      }

   }

   public GenericGun setGravity(double grav) {
      this.gravity = grav;
      return this;
   }

   public GenericGun setAmmoCount(int count) {
      this.ammoCount = count;
      return this;
   }

   public GenericGun setZoom(float mult, boolean toggle, float bonus, boolean fireCenteredWhileZooming) {
      this.canZoom = true;
      this.zoomMult = mult;
      this.toggleZoom = toggle;
      this.zoombonus = bonus;
      this.fireCenteredZoomed = fireCenteredWhileZooming;
      return this;
   }

   public GenericGun setShotgunSpread(int count, float spread, boolean burst) {
      this.shotgun = true;
      this.spread = spread;
      this.bulletcount = count;
      this.burst = burst;
      return this;
   }

   public GenericGun setForwardOffset(float offset) {
      this.projectileForwardOffset = offset;
      return this;
   }

   public GenericGun setBulletSpeed(float speed) {
      this.speed = speed;
      return this;
   }

   public GenericGun setPenetration(float pen) {
      this.penetration = pen;
      return this;
   }

   public GenericGun setMuzzleFlashTime(int time) {
      this.muzzleFlashtime = time;
      return this;
   }

   public GenericGun setFiresoundStart(SoundEvent firesoundStart) {
      this.firesoundStart = firesoundStart;
      return this;
   }

   public GenericGun setMaxLoopDelay(int maxLoopDelay) {
      this.maxLoopDelay = maxLoopDelay;
      return this;
   }

   public GenericGun setRecoiltime(int recoiltime) {
      this.recoiltime = recoiltime;
      return this;
   }

   public GenericGun setRechamberSound(SoundEvent rechamberSound) {
      this.rechamberSound = rechamberSound;
      return this;
   }

   public GenericGun setTurretPosOffset(float x, float y, float z) {
      this.turretPosOffsetX = x;
      this.turretPosOffsetY = y;
      this.turretPosOffsetZ = z;
      return this;
   }

   protected int getScaledTTL() {
      return (int)Math.ceil((double)((float)this.ticksToLive / this.speed));
   }

   public GenericGun setLockOn(int ticks, int lockExtraTicks) {
      this.lockOnTicks = ticks;
      this.lockOnPersistTicks = lockExtraTicks;
      return this;
   }

   public int getLockOnTicks() {
      return this.lockOnTicks;
   }

   public void gunSecondaryAction(EntityPlayer player, ItemStack stack) {
      if (player.field_70170_p.field_72995_K && this.canZoom && this.toggleZoom && !ShooterValues.getPlayerIsReloading(player, false)) {
         ClientProxy cp = ClientProxy.get();
         if (cp.player_zoom != 1.0F) {
            cp.player_zoom = 1.0F;
         } else {
            cp.player_zoom = this.zoomMult;
         }
      }

   }

   public boolean onEntitySwing(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack stack) {
      if (this.shootWithLeftClick) {
         return true;
      } else {
         return this.getCurrentAmmo(stack) >= this.miningAmmoConsumption;
      }
   }

   public @NotNull ActionResult func_77659_a(@NotNull World worldIn, @NotNull EntityPlayer playerIn, @NotNull EnumHand handIn) {
      this.gunSecondaryAction(playerIn, playerIn.func_184586_b(handIn));
      return new ActionResult(EnumActionResult.PASS, playerIn.func_184586_b(handIn));
   }

   public ItemStack[] getReloadItem(ItemStack stack) {
      return this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack));
   }

   public int getAmmoCount() {
      return this.ammoCount;
   }

   public int getAmmoLeftCountTooltip(ItemStack item) {
      int ammo = this.getCurrentAmmo(item);
      return this.burst ? ammo * (this.bulletcount + 1) : ammo;
   }

   public int getClipsizeTooltip() {
      return this.burst ? this.clipsize * (this.bulletcount + 1) : this.clipsize;
   }

   protected void spawnProjectile(World world, EntityLivingBase player, ItemStack itemstack, float spread, float offset, float damagebonus, EnumBulletFirePos firePos, Entity target) {
      IProjectileFactory projectile = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(itemstack));
      GenericProjectile proj = projectile.createProjectile(this, world, player, this.damage * damagebonus, this.speed, this.getScaledTTL(), spread, this.damageDropStart, this.damageDropEnd, this.damageMin * damagebonus, this.penetration, getDoBlockDamage(player), firePos, this.radius, this.gravity);
      float f = 1.0F;
      if (this.muzzelight) {
         Techguns.proxy.createLightPulse(proj.field_70165_t + player.func_70040_Z().field_72450_a * (double)f, proj.field_70163_u + player.func_70040_Z().field_72448_b * (double)f, proj.field_70161_v + player.func_70040_Z().field_72449_c * (double)f, this.light_lifetime, this.light_radius_start, this.light_radius_end, this.light_r, this.light_g, this.light_b);
      }

      if (this.silenced) {
         proj.setSilenced();
      }

      if (offset > 0.0F) {
         proj.shiftForward(offset / this.speed);
      }

      world.func_72838_d(proj);
   }

   public static boolean getDoBlockDamage(EntityLivingBase elb) {
      boolean blockdamage = false;
      if (elb instanceof EntityPlayer) {
         TGExtendedPlayer caps = TGExtendedPlayer.get((EntityPlayer)elb);
         if (caps != null) {
            blockdamage = !caps.enableSafemode;
         }
      }

      return blockdamage;
   }

   public boolean isShootWithLeftClick() {
      return this.shootWithLeftClick;
   }

   public boolean isSemiAuto() {
      return this.semiAuto;
   }

   public GenericGun setCheckRecoil() {
      this.checkRecoil = true;
      return this;
   }

   public GenericGun setCheckMuzzleFlash() {
      this.checkMuzzleFlash = true;
      return this;
   }

   public boolean isZooming() {
      return ClientProxy.get().player_zoom == this.zoomMult;
   }

   public void shootGunPrimary(ItemStack stack, World world, EntityPlayer player, boolean zooming, EnumHand hand, Entity target) {
      int ammo = this.getCurrentAmmo(stack);
      byte ATTACK_TYPE = 0;
      TGExtendedPlayer extendedPlayer = TGExtendedPlayer.get(player);
      if (ammo > 0) {
         int firedelay = extendedPlayer.getFireDelay(hand);
         if (firedelay <= 0) {
            extendedPlayer.setFireDelay(hand, this.minFiretime);
            if (!player.field_71075_bZ.field_75098_d) {
               this.useAmmo(stack, 1);
            }

            if (!world.field_72995_K) {
               float accuracybonus = MathUtil.clamp(1.0F - GenericArmor.getArmorBonusForPlayer(player, TGArmorBonus.GUN_ACCURACY, false), 0.0F, 1.0F);
               if (hand == EnumHand.MAIN_HAND && player.func_184585_cz()) {
                  if (this.handType == GunHandType.ONE_HANDED) {
                     accuracybonus *= 4.0F;
                  } else {
                     accuracybonus *= 8.0F;
                  }
               }

               EnumBulletFirePos firePos;
               if ((hand != EnumHand.MAIN_HAND || player.func_184591_cq() != EnumHandSide.RIGHT) && (hand != EnumHand.OFF_HAND || player.func_184591_cq() != EnumHandSide.LEFT)) {
                  firePos = EnumBulletFirePos.LEFT;
               } else {
                  firePos = EnumBulletFirePos.RIGHT;
               }

               if (zooming) {
                  accuracybonus *= this.zoombonus;
                  if (this.fireCenteredZoomed) {
                     firePos = EnumBulletFirePos.CENTER;
                  } else if (player.func_184591_cq() == EnumHandSide.RIGHT && hand == EnumHand.MAIN_HAND) {
                     firePos = EnumBulletFirePos.ZOOMED;
                  }
               }

               this.shootGun(world, player, stack, accuracybonus, 1.0F, ATTACK_TYPE, hand, firePos, target);
            } else {
               int recoiltime_l = (int)((float)this.recoiltime / 20.0F * 1000.0F);
               int muzzleFlashtime_l = (int)((float)this.muzzleFlashtime / 20.0F * 1000.0F);
               if (!this.checkRecoil || !ShooterValues.isStillRecoiling(player, hand == EnumHand.OFF_HAND, ATTACK_TYPE)) {
                  ShooterValues.setRecoiltime(player, hand == EnumHand.OFF_HAND, System.currentTimeMillis() + (long)recoiltime_l, recoiltime_l, ATTACK_TYPE);
               }

               ClientProxy cp = ClientProxy.get();
               if (!this.checkMuzzleFlash || ShooterValues.getMuzzleFlashTime(player, hand == EnumHand.OFF_HAND) <= System.currentTimeMillis()) {
                  ShooterValues.setMuzzleFlashTime(player, hand == EnumHand.OFF_HAND, System.currentTimeMillis() + (long)muzzleFlashtime_l, muzzleFlashtime_l);
                  Random rand = world.field_73012_v;
                  cp.muzzleFlashJitterX = 1.0F - rand.nextFloat() * 2.0F;
                  cp.muzzleFlashJitterY = 1.0F - rand.nextFloat() * 2.0F;
                  cp.muzzleFlashJitterScale = 1.0F - rand.nextFloat() * 2.0F;
                  cp.muzzleFlashJitterAngle = 1.0F - rand.nextFloat() * 2.0F;
               }

               this.client_weaponFired();
            }

            if (this.maxLoopDelay > 0 && extendedPlayer.getLoopSoundDelay(hand) <= 0) {
               SoundUtil.playSoundOnEntityGunPosition(world, player, this.firesoundStart, 4.0F, 1.0F, false, false, TGSoundCategory.GUN_FIRE);
               extendedPlayer.setLoopSoundDelay(hand, this.maxLoopDelay);
            } else {
               SoundUtil.playSoundOnEntityGunPosition(world, player, this.firesound, 4.0F, 1.0F, false, false, TGSoundCategory.GUN_FIRE);
               if (this.maxLoopDelay > 0) {
                  extendedPlayer.setLoopSoundDelay(hand, this.maxLoopDelay);
               }
            }

            if (this.rechamberSound != null) {
               SoundUtil.playSoundOnEntityGunPosition(world, player, this.rechamberSound, 1.0F, 1.0F, false, false, TGSoundCategory.RELOAD);
            }
         }
      } else if (InventoryUtil.consumeAmmoPlayer(player, this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack)))) {
         Arrays.stream(this.ammoType.getEmptyMag()).forEach((e) -> {
            if (!e.func_190926_b()) {
               int amount = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(e.func_77973_b(), 1, e.func_77952_i()));
               if (amount > 0 && !world.field_72995_K) {
                  player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(e.func_77973_b(), amount, e.func_77952_i())));
               }
            }

         });
         if (world.field_72995_K && this.canZoom && this.toggleZoom) {
            ClientProxy cp = ClientProxy.get();
            if (cp.player_zoom != 1.0F) {
               cp.player_zoom = 1.0F;
            }
         }

         extendedPlayer.setFireDelay(hand, this.reloadtime - this.minFiretime);
         if (this.ammoCount <= 1) {
            this.reloadAmmo(stack);
         } else {
            int i;
            for(i = 1; i < this.ammoCount && InventoryUtil.consumeAmmoPlayer(player, this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack))); ++i) {
            }

            this.reloadAmmo(stack, i);
         }

         SoundUtil.playReloadSoundOnEntity(world, player, this.reloadsound, 1.0F, 1.0F, false, true, TGSoundCategory.RELOAD);
         if (world.field_72995_K) {
            int time = (int)((float)this.reloadtime / 20.0F * 1000.0F);
            ShooterValues.setReloadtime(player, hand == EnumHand.OFF_HAND, System.currentTimeMillis() + (long)time, time, ATTACK_TYPE);
            this.client_startReload();
         } else {
            int msg_reloadtime = (int)((float)this.reloadtime / 20.0F * 1000.0F);
            TGPackets.wrapper.sendToAllAround(new ReloadStartedMessage(player, hand, msg_reloadtime, ATTACK_TYPE), TGPackets.targetPointAroundEnt(player, 100.0D));
         }
      } else if (!world.field_72995_K) {
      }

   }

   protected void client_weaponFired() {
   }

   protected void client_startReload() {
   }

   public GenericGun setAIStats(float attackRange, int attackTime, int burstCount, int burstAttackTime) {
      this.AI_attackRange = attackRange;
      this.AI_attackTime = attackTime;
      this.AI_burstCount = burstCount;
      this.AI_burstAttackTime = burstAttackTime;
      return this;
   }

   public GenericGun setTexture(String path) {
      return this.setTextures(path, 1);
   }

   public GenericGun setTextures(String path, int variations) {
      Techguns.proxy.setGunTextures(this, path, variations);
      this.camoCount = variations;
      return this;
   }

   public GenericGun setTexture(ResourceLocation path) {
      return this.setTextures(path, 1);
   }

   public GenericGun setTextures(ResourceLocation path, int variations) {
      Techguns.proxy.setGunTextures(this, path, variations);
      this.camoCount = variations;
      return this;
   }

   public ResourceLocation getCurrentTexture(ItemStack stack) {
      int camo = this.getCurrentCamoIndex(stack);
      return camo < this.textures.size() ? (ResourceLocation)this.textures.get(camo) : (ResourceLocation)this.textures.get(0);
   }

   protected void shootGun(World world, EntityLivingBase player, ItemStack itemstack, float accuracybonus, float damagebonus, int attackType, EnumHand hand, EnumBulletFirePos firePos, Entity target) {
      if (!world.field_72995_K) {
         int msg_recoiltime = (int)((float)this.recoiltime / 20.0F * 1000.0F);
         int msg_muzzleflashtime = (int)((float)this.muzzleFlashtime / 20.0F * 1000.0F);
         TGPackets.wrapper.sendToAllAround(new GunFiredMessage(player, msg_recoiltime, msg_muzzleflashtime, attackType, this.checkRecoil, hand == EnumHand.OFF_HAND), TGPackets.targetPointAroundEnt(player, 100.0D));
      }

      this.spawnProjectile(world, player, itemstack, this.accuracy * accuracybonus, this.projectileForwardOffset, damagebonus, firePos, target);
      if (this.shotgun) {
         float offset = 0.0F;
         if (this.burst) {
            offset = this.speed / (float)this.bulletcount;
         }

         for(int i = 0; i < this.bulletcount; ++i) {
            this.spawnProjectile(world, player, itemstack, this.spread * accuracybonus, this.projectileForwardOffset + offset * ((float)i + 1.0F), damagebonus, firePos, target);
         }
      }

   }

   public void func_77622_d(ItemStack stack, @NotNull World world, @NotNull EntityPlayer player) {
      NBTTagCompound tags = stack.func_77978_p();
      if (tags == null) {
         tags = new NBTTagCompound();
         stack.func_77982_d(tags);
         int dmg = stack.func_77952_i();
         tags.func_74774_a("camo", (byte)0);
         tags.func_74778_a("ammovariant", "default");
         tags.func_74777_a("ammo", dmg == 0 ? (short)this.clipsize : (short)(this.clipsize - dmg));
         stack.func_77964_b(0);
         this.addInitialTags(tags);
      } else {
         stack.func_77964_b(0);
      }

   }

   protected void addInitialTags(NBTTagCompound tags) {
   }

   public int getCurrentAmmo(ItemStack stack) {
      NBTTagCompound tags = stack.func_77978_p();
      if (tags == null) {
         this.func_77622_d(stack, (World)null, (EntityPlayer)null);
         tags = stack.func_77978_p();
      }

      return tags.func_74765_d("ammo");
   }

   public int getCurrentAmmoVariant(ItemStack stack) {
      String variant = this.getCurrentAmmoVariantKey(stack);
      return this.getAmmoType().getIDforVariantKey(variant);
   }

   public String getCurrentAmmoVariantKey(ItemStack stack) {
      NBTTagCompound tags = stack.func_77978_p();
      if (tags == null) {
         this.func_77622_d(stack, (World)null, (EntityPlayer)null);
         tags = stack.func_77978_p();
      }

      String var = tags.func_74779_i("ammovariant");
      return var.isEmpty() ? "default" : var;
   }

   public void setCurrentAmmoVariant(ItemStack stack, String variant) {
      NBTTagCompound tags = stack.func_77978_p();
      if (tags == null) {
         this.func_77622_d(stack, (World)null, (EntityPlayer)null);
         tags = stack.func_77978_p();
      }

      tags.func_74778_a("ammovariant", variant);
   }

   public void toggleAmmoType(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
      TGExtendedPlayer extendedPlayer = TGExtendedPlayer.get(player);
      if (extendedPlayer.getFireDelay(hand) <= 0 && this.projectile_selector.ammoType.hasMultipleVariants()) {
         int oldAmmo = this.getCurrentAmmo(item);
         int currentVariant = this.getCurrentAmmoVariant(item);
         List variants = this.projectile_selector.ammoType.getVariants();
         int nextVariant = (currentVariant + 1) % variants.size();
         String newVariantString = ((AmmoVariant)variants.get(nextVariant)).getKey();
         if (this.projectile_selector != TGuns.ROCKET_PROJECTILES && this.projectile_selector != TGuns.GUIDED_MISSILE_PROJECTILES) {
            if (oldAmmo == this.clipsize) {
               this.useAmmo(item, oldAmmo);
               int ammos = this.getAmmoType().getEmptyMag().length;

               for(int i = 0; i < ammos; ++i) {
                  if (!this.ammoType.getEmptyMag()[i].func_190926_b()) {
                     int amount = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(this.getAmmoType().getAmmo(currentVariant)[i].func_77973_b(), 1, this.getAmmoType().getAmmo(currentVariant)[i].func_77952_i()));
                     if (amount > 0 && !world.field_72995_K) {
                        player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(this.ammoType.getAmmo(currentVariant)[i].func_77973_b(), amount, this.ammoType.getAmmo(currentVariant)[i].func_77952_i())));
                     }
                  }
               }
            } else if (oldAmmo > 0) {
               this.useAmmo(item, oldAmmo);
               int ammos = this.getAmmoType().getEmptyMag().length;

               for(int i = 0; i < ammos; ++i) {
                  if (!this.ammoType.getEmptyMag()[i].func_190926_b()) {
                     int amount = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(this.ammoType.getEmptyMag()[i].func_77973_b(), 1, this.ammoType.getEmptyMag()[i].func_77952_i()));
                     if (amount > 0 && !world.field_72995_K) {
                        player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(this.ammoType.getEmptyMag()[i].func_77973_b(), amount, this.ammoType.getEmptyMag()[i].func_77952_i())));
                        int bulletsBack = (int)Math.floor((double)((float)oldAmmo / this.ammoType.getShotsPerBullet(this.clipsize, oldAmmo)));
                        if (bulletsBack > 0) {
                           int amount2 = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(this.ammoType.getBullet(currentVariant)[i].func_77973_b(), bulletsBack, this.ammoType.getBullet(currentVariant)[i].func_77952_i()));
                           if (amount2 > 0) {
                              player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(this.ammoType.getBullet(currentVariant)[i].func_77973_b(), amount2, this.ammoType.getBullet(currentVariant)[i].func_77952_i())));
                           }
                        }
                     }
                  }
               }
            }
         } else {
            int ammoCount = this.getCurrentAmmo(item);
            this.useAmmo(item, oldAmmo);

            for(int i = 0; i < ammoCount; ++i) {
               int amount = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(this.getAmmoType().getBullet(currentVariant)[0].func_77973_b(), 1, this.getAmmoType().getBullet(currentVariant)[0].func_77952_i()));
               if (amount > 0 && !world.field_72995_K) {
                  player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(this.ammoType.getBullet(currentVariant)[0].func_77973_b(), amount, this.ammoType.getBullet(currentVariant)[0].func_77952_i())));
               }
            }
         }

         this.setCurrentAmmoVariant(item, newVariantString);
         if (InventoryUtil.consumeAmmoPlayer(player, this.getReloadItem(item))) {
            extendedPlayer.setFireDelay(hand, 20);
            if (this.ammoCount <= 1) {
               this.reloadAmmo(item);
            } else {
               int i;
               for(i = 1; i < this.ammoCount - oldAmmo && InventoryUtil.consumeAmmoPlayer(player, this.ammoType.getAmmo(this.getCurrentAmmoVariant(item))); ++i) {
               }

               this.reloadAmmo(item, i);
            }

            player.field_70170_p.func_184148_a(player, player.field_70165_t, player.field_70163_u, player.field_70161_v, TGSounds.SWITCH_AMMO_TYPE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            if (world.field_72995_K) {
               this.client_startReload();
            } else {
               int msg_reloadtime = 0;
               TGPackets.wrapper.sendToAllAround(new ReloadStartedMessage(player, hand, msg_reloadtime, 0), new NetworkRegistry.TargetPoint(player.field_71093_bK, player.field_70165_t, player.field_70163_u, player.field_70161_v, 100.0D));
            }
         }
      }

   }

   public int useAmmo(ItemStack stack, int amount) {
      int ammo = this.getCurrentAmmo(stack);
      NBTTagCompound tags = stack.func_77978_p();
      if (ammo - amount >= 0) {
         tags.func_74777_a("ammo", (short)(ammo - amount));
         return amount;
      } else {
         tags.func_74777_a("ammo", (short)0);
         return ammo;
      }
   }

   public void reloadAmmo(ItemStack stack) {
      this.reloadAmmo(stack, this.clipsize);
   }

   public void reloadAmmo(ItemStack stack, int amount) {
      int ammo = this.getCurrentAmmo(stack);
      NBTTagCompound tags = stack.func_77978_p();
      tags.func_74777_a("ammo", (short)(ammo + amount));
   }

   public void func_150895_a(@NotNull CreativeTabs tab, @NotNull NonNullList items) {
      if (this.func_194125_a(tab)) {
         ItemStack gun = new ItemStack(this, 1, 0);
         this.func_77622_d(gun, (World)null, (EntityPlayer)null);
         items.add(gun);
      }

   }

   public boolean showDurabilityBar(@NotNull ItemStack stack) {
      return true;
   }

   public double getDurabilityForDisplay(@NotNull ItemStack stack) {
      return 1.0D - this.getPercentAmmoLeft(stack);
   }

   public double getPercentAmmoLeft(ItemStack stack) {
      return (double)this.getCurrentAmmo(stack) / (double)this.clipsize;
   }

   public int getAmmoLeft(ItemStack stack) {
      return this.getCurrentAmmo(stack);
   }

   public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
      return slotChanged || !oldStack.func_77969_a(newStack);
   }

   public GenericGun setDamageDrop(float start, float end, float minDamage) {
      this.damageDropStart = start;
      this.damageDropEnd = end;
      this.damageMin = minDamage;
      return this;
   }

   public GenericGun setSilenced(boolean s) {
      this.silenced = s;
      return this;
   }

   public GunHandType getGunHandType() {
      return this.handType;
   }

   public GenericGun setHandType(GunHandType type) {
      this.handType = type;
      return this;
   }

   public boolean isHoldZoom() {
      return !this.toggleZoom;
   }

   public float getZoomMult() {
      return this.zoomMult;
   }

   protected String getTooltipTextDmg(ItemStack stack, boolean expanded) {
      DamageModifier mod = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageModifier();
      float dmg = mod.getDamage(this.damage);
      if (dmg == this.damage) {
         return this.damage + (this.damageMin != this.damage ? "-" + this.damageMin : "");
      } else {
         float dmgmin = mod.getDamage(this.damageMin);
         ChatFormatting prefix = ChatFormatting.GREEN;
         String sgn = "+";
         if (dmg < this.damage) {
            prefix = ChatFormatting.RED;
            sgn = "-";
         }

         String suffix = "";
         if (expanded) {
            if (mod.getDmgMul() != 1.0F) {
               float f = mod.getDmgMul() - 1.0F;
               String x = String.format("%.0f", f * 100.0F);
               suffix = suffix + " (" + sgn + x + "%)";
            }

            if (mod.getDmgAdd() != 0.0F) {
               float add = mod.getDmgAdd();
               suffix = suffix + " (" + (add > 0.0F ? "+" : "") + String.format("%.1f", add) + ")";
            }
         }

         String sd = String.format("%.1f", dmg);
         String sm = String.format("%.1f", dmgmin);
         return prefix + sd + (dmgmin != dmg ? "-" + sm : "") + suffix;
      }
   }

   protected String getTooltipTextRange(ItemStack stack) {
      DamageModifier mod = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageModifier();
      int ttl = mod.getTTL(this.ticksToLive);
      float rangeStart = mod.getRange(this.damageDropStart);
      float rangeEnd = mod.getRange(this.damageDropEnd);
      String prefix = "";
      String suffix = "";
      if (rangeStart != this.damageDropStart) {
         String sgn = "+";
         if (rangeStart > this.damageDropStart) {
            prefix = ChatFormatting.GREEN.toString();
         } else {
            prefix = ChatFormatting.RED.toString();
            sgn = "-";
         }

         if (mod.getRangeMul() != 1.0F) {
            float f = mod.getRangeMul() - 1.0F;
            String x = String.format("%.0f", f * 100.0F);
            suffix = suffix + " (" + sgn + x + "%)";
         }

         if (mod.getRangeAdd() != 0.0F) {
            float add = mod.getRangeAdd();
            suffix = suffix + " (" + (add > 0.0F ? "+" : "") + String.format("%.1f", add) + ")";
         }
      }

      String sStart = String.format("%.1f", rangeStart);
      String sEnd = String.format("%.1f", rangeEnd);
      if (this.rangeTooltipType == RangeTooltipType.DROP) {
         return TextUtil.trans("techguns.gun.tooltip.range", new Object[0]) + ": " + prefix + sStart + "," + sEnd + "," + ttl + suffix;
      } else {
         return this.rangeTooltipType == RangeTooltipType.NO_DROP ? TextUtil.trans("techguns.gun.tooltip.range", new Object[0]) + ": " + prefix + sStart + suffix : TextUtil.trans("techguns.gun.tooltip.radius", new Object[0]) + ": " + prefix + sStart + "-" + sEnd + suffix;
      }
   }

   protected String getTooltipTextVelocity(ItemStack stack) {
      DamageModifier mod = this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageModifier();
      float velocity = mod.getVelocity(this.speed);
      String prefix = "";
      String suffix = "";
      if (velocity != this.speed) {
         String sgn = "+";
         if (velocity >= this.speed) {
            prefix = ChatFormatting.GREEN.toString();
         } else {
            prefix = ChatFormatting.RED.toString();
            sgn = "";
         }

         if (mod.getVelocityMul() != 1.0F) {
            float f = mod.getVelocityMul() - 1.0F;
            String x = String.format("%.0f", f * 100.0F);
            suffix = suffix + " (" + sgn + x + "%)";
         }

         if (mod.getVelocityAdd() != 0.0F) {
            float add = mod.getVelocityAdd();
            suffix = suffix + " (" + (add > 0.0F ? "+" : "") + String.format("%.1f", add) + ")";
         }
      }

      String sVelocity = String.format("%.1f", velocity);
      return TextUtil.trans("techguns.gun.tooltip.velocity", new Object[0]) + ": " + prefix + sVelocity + suffix;
   }

   protected void addMiningTooltip(ItemStack stack, World world, List list, ITooltipFlag flagIn, boolean longTooltip) {
   }

   public void func_77624_a(@NotNull ItemStack stack, World worldIn, @NotNull List list, @NotNull ITooltipFlag flagIn) {
      super.func_77624_a(stack, worldIn, list, flagIn);
      if (!Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54)) {
         ItemStack[] ammo = this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack));

         for(ItemStack itemStack : ammo) {
            list.add(TextUtil.trans("techguns.gun.tooltip.ammo", new Object[0]) + ": " + (this.ammoCount > 1 ? this.ammoCount + "x " : "") + ChatFormatting.WHITE + TextUtil.trans(itemStack.func_77977_a() + ".name", new Object[0]));
         }

         this.addMiningTooltip(stack, worldIn, list, flagIn, false);
         list.add(TextUtil.trans("techguns.gun.tooltip.damage", new Object[0]) + (this.shotgun ? "(x" + (this.bulletcount + 1) + ")" : "") + ": " + this.getTooltipTextDmg(stack, false));
         list.add(TextUtil.trans("techguns.gun.tooltip.shift1", new Object[0]) + " " + ChatFormatting.GREEN + TextUtil.trans("techguns.gun.tooltip.shift2", new Object[0]) + " " + ChatFormatting.GRAY + TextUtil.trans("techguns.gun.tooltip.shift3", new Object[0]));
      } else {
         list.add(TextUtil.trans("techguns.gun.tooltip.handtype", new Object[0]) + ": " + this.getGunHandType().toString());
         ItemStack[] ammo = this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack));

         for(ItemStack itemStack : ammo) {
            list.add(TextUtil.trans("techguns.gun.tooltip.ammo", new Object[0]) + ": " + (this.ammoCount > 1 ? this.ammoCount + "x " : "") + ChatFormatting.WHITE + TextUtil.trans(itemStack.func_77977_a() + ".name", new Object[0]));
         }

         this.addMiningTooltip(stack, worldIn, list, flagIn, true);
         list.add(TextUtil.trans("techguns.gun.tooltip.damageType", new Object[0]) + ": " + this.getDamageType(stack).toString());
         list.add(TextUtil.trans("techguns.gun.tooltip.damage", new Object[0]) + (this.shotgun ? "(x" + (this.bulletcount + 1) + ")" : "") + ": " + this.getTooltipTextDmg(stack, true));
         list.add(this.getTooltipTextRange(stack));
         list.add(this.getTooltipTextVelocity(stack));
         list.add(TextUtil.trans("techguns.gun.tooltip.spread", new Object[0]) + ": " + this.accuracy + ((double)this.zoombonus != 1.0D ? " Z:" + this.zoombonus * this.accuracy : ""));
         list.add(TextUtil.trans("techguns.gun.tooltip.clipsize", new Object[0]) + ": " + this.clipsize);
         list.add(TextUtil.trans("techguns.gun.tooltip.reloadTime", new Object[0]) + ": " + (float)this.reloadtime * 0.05F + "s");
         if (this.penetration > 0.0F) {
            list.add(TextUtil.trans("techguns.gun.tooltip.armorPen", new Object[0]) + ": " + String.format("%.1f", this.penetration));
         }

         if (this.canZoom) {
            list.add(TextUtil.trans("techguns.gun.tooltip.zoom", new Object[0]) + ":" + (this.toggleZoom ? "(" + TextUtil.trans("techguns.gun.tooltip.zoom.toogle", new Object[0]) + ")" : "(" + TextUtil.trans("techguns.gun.tooltip.zoom.hold", new Object[0]) + ")") + " " + TextUtil.trans("techguns.gun.tooltip.zoom.multiplier", new Object[0]) + ":" + this.zoomMult);
         }
      }

   }

   public DamageType getDamageType(ItemStack stack) {
      return this.projectile_selector.getFactoryForType(this.getCurrentAmmoVariantKey(stack)).getDamageType();
   }

   public int getCamoCount() {
      return this.camoCount;
   }

   public String getCurrentCamoName(ItemStack item) {
      NBTTagCompound tags = item.func_77978_p();
      byte camoID = 0;
      if (tags != null && tags.func_74764_b("camo")) {
         camoID = tags.func_74771_c("camo");
      }

      return camoID > 0 ? TextUtil.trans(this.func_77658_a() + ".camoname." + camoID, new Object[0]) : TextUtil.trans("techguns.item.defaultcamo", new Object[0]);
   }

   public GenericGun setShootWithLeftClick(boolean shootWithLeftClick) {
      this.shootWithLeftClick = shootWithLeftClick;
      return this;
   }

   public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull EntityPlayer player, @NotNull Entity targetEntity) {
      if (!this.shootWithLeftClick) {
         if (player.field_70170_p.field_72995_K) {
            int time = (int)((float)this.recoiltime / 20.0F * 1000.0F);
            ShooterValues.setRecoiltime(player, false, System.currentTimeMillis() + (long)time, time, (byte)0);
         }

         if (targetEntity.func_70075_an() && GenericProjectile.BULLET_TARGETS.test(targetEntity) && !targetEntity.func_85031_j(player)) {
            float f = (float)player.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
            float f1;
            if (targetEntity instanceof EntityLivingBase) {
               f1 = EnchantmentHelper.func_152377_a(player.func_184614_ca(), ((EntityLivingBase)targetEntity).func_70668_bt());
            } else {
               f1 = EnchantmentHelper.func_152377_a(player.func_184614_ca(), EnumCreatureAttribute.UNDEFINED);
            }

            float f2 = player.func_184825_o(0.5F);
            f *= 0.2F + f2 * f2 * 0.8F;
            f1 *= f2;
            player.func_184821_cY();
            if (f > 0.0F || f1 > 0.0F) {
               boolean flag = f2 > 0.9F;
               boolean flag1 = false;
               int i = 0;
               i += EnchantmentHelper.func_77501_a(player);
               if (player.func_70051_ag() && flag) {
                  player.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187721_dT, player.func_184176_by(), 1.0F, 1.0F);
                  ++i;
                  flag1 = true;
               }

               boolean flag2 = flag && player.field_70143_R > 0.0F && !player.field_70122_E && !player.func_70617_f_() && !player.func_70090_H() && !player.func_70644_a(MobEffects.field_76440_q) && !player.func_184218_aH() && targetEntity instanceof EntityLivingBase;
               flag2 = flag2 && !player.func_70051_ag();
               if (flag2) {
                  f *= 1.5F;
               }

               f += f1;
               boolean flag3 = false;
               double d0 = (double)(player.field_70140_Q - player.field_70141_P);
               if (flag && !flag2 && !flag1 && player.field_70122_E && d0 < (double)player.func_70689_ay()) {
                  flag3 = this.hasSwordSweep() && this.getAmmoLeft(stack) > 0;
               }

               float f4 = 0.0F;
               boolean flag4 = false;
               int j = EnchantmentHelper.func_90036_a(player);
               if (targetEntity instanceof EntityLivingBase) {
                  f4 = ((EntityLivingBase)targetEntity).func_110143_aJ();
                  if (j > 0 && !targetEntity.func_70027_ad()) {
                     flag4 = true;
                     targetEntity.func_70015_d(1);
                  }
               }

               double d1 = targetEntity.field_70159_w;
               double d2 = targetEntity.field_70181_x;
               double d3 = targetEntity.field_70179_y;
               TGDamageSource src = this.getMeleeDamageSource(player, stack);
               targetEntity.func_70097_a(src, f);
               boolean flag5 = src.wasSuccessful();
               if (flag5) {
                  this.consumeAmmoOnMeleeHit(player, stack);
                  if (i > 0) {
                     if (targetEntity instanceof EntityLivingBase) {
                        ((EntityLivingBase)targetEntity).func_70653_a(player, (float)i * 0.5F, (double)MathHelper.func_76126_a(player.field_70177_z * ((float)Math.PI / 180F)), (double)(-MathHelper.func_76134_b(player.field_70177_z * ((float)Math.PI / 180F))));
                     } else {
                        targetEntity.func_70024_g((double)(-MathHelper.func_76126_a(player.field_70177_z * ((float)Math.PI / 180F)) * (float)i * 0.5F), 0.1D, (double)(MathHelper.func_76134_b(player.field_70177_z * ((float)Math.PI / 180F)) * (float)i * 0.5F));
                     }

                     player.field_70159_w *= 0.6D;
                     player.field_70179_y *= 0.6D;
                     player.func_70031_b(false);
                  }

                  if (flag3) {
                     float f3 = 1.0F + EnchantmentHelper.func_191527_a(player) * f;

                     for(EntityLivingBase entitylivingbase : player.field_70170_p.func_72872_a(EntityLivingBase.class, targetEntity.func_174813_aQ().func_72314_b(1.0D, 0.25D, 1.0D))) {
                        if (entitylivingbase != player && entitylivingbase != targetEntity && !player.func_184191_r(entitylivingbase) && player.func_70068_e(entitylivingbase) < 9.0D) {
                           entitylivingbase.func_70653_a(player, 0.4F, (double)MathHelper.func_76126_a(player.field_70177_z * ((float)Math.PI / 180F)), (double)(-MathHelper.func_76134_b(player.field_70177_z * ((float)Math.PI / 180F))));
                           TGDamageSource dmgsrc = this.getMeleeDamageSource(player, stack);
                           entitylivingbase.func_70097_a(dmgsrc, f3);
                           if (dmgsrc.wasSuccessful()) {
                              this.onMeleeHitTarget(stack, entitylivingbase);
                           }
                        }
                     }

                     this.doSweepAttackEffect(player);
                  }

                  this.onMeleeHitTarget(stack, targetEntity);
                  if (targetEntity instanceof EntityPlayerMP && targetEntity.field_70133_I) {
                     ((EntityPlayerMP)targetEntity).field_71135_a.func_147359_a(new SPacketEntityVelocity(targetEntity));
                     targetEntity.field_70133_I = false;
                     targetEntity.field_70159_w = d1;
                     targetEntity.field_70181_x = d2;
                     targetEntity.field_70179_y = d3;
                  }

                  if (flag2) {
                     player.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187718_dS, player.func_184176_by(), 1.0F, 1.0F);
                     player.func_71009_b(targetEntity);
                  }

                  if (!flag2 && !flag3) {
                     if (flag) {
                        player.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187727_dV, player.func_184176_by(), 1.0F, 1.0F);
                     } else {
                        player.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187733_dX, player.func_184176_by(), 1.0F, 1.0F);
                     }
                  }

                  if (f1 > 0.0F) {
                     player.func_71047_c(targetEntity);
                  }

                  player.func_130011_c(targetEntity);
                  if (targetEntity instanceof EntityLivingBase) {
                     EnchantmentHelper.func_151384_a((EntityLivingBase)targetEntity, player);
                  }

                  EnchantmentHelper.func_151385_b(player, targetEntity);
                  ItemStack itemstack1 = player.func_184614_ca();
                  Entity entity = targetEntity;
                  if (targetEntity instanceof MultiPartEntityPart) {
                     IEntityMultiPart ientitymultipart = ((MultiPartEntityPart)targetEntity).field_70259_a;
                     if (ientitymultipart instanceof EntityLivingBase) {
                        entity = (EntityLivingBase)ientitymultipart;
                     }
                  }

                  if (!itemstack1.func_190926_b() && entity instanceof EntityLivingBase) {
                     ItemStack beforeHitCopy = itemstack1.func_77946_l();
                     itemstack1.func_77961_a((EntityLivingBase)entity, player);
                     if (itemstack1.func_190926_b()) {
                        ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
                        player.func_184611_a(EnumHand.MAIN_HAND, ItemStack.field_190927_a);
                     }
                  }

                  if (targetEntity instanceof EntityLivingBase) {
                     float f5 = f4 - ((EntityLivingBase)targetEntity).func_110143_aJ();
                     player.func_71064_a(StatList.field_188111_y, Math.round(f5 * 10.0F));
                     if (j > 0) {
                        targetEntity.func_70015_d(j * 4);
                     }

                     if (player.field_70170_p instanceof WorldServer && f5 > 2.0F) {
                        int k = (int)((double)f5 * 0.5D);
                        ((WorldServer)player.field_70170_p).func_175739_a(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.field_70165_t, targetEntity.field_70163_u + (double)(targetEntity.field_70131_O * 0.5F), targetEntity.field_70161_v, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                     }
                  }

                  player.func_71020_j(0.1F);
               } else {
                  player.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187724_dU, player.func_184176_by(), 1.0F, 1.0F);
                  if (flag4) {
                     targetEntity.func_70066_B();
                  }
               }
            }
         }
      }

      return true;
   }

   protected void onMeleeHitTarget(ItemStack stack, Entity target) {
   }

   protected void consumeAmmoOnMeleeHit(EntityLivingBase elb, ItemStack stack) {
      if (elb instanceof EntityPlayer) {
         EntityPlayer ply = (EntityPlayer)elb;
         if (ply.field_71075_bZ.field_75098_d) {
            return;
         }
      }

      this.useAmmo(stack, 1);
   }

   protected void doSweepAttackEffect(EntityPlayer player) {
      if (!player.field_70170_p.field_72995_K) {
         double d0 = (double)(-MathHelper.func_76126_a(player.field_70177_z * ((float)Math.PI / 180F)));
         double d1 = (double)MathHelper.func_76134_b(player.field_70177_z * ((float)Math.PI / 180F));
         double x = player.field_70165_t + d0;
         double y = player.field_70163_u + (double)player.field_70131_O * 0.8D;
         double z = player.field_70161_v + d1;
         this.spawnSweepParticle(player.field_70170_p, x, y, z, d0, d1);
         this.playSweepSoundEffect(player);
      }

   }

   protected void playSweepSoundEffect(EntityPlayer player) {
      player.field_70170_p.func_184148_a((EntityPlayer)null, player.field_70165_t, player.field_70163_u, player.field_70161_v, SoundEvents.field_187730_dW, player.func_184176_by(), 1.0F, 1.0F);
   }

   protected void spawnSweepParticle(World w, double x, double y, double z, double motionX, double motionZ) {
   }

   protected TGDamageSource getMeleeDamageSource(EntityPlayer player, ItemStack stack) {
      return new TGDamageSource("player", player, player, DamageType.PHYSICAL, DeathType.GORE);
   }

   protected boolean hasSwordSweep() {
      return true;
   }

   public int getMiningAmmoConsumption() {
      return this.miningAmmoConsumption;
   }

   public boolean isModelBase(ItemStack stack) {
      return this.hasCustomTexture;
   }

   public EntityAIRangedAttack getAIAttack(IRangedAttackMob shooter) {
      return new EntityAIRangedAttack(shooter, 1.0D, this.AI_attackTime / 3, this.AI_attackTime, this.AI_attackRange, this.AI_burstCount, this.AI_burstAttackTime);
   }

   public AmmoType getAmmoType() {
      return this.ammoType;
   }

   public float getAI_attackRange() {
      return this.AI_attackRange;
   }

   public boolean isFullyLoaded(ItemStack stack) {
      return this.clipsize == this.getCurrentAmmo(stack);
   }

   public boolean hasRightClickAction() {
      return this.getGunHandType() == GunHandType.TWO_HANDED && this.canZoom;
   }

   public void fireWeaponFromNPC(EntityLivingBase shooter, float dmgscale, float accscale) {
      SoundUtil.playSoundOnEntityGunPosition(shooter.field_70170_p, shooter, this.firesound, 4.0F, 1.0F, false, false, TGSoundCategory.GUN_FIRE);
      EnumBulletFirePos firePos = EnumBulletFirePos.RIGHT;
      if (shooter instanceof NPCTurret) {
         firePos = EnumBulletFirePos.CENTER;
      }

      if (!shooter.field_70170_p.field_72995_K) {
         this.shootGun(shooter.field_70170_p, shooter, shooter.func_184614_ca(), this.zoombonus * accscale, dmgscale, 0, EnumHand.MAIN_HAND, firePos, (Entity)null);
      }

   }

   public List getAmmoOnUnload(ItemStack stack) {
      List items = new ArrayList();
      int ammo = this.getCurrentAmmo(stack);
      if (this.ammoCount > 1 && this.getAmmoLeft(stack) > 0) {
         for(ItemStack s : this.getAmmoType().getBullet(this.getCurrentAmmoVariant(stack))) {
            items.add(new ItemStack(s.func_77973_b(), this.getAmmoLeft(stack), s.func_77952_i()));
         }
      } else if (!this.isFullyLoaded(stack)) {
         int amount = this.ammoType.getEmptyMag().length;

         for(int i = 0; i < amount; ++i) {
            int bulletsBack = (int)Math.floor((double)((float)ammo / this.ammoType.getShotsPerBullet(this.clipsize, ammo)));
            if (bulletsBack > 0) {
               ItemStack bullet = this.getAmmoType().getBullet(this.getCurrentAmmoVariant(stack))[i];
               items.add(new ItemStack(bullet.func_77973_b(), bulletsBack, bullet.func_77952_i()));
            }

            if (!this.ammoType.getEmptyMag()[i].func_190926_b()) {
               items.add(new ItemStack(this.ammoType.getEmptyMag()[i].func_77973_b(), 1, this.ammoType.getEmptyMag()[i].func_77952_i()));
            }
         }
      } else {
         int amount = this.ammoType.getEmptyMag().length;

         for(int i = 0; i < amount; ++i) {
            items.add(new ItemStack(this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack))[i].func_77973_b(), 1, this.ammoType.getAmmo(this.getCurrentAmmoVariant(stack))[i].func_77952_i()));
         }
      }

      return items;
   }

   public void tryForcedReload(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
      TGExtendedPlayer extendedPlayer = TGExtendedPlayer.get(player);
      if (extendedPlayer.getFireDelay(hand) <= 0 && !this.isFullyLoaded(item)) {
         int oldAmmo = this.getCurrentAmmo(item);
         if (InventoryUtil.consumeAmmoPlayer(player, this.getReloadItem(item))) {
            if (this.ammoCount <= 1) {
               this.useAmmo(item, oldAmmo);
            }

            int ammos = this.getAmmoType().getEmptyMag().length;

            for(int i = 0; i < ammos; ++i) {
               if (!this.ammoType.getEmptyMag()[i].func_190926_b()) {
                  int amount = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(this.ammoType.getEmptyMag()[i].func_77973_b(), 1, this.ammoType.getEmptyMag()[i].func_77952_i()));
                  if (amount > 0 && !world.field_72995_K) {
                     player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(this.ammoType.getEmptyMag()[i].func_77973_b(), amount, this.ammoType.getEmptyMag()[i].func_77952_i())));
                  }

                  int bulletsBack = (int)Math.floor((double)((float)oldAmmo / this.ammoType.getShotsPerBullet(this.clipsize, oldAmmo)));
                  if (bulletsBack > 0) {
                     int amount2 = InventoryUtil.addAmmoToPlayerInventory(player, new ItemStack(this.ammoType.getBullet(this.getCurrentAmmoVariant(item))[i].func_77973_b(), bulletsBack, this.ammoType.getBullet(this.getCurrentAmmoVariant(item))[i].func_77952_i()));
                     if (amount2 > 0 && !world.field_72995_K) {
                        player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, player.field_70163_u, player.field_70161_v, new ItemStack(this.ammoType.getBullet(this.getCurrentAmmoVariant(item))[i].func_77973_b(), amount2, this.ammoType.getBullet(this.getCurrentAmmoVariant(item))[i].func_77952_i())));
                     }
                  }
               }
            }

            if (world.field_72995_K && this.canZoom && this.toggleZoom) {
               ClientProxy cp = ClientProxy.get();
               if (cp.player_zoom != 1.0F) {
                  cp.player_zoom = 1.0F;
               }
            }

            extendedPlayer.setFireDelay(hand, this.reloadtime - this.minFiretime);
            if (this.ammoCount <= 1) {
               this.reloadAmmo(item);
            } else {
               int i;
               for(i = 1; i < this.ammoCount - oldAmmo && InventoryUtil.consumeAmmoPlayer(player, this.ammoType.getAmmo(this.getCurrentAmmoVariant(item))); ++i) {
               }

               this.reloadAmmo(item, i);
            }

            SoundUtil.playReloadSoundOnEntity(world, player, this.reloadsound, 1.0F, 1.0F, false, true, TGSoundCategory.RELOAD);
            if (world.field_72995_K) {
               int time = (int)((float)this.reloadtime / 20.0F * 1000.0F);
               ShooterValues.setReloadtime(player, hand == EnumHand.OFF_HAND, System.currentTimeMillis() + (long)time, time, (byte)0);
               this.client_startReload();
            } else {
               int msg_reloadtime = (int)((float)this.reloadtime / 20.0F * 1000.0F);
               TGPackets.wrapper.sendToAllAround(new ReloadStartedMessage(player, hand, msg_reloadtime, 0), new NetworkRegistry.TargetPoint(player.field_71093_bK, player.field_70165_t, player.field_70163_u, player.field_70161_v, 100.0D));
            }
         }
      }

   }

   public int getClipsize() {
      return this.clipsize;
   }

   public float getZoomX() {
      return this.Xzoom;
   }

   public float getZoomY() {
      return this.Yzoom;
   }

   public float getZoomZ() {
      return this.Zzoom;
   }

   public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
      return this.handType != GunHandType.TWO_HANDED;
   }

   public boolean setGunStat(EnumGunStat stat, float value) {
      boolean var10000;
      switch (1.$SwitchMap$techguns$plugins$crafttweaker$EnumGunStat[stat.ordinal()]) {
         case 1:
            this.damage = value;
            var10000 = true;
            break;
         case 2:
            this.damageMin = value;
            var10000 = true;
            break;
         case 3:
            this.damageDropStart = value;
            var10000 = true;
            break;
         case 4:
            this.damageDropEnd = value;
            var10000 = true;
            break;
         case 5:
            this.speed = value;
            var10000 = true;
            break;
         case 6:
            this.ticksToLive = (int)value;
            var10000 = true;
            break;
         case 7:
            this.gravity = (double)value;
            var10000 = true;
            break;
         case 8:
            this.spread = value;
            var10000 = true;
            break;
         case 9:
            this.penetration = value;
            var10000 = true;
            break;
         case 10:
            this.clipsize = (int)value;
            var10000 = true;
            break;
         case 11:
            this.ammoCount = (int)value;
            var10000 = true;
            break;
         case 12:
            this.bulletcount = (int)value;
            var10000 = true;
            break;
         case 13:
            this.reloadtime = (int)value;
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public float getSpread() {
      return this.spread;
   }

   public GunHandType getHandType() {
      return this.handType;
   }

   public float getZoombonus() {
      return this.zoombonus;
   }

   public EnumCrosshairStyle getCrossHairStyle() {
      return this.crossHairStyle;
   }

   public GenericGun setCrossHair(EnumCrosshairStyle crosshair) {
      this.crossHairStyle = crosshair;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomMeshDefinition(this, (stack) -> new ModelResourceLocation(this.getModelLocation(), "inventory"));
   }
}
