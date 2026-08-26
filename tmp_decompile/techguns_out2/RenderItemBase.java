package techguns.client.render.item;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import techguns.api.npc.INPCTechgunsShooter;
import techguns.api.render.IItemRenderer;
import techguns.capabilities.TGExtendedPlayerClient;
import techguns.capabilities.TGShooterValues;
import techguns.client.ClientProxy;
import techguns.client.models.ModelMultipart;
import techguns.client.particle.ITGParticle;
import techguns.client.render.item.RenderItemBase.1;
import techguns.entities.npcs.NPCTurret;

public class RenderItemBase implements IItemRenderer {
   public static final float SCALE = 0.0625F;
   protected ModelMultipart model;
   protected ResourceLocation texture;
   protected float baseScale = 1.0F;
   protected float scale_thirdp = 0.35F;
   protected float scale_ground = 0.5F;
   protected float scale_ego = 0.5F;
   protected float scale_gui = 0.4F;
   protected float scale_itemframe = 0.5F;
   protected float[] translateBase = new float[]{0.0F, 0.0F, 0.0F};
   protected float[][] translateType = new float[][]{{0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -0.05F}};
   protected int parts = 1;
   protected String ambientParticleFX = null;

   public RenderItemBase(ModelMultipart model, ResourceLocation texture) {
      this.model = model;
      this.texture = texture;
   }

   public RenderItemBase setGUIScale(float guiscale) {
      this.scale_gui = guiscale;
      return this;
   }

   public RenderItemBase setFirstPersonScale(float scale) {
      this.scale_ego = scale;
      return this;
   }

   public RenderItemBase setGroundAndFrameScale(float scale) {
      this.scale_ground = scale;
      this.scale_itemframe = scale;
      return this;
   }

   public RenderItemBase setBaseTranslation(float x, float y, float z) {
      this.translateBase[0] = x;
      this.translateBase[1] = y;
      this.translateBase[2] = z;
      return this;
   }

   public RenderItemBase setTransformTranslations(float[][] translations) {
      this.translateType = translations;
      return this;
   }

   public RenderItemBase setBaseScale(float baseScale) {
      this.baseScale = baseScale;
      return this;
   }

   protected float getScaleFactorFromTransform(ItemCameraTransforms.TransformType transform) {
      switch (1.$SwitchMap$net$minecraft$client$renderer$block$model$ItemCameraTransforms$TransformType[transform.ordinal()]) {
         case 1:
         case 2:
            return this.baseScale * this.scale_ego;
         case 3:
         case 4:
            return this.baseScale * this.scale_thirdp;
         case 5:
            return this.baseScale * this.scale_gui;
         case 6:
            return this.baseScale * this.scale_ground;
         case 7:
            return this.baseScale * this.scale_itemframe;
         default:
            return this.baseScale;
      }
   }

   protected void applyTranslation(ItemCameraTransforms.TransformType transform) {
      int index = -1;
      boolean flip = false;
      switch (1.$SwitchMap$net$minecraft$client$renderer$block$model$ItemCameraTransforms$TransformType[transform.ordinal()]) {
         case 1:
            flip = true;
         case 2:
            index = 0;
            break;
         case 3:
            flip = true;
         case 4:
            index = 1;
            break;
         case 5:
            index = 2;
            break;
         case 6:
            index = 3;
            break;
         case 7:
            index = 4;
      }

      if (index >= 0) {
         float mirror = flip ? -1.0F : 1.0F;
         GlStateManager.func_179109_b(this.translateType[index][0] * mirror, this.translateType[index][1], this.translateType[index][2]);
      }

   }

   protected void applyBaseTranslation() {
      GlStateManager.func_179109_b(this.translateBase[0], this.translateBase[1], this.translateBase[2]);
   }

