package techguns.client.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import techguns.api.capabilities.AttackTime;
import techguns.api.capabilities.ITGShooterValues;
import techguns.api.npc.INPCTechgunsShooter;
import techguns.capabilities.TGExtendedPlayer;
import techguns.capabilities.TGShooterValues;
import techguns.client.ClientProxy;
import techguns.client.models.ModelMultipart;
import techguns.client.render.fx.IScreenEffect;
import techguns.items.guns.GenericGun;
import techguns.items.guns.GenericGunCharge;

public class RenderGunBase extends RenderItemBase {
   protected IScreenEffect muzzleFX = null;
   protected IScreenEffect scope = null;
   protected float muzzleFX_x_l = -1.0F;
   protected float muzzleFX_x_r = 0.0F;
   protected float muzzleFX_y = 0.0F;
   protected float muzzleFX_z = 0.0F;
   protected float muzzleFX_scale = 1.0F;
   protected float muzzleFX_3p_y = 0.0F;
   protected float muzzleFX_3p_z = 0.0F;
   protected float muzzleFX_3p_scale = 0.5F;
   protected float mf_jitterX = 0.0F;
   protected float mf_jitterY = 0.0F;
   protected float mf_jitterAngle = 0.0F;
   protected float mf_jitterScale = 0.0F;
   protected GunAnimation recoilAnim = GunAnimation.genericRecoil;
   protected float[] recoilParams = new float[]{0.15F, 5.0F};
   protected GunAnimation reloadAnim = GunAnimation.genericReload;
   protected float[] reloadParams = new float[]{1.0F, 40.0F};
   protected GunAnimation scopeRecoilAnim = null;
   protected float[] scopeRecoilParams = null;
   protected GunAnimation recoilAnim3p = GunAnimation.genericRecoil;
   protected float[] recoilParams3p = new float[]{0.0F, 5.0F};
   protected GunAnimation reloadAnim3p = GunAnimation.genericReload;
   protected float[] reloadParams3p = new float[]{0.0F, 40.0F};
   protected float scopescale = 3.0F;
   protected float chargeTranslation = 0.25F;

   public RenderGunBase(ModelMultipart model, int parts) {
      super(model, (ResourceLocation)null);
      this.parts = parts;
      this.scale_ground = this.scale_thirdp;
   }

   public RenderGunBase setBaseTranslation(float x, float y, float z) {
      return (RenderGunBase)super.setBaseTranslation(x, y, z);
   }

   public RenderGunBase setGUIScale(float guiscale) {
      return (RenderGunBase)super.setGUIScale(guiscale);
   }

   public RenderGunBase setReloadAnim(GunAnimation anim, float... params) {
      this.reloadAnim = anim;
      this.reloadParams = params;
      return this;
   }

   public RenderGunBase setRecoilAnim(GunAnimation anim, float... params) {
      this.recoilAnim = anim;
      this.recoilParams = params;
      return this;
   }

   public RenderGunBase setScopeRecoilAnim(GunAnimation anim, float... params) {
      this.scopeRecoilAnim = anim;
      this.scopeRecoilParams = params;
      return this;
   }

   public RenderGunBase setReloadAnim3p(GunAnimation anim, float... params) {
      this.reloadAnim3p = anim;
      this.reloadParams3p = params;
      return this;
   }

   public RenderGunBase setChargeTranslationAmount(float value) {
      this.chargeTranslation = value;
      return this;
   }

   public RenderGunBase setTransformTranslations(float[][] translations) {
      return (RenderGunBase)super.setTransformTranslations(translations);
   }

   public RenderGunBase setMuzzleFx(IScreenEffect muzzleFX, float x, float y, float z, float scale, float x_l) {
      this.muzzleFX = muzzleFX;
      this.muzzleFX_x_r = x;
      this.muzzleFX_x_l = x_l;
      this.muzzleFX_y = y;
      this.muzzleFX_z = z;
      this.muzzleFX_scale = scale;
      return this;
   }

   public RenderGunBase setMuzzleFXPos3P(float y, float z) {
      this.muzzleFX_3p_y = y;
      this.muzzleFX_3p_z = z;
      return this;
   }

   public RenderGunBase setScope(IScreenEffect scope) {
      this.scope = scope;
      return this;
   }

   public RenderGunBase setScope(IScreenEffect scope, float scale) {
      this.scope = scope;
      this.scopescale = scale;
      return this;
   }

