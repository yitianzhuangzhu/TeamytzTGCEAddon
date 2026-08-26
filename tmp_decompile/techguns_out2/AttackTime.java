package techguns.api.capabilities;

public class AttackTime {
   private long reloadTime = 0L;
   private int reloadTimeTotal = 0;
   private long recoilTime = 0L;
   private int recoilTimeTotal = 0;
   private byte attackType = 0;
   private float recoilChargeProgress = 0.0F;
   private long muzzleFlashTime = 0L;
   private int muzzleFlashTimeTotal = 0;

   public long getReloadTime() {
      return this.reloadTime;
   }

   public void setReloadTime(long reloadTime) {
      this.reloadTime = reloadTime;
   }

   public int getReloadTimeTotal() {
      return this.reloadTimeTotal;
   }

   public void setReloadTimeTotal(int reloadTimeTotal) {
      this.reloadTimeTotal = reloadTimeTotal;
   }

   public long getRecoilTime() {
      return this.recoilTime;
   }

   public void setRecoilTime(long recoilTime) {
      this.recoilTime = recoilTime;
   }

   public int getRecoilTimeTotal() {
      return this.recoilTimeTotal;
   }

   public void setRecoilTimeTotal(int recoilTimeTotal) {
      this.recoilTimeTotal = recoilTimeTotal;
   }

   public byte getAttackType() {
      return this.attackType;
   }

   public void setAttackType(byte attackType) {
      this.attackType = attackType;
   }

   public long getMuzzleFlashTime() {
      return this.muzzleFlashTime;
   }

   public void setMuzzleFlashTime(long muzzleFlashTime) {
      this.muzzleFlashTime = muzzleFlashTime;
   }

   public int getMuzzleFlashTimeTotal() {
      return this.muzzleFlashTimeTotal;
   }

   public void setMuzzleFlashTimeTotal(int muzzleFlashTimeTotal) {
      this.muzzleFlashTimeTotal = muzzleFlashTimeTotal;
   }

   public float getRecoilChargeProgress() {
      return this.recoilChargeProgress;
   }

   public void setRecoilChargeProgress(float recoilChargeProgress) {
      this.recoilChargeProgress = recoilChargeProgress;
   }

   public boolean isRecoiling() {
      return this.recoilTime > 0L;
   }

   public boolean isReloading() {
      return this.reloadTime > 0L;
   }
}