   public void renderItem(@NotNull ItemCameraTransforms.@NotNull TransformType transform, @NotNull ItemStack stack, EntityLivingBase elb, boolean leftHanded) {
      GlStateManager.func_179094_E();
      GlStateManager.func_179129_p();
      GlStateManager.func_179109_b(0.5F, 0.5F, 0.5F);
      Minecraft.func_71410_x().func_110434_K().func_110577_a(this.texture);
      this.applyTranslation(transform);
      if (transform == TransformType.FIRST_PERSON_LEFT_HAND || transform == TransformType.THIRD_PERSON_LEFT_HAND) {
         GlStateManager.func_179109_b(-1.0F, 0.0F, 0.0F);
      }

      if (TransformType.GUI == transform) {
         GlStateManager.func_179114_b(40.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b(20.0F, 1.0F, 0.0F, 0.0F);
      } else if (TransformType.FIXED == transform) {
         GlStateManager.func_179114_b(-90.0F, 0.0F, 1.0F, 0.0F);
      }

      this.setBaseScale(elb, transform);
      this.setBaseRotation(transform);
      this.applyBaseTranslation();

      for(int i = 0; i < this.parts; ++i) {
         this.model.render(elb, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, 0, 0.0F, transform, i, 0.0F, 0.0F);
      }

      this.renderItemParticles(elb, transform, ClientProxy.get().PARTIAL_TICK_TIME);
      GlStateManager.func_179089_o();
      GlStateManager.func_179121_F();
   }

   protected void setBaseScale(EntityLivingBase entity, ItemCameraTransforms.TransformType transform) {
      float scale = this.getScaleFactorFromTransform(transform);
      if (entity instanceof INPCTechgunsShooter) {
         INPCTechgunsShooter shooter = (INPCTechgunsShooter)entity;
         scale *= shooter.getGunScale();
      }

      GlStateManager.func_179152_a(scale, scale, scale);
   }

   protected void setBaseRotation(ItemCameraTransforms.TransformType transform) {
      GlStateManager.func_179114_b(-180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
   }

   protected void renderItemParticles(EntityLivingBase ent, ItemCameraTransforms.TransformType transform, float ptt) {
      EnumHand hand = EnumHand.MAIN_HAND;
      if (ent != null) {
         if (!(ent instanceof NPCTurret)) {
            if (transform != TransformType.FIRST_PERSON_LEFT_HAND && transform != TransformType.THIRD_PERSON_LEFT_HAND) {
               if (transform != TransformType.FIRST_PERSON_RIGHT_HAND && transform != TransformType.THIRD_PERSON_RIGHT_HAND) {
                  return;
               }

               if (ent.func_184591_cq() == EnumHandSide.LEFT) {
                  hand = EnumHand.OFF_HAND;
               }
            } else {
               GlStateManager.func_179109_b(-1.0F, 0.0F, 0.0F);
               if (ent.func_184591_cq() == EnumHandSide.RIGHT) {
                  hand = EnumHand.OFF_HAND;
               }
            }
         }

         List particles = null;
         if (ent instanceof EntityPlayer) {
            if (hand == EnumHand.MAIN_HAND) {
               particles = TGExtendedPlayerClient.get((EntityPlayer)ent).getEntityParticlesMH();
            } else {
               particles = TGExtendedPlayerClient.get((EntityPlayer)ent).getEntityParticlesOH();
            }
         } else if (ent instanceof INPCTechgunsShooter) {
            if (hand == EnumHand.MAIN_HAND) {
               particles = TGShooterValues.get(ent).getEntityParticlesMH();
            } else {
               particles = TGShooterValues.get(ent).getEntityParticlesOH();
            }
         }

         if (particles != null && !particles.isEmpty()) {
            Tessellator tessellator = Tessellator.func_178181_a();
            BufferBuilder buffer = tessellator.func_178180_c();
            particles.forEach((p) -> p.doRender(buffer, ent, ptt, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F));
         }

      }
   }

   public String getAmbientParticleFX() {
      return this.ambientParticleFX;
   }

   public RenderItemBase setAmbientParticleFX(String ambientParticleFX) {
      this.ambientParticleFX = ambientParticleFX;
      return this;
   }
}