   public boolean hasScopeTexture() {
      return this.scope != null;
   }

   protected static ITGShooterValues getShooterValues(EntityLivingBase ent) {
      ITGShooterValues values = null;
      if (ent != null) {
         if (ent instanceof EntityPlayer) {
            values = TGExtendedPlayer.get((EntityPlayer)ent);
         } else {
            values = TGShooterValues.get(ent);
         }
      }

      return values;
   }

   public void renderItem(@NotNull ItemCameraTransforms.@NotNull TransformType transform, @NotNull ItemStack stack, EntityLivingBase entityIn, boolean leftHand) {
      GenericGun gun = (GenericGun)stack.func_77973_b();
      ITGShooterValues values = getShooterValues(entityIn);
      boolean sneaking = false;
      boolean isOffhand = false;
      if (entityIn != null) {
         sneaking = entityIn.func_70093_af();
         isOffhand = !leftHand && entityIn.func_184591_cq() == EnumHandSide.LEFT || leftHand && entityIn.func_184591_cq() == EnumHandSide.RIGHT;
      }

      GlStateManager.func_179094_E();
      GlStateManager.func_179129_p();
      GlStateManager.func_179109_b(0.5F, 0.5F, 0.5F);
      float fireProgress = 0.0F;
      float reloadProgress = 0.0F;
      float muzzleFlashProgress = 0.0F;
      float chargeProgress = 0.0F;
      boolean renderScope = false;
      if (transform == TransformType.FIRST_PERSON_LEFT_HAND || transform == TransformType.THIRD_PERSON_LEFT_HAND) {
         GlStateManager.func_179109_b(-1.0F, 0.0F, 0.0F);
      }

      if (values != null && (TransformType.FIRST_PERSON_LEFT_HAND == transform || TransformType.FIRST_PERSON_RIGHT_HAND == transform || TransformType.THIRD_PERSON_LEFT_HAND == transform || TransformType.THIRD_PERSON_RIGHT_HAND == transform)) {
         AttackTime attack = values.getAttackTime(isOffhand);
         if (entityIn != null && gun.canCharge() && !isOffhand && !entityIn.func_184607_cu().func_190926_b()) {
            int dur = stack.func_77973_b().func_77626_a(stack) - entityIn.func_184605_cv();
            chargeProgress = (float)dur / ((GenericGunCharge)stack.func_77973_b()).fullChargeTime;
            if (chargeProgress < 0.0F) {
               chargeProgress = 0.0F;
            } else if (chargeProgress > 1.0F) {
               chargeProgress = 1.0F;
            }
         } else if (attack.isReloading()) {
            long diff = attack.getReloadTime() - System.currentTimeMillis();
            if (diff <= 0L) {
               attack.setReloadTime(0L);
               attack.setReloadTimeTotal(0);
               attack.setAttackType((byte)0);
            } else {
               reloadProgress = 1.0F - (float)diff / (float)attack.getReloadTimeTotal();
            }
         } else if (attack.isRecoiling()) {
            long diff = attack.getRecoilTime() - System.currentTimeMillis();
            if (diff <= 0L) {
               attack.setRecoilTime(0L);
               attack.setRecoilTimeTotal(0);
               attack.setAttackType((byte)0);
               attack.setRecoilChargeProgress(0.0F);
            } else {
               fireProgress = 1.0F - (float)diff / (float)attack.getRecoilTimeTotal();
               if (gun.canCharge()) {
                  chargeProgress = (1.0F - fireProgress) * attack.getRecoilChargeProgress();
               }
            }
         }

         if (attack.getMuzzleFlashTime() > 0L) {
            long diff = attack.getMuzzleFlashTime() - System.currentTimeMillis();
            if (diff > 0L && diff <= (long)attack.getMuzzleFlashTimeTotal()) {
               muzzleFlashProgress = 1.0F - (float)diff / (float)attack.getMuzzleFlashTimeTotal();
            } else {
               attack.setMuzzleFlashTime(0L);
               attack.setMuzzleFlashTimeTotal(0);
            }
         }
      }

      this.applyTranslation(transform);
      if (TransformType.FIRST_PERSON_LEFT_HAND != transform && TransformType.FIRST_PERSON_RIGHT_HAND != transform) {
         if (TransformType.THIRD_PERSON_LEFT_HAND != transform && TransformType.THIRD_PERSON_RIGHT_HAND != transform) {
            if (TransformType.GUI == transform) {
               this.transformGUI();
            } else if (TransformType.GROUND == transform) {
               this.transformGround();
            } else if (TransformType.FIXED == transform) {
               this.transformFixed();
            }
         } else {
            this.transformThirdPerson(entityIn, fireProgress, reloadProgress, TransformType.THIRD_PERSON_LEFT_HAND == transform);
         }
      } else if (!isOffhand && gun.isZooming() && this.scope != null) {
         renderScope = true;
      } else {
         if (!isOffhand && gun.getZoomMult() < 1.0F && gun.isZooming()) {
            this.transformADS(gun.getZoomX(), gun.getZoomY(), gun.getZoomZ());
         }

         this.transformFirstPerson(fireProgress, reloadProgress, chargeProgress, TransformType.FIRST_PERSON_LEFT_HAND == transform, sneaking && isOffhand);
      }

      if (!renderScope) {
         this.setBaseScale(entityIn, transform);
         this.setBaseRotation(transform);
         this.applyBaseTranslation();
         GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);

         for(int i = 0; i < this.parts; ++i) {
            this.bindTextureForPart(gun, i, stack);
            this.setGLColorForPart(gun, i, stack);
            this.model.render(entityIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, gun.getAmmoLeft(stack), reloadProgress, transform, i, fireProgress, chargeProgress);
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
         }

         GlStateManager.func_179121_F();
         this.applyAnimForParticles(entityIn, reloadProgress, transform, sneaking, isOffhand);
         this.renderItemParticles(entityIn, transform, ClientProxy.get().PARTIAL_TICK_TIME);
         if (muzzleFlashProgress > 0.0F) {
            if (TransformType.FIRST_PERSON_LEFT_HAND != transform && TransformType.FIRST_PERSON_RIGHT_HAND != transform) {
               this.drawMuzzleFx3P(muzzleFlashProgress);
            } else if (!isOffhand && gun.getZoomMult() < 1.0F && gun.isZooming()) {
               this.drawMuzzleFx(muzzleFlashProgress, leftHand, gun.getZoomX(), gun.getZoomY());
            } else {
               this.drawMuzzleFx(muzzleFlashProgress, leftHand, 0.0F, 0.0F);
            }
         } else if (reloadProgress <= 0.0F) {
            if (TransformType.FIRST_PERSON_LEFT_HAND != transform && TransformType.FIRST_PERSON_RIGHT_HAND != transform) {
               this.drawIdleFx3P(leftHand);
            } else {
               this.drawIdleFx(leftHand);
            }
         }

         GlStateManager.func_179089_o();
         GlStateManager.func_179121_F();
      } else {
         GlStateManager.func_179089_o();
         GlStateManager.func_179121_F();
         if (this.scopeRecoilAnim != null && fireProgress > 0.0F) {
            this.scopeRecoilAnim.play(fireProgress, TransformType.FIRST_PERSON_LEFT_HAND == transform, this.scopeRecoilParams);
         }

         this.renderScope(fireProgress, leftHand);
      }

   }

   protected void drawIdleFx(boolean leftHand) {
   }

   protected void drawIdleFx3P(boolean leftHand) {
   }

   protected void setGLColorForPart(GenericGun gun, int part, ItemStack stack) {
   }

   public void applyAnimForParticles(EntityLivingBase entity, float reloadProgress, ItemCameraTransforms.TransformType transform, boolean sneaking, boolean isOffhand) {
      GlStateManager.func_179094_E();
      if (reloadProgress != 0.0F) {
         if (transform != TransformType.FIRST_PERSON_LEFT_HAND && transform != TransformType.FIRST_PERSON_RIGHT_HAND) {
            if (transform == TransformType.THIRD_PERSON_LEFT_HAND || transform == TransformType.THIRD_PERSON_RIGHT_HAND) {
               this.transformThirdPerson(entity, 0.0F, reloadProgress, TransformType.THIRD_PERSON_LEFT_HAND == transform);
            }
         } else {
            this.transformFirstPerson(0.0F, reloadProgress, 0.0F, TransformType.FIRST_PERSON_LEFT_HAND == transform, sneaking && isOffhand);
         }

      }
   }

   protected void transformFirstPerson(float fireProgress, float reloadProgress, float chargeProgress, boolean left, boolean shoudLowerWeapon) {
      if (chargeProgress > 0.0F) {
         GlStateManager.func_179109_b(0.0F, 0.0F, this.chargeTranslation * chargeProgress);
      }

      if (fireProgress > 0.0F) {
         this.recoilAnim.play(fireProgress, left, this.recoilParams);
      } else if (reloadProgress > 0.0F) {
         this.reloadAnim.play(reloadProgress, left, this.reloadParams);
      } else if (shoudLowerWeapon) {
         GlStateManager.func_179114_b(-35.0F, 1.0F, 0.0F, 0.0F);
      }

   }

   protected void transformThirdPerson(EntityLivingBase ent, float fireProgress, float reloadProgress, boolean left) {
      if (ent instanceof INPCTechgunsShooter) {
         INPCTechgunsShooter shooter = (INPCTechgunsShooter)ent;
         if (!shooter.hasWeaponArmPose()) {
            GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
         }
      }

      if (fireProgress > 0.0F) {
         this.recoilAnim3p.play(fireProgress, left, this.recoilParams3p);
      } else if (reloadProgress > 0.0F) {
         this.reloadAnim3p.play(reloadProgress, left, this.reloadParams3p);
      }

   }

   protected void transformGUI() {
      GlStateManager.func_179114_b(40.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(20.0F, 1.0F, 0.0F, 0.0F);
   }

   protected void transformGround() {
   }

   protected void transformFixed() {
      GlStateManager.func_179114_b(-90.0F, 0.0F, 1.0F, 0.0F);
   }

   protected void drawMuzzleFx(float progress, boolean leftHand, float zoomX, float zoomY) {
      if (this.muzzleFX != null) {
         float x = leftHand ? this.muzzleFX_x_l : this.muzzleFX_x_r;
         ClientProxy cp = ClientProxy.get();
         float scale = this.muzzleFX_scale;
         float offsetX = x + zoomX;
         float offsetY = this.muzzleFX_y + zoomY;
         if (this.mf_jitterScale > 0.0F) {
            scale += this.mf_jitterScale * cp.muzzleFlashJitterScale;
         }

         if (this.mf_jitterX > 0.0F) {
            offsetX += this.mf_jitterX * cp.muzzleFlashJitterX;
         }

         if (this.mf_jitterY > 0.0F) {
            offsetY += this.mf_jitterY * cp.muzzleFlashJitterY;
         }

         this.muzzleFX.doRender(progress, offsetX, offsetY, this.muzzleFX_z, scale, false);
      }

   }

   protected void drawMuzzleFx3P(float progress) {
      if (this.muzzleFX != null) {
         this.muzzleFX.doRender(progress, 0.0F, this.muzzleFX_3p_y, this.muzzleFX_3p_z, this.muzzleFX_scale * this.muzzleFX_3p_scale, true);
      }

   }

   protected void renderScope(float fireProgress, boolean leftHand) {
      if (this.scope != null) {
         float x = leftHand ? 0.56F : -0.56F;
         this.scope.doRender(fireProgress, x, 0.52F, 0.0F, this.scopescale, false);
      }

   }

   public RenderGunBase setBaseScale(float baseScale) {
      return (RenderGunBase)super.setBaseScale(baseScale);
   }

   protected void transformADS(float Xzoom, float Yzoom, float Zzoom) {
      GlStateManager.func_179109_b(Xzoom, Yzoom, Zzoom);
   }

   public RenderGunBase setMuzzleFlashJitter(float jX, float jY, float jAngle, float jScale) {
      this.mf_jitterX = jX;
      this.mf_jitterY = jY;
      this.mf_jitterAngle = jAngle;
      this.mf_jitterScale = jScale;
      return this;
   }

   public void bindTextureForPart(GenericGun gun, int part, ItemStack stack) {
      if (part == 0) {
         if (gun.hasCustomTexture) {
            Minecraft.func_71410_x().func_110434_K().func_110577_a(gun.getCurrentTexture(stack));
         } else {
            Minecraft.func_71410_x().func_110434_K().func_110577_a(TextureMap.field_110575_b);
         }
      }

   }

   public RenderGunBase setAmbientParticleFX(String ambientParticleFX) {
      this.ambientParticleFX = ambientParticleFX;
      return this;
   }
}
