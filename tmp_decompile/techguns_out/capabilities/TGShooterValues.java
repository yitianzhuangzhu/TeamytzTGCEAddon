package techguns.capabilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.api.capabilities.AttackTime;
import techguns.api.capabilities.ITGShooterValues;
import techguns.client.particle.ITGParticle;
import techguns.client.particle.ITGParticleAttachments;

public class TGShooterValues implements ITGShooterValues, ITGParticleAttachments {
   protected AttackTime attackTime = new AttackTime();
   protected List entityParticles = null;
   protected List entityParticlesMH = null;
   protected List entityParticlesOH = null;
   protected List particleSysMH = null;
   protected List particleSysOH = null;

   public AttackTime getAttackTime(boolean offHand) {
      return this.attackTime;
   }

   public static TGShooterValues get(EntityLivingBase ent) {
      return (TGShooterValues)ent.getCapability(TGShooterValuesCapProvider.TG_SHOOTER_VALUES, (EnumFacing)null);
   }

   public boolean isRecoiling(boolean offHand) {
      return this.attackTime.isRecoiling();
   }

   public boolean isReloading(boolean offHand) {
      return this.attackTime.isReloading();
   }

   @SideOnly(Side.CLIENT)
   public void tickParticles() {
      if (this.entityParticles != null) {
         Iterator it = this.entityParticles.iterator();

         while(it.hasNext()) {
            ITGParticle p = (ITGParticle)it.next();
            p.updateTick();
            if (p.shouldRemove()) {
               it.remove();
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public List getEntityParticles() {
      return this.entityParticles;
   }

   @SideOnly(Side.CLIENT)
   public List getParticleSysMainhand() {
      return this.particleSysMH;
   }

   @SideOnly(Side.CLIENT)
   public List getParticleSysOffhand() {
      return this.particleSysOH;
   }

   @SideOnly(Side.CLIENT)
   public List getOrInitParticleSysMainhand() {
      if (this.particleSysMH == null) {
         this.particleSysMH = new LinkedList();
      }

      return this.particleSysMH;
   }

   @SideOnly(Side.CLIENT)
   public List getOrInitParticleSysOffhand() {
      if (this.particleSysOH == null) {
         this.particleSysOH = new LinkedList();
      }

      return this.particleSysOH;
   }

   public List getEntityParticlesMH() {
      return this.entityParticlesMH;
   }

   public List getEntityParticlesOH() {
      return this.entityParticlesOH;
   }

   public List getOrInitEntityParticlesOH() {
      if (this.entityParticlesOH == null) {
         this.entityParticlesOH = new LinkedList();
      }

      return this.entityParticlesOH;
   }

   public List getOrInitEntityParticlesMH() {
      if (this.entityParticlesMH == null) {
         this.entityParticlesMH = new LinkedList();
      }

      return this.entityParticlesMH;
   }
}
