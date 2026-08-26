package techguns.client.render.item;

import java.util.ArrayList;
import java.util.List;

public class GunAnimation {
   static GunAnimation.ICurveType C_LINEAR = (x) -> x;
   static GunAnimation.ICurveType C_FAST = (x) -> 1.0F - (1.0F - x) * (1.0F - x);
   static GunAnimation.ICurveType C_SMOOTH = (x) -> {
      double d = Math.sin(Math.PI * (double)x * 0.5D);
      return (float)(d * d);
   };
   static GunAnimation.ICurveType C_SINUS = (x) -> (float)Math.sin(Math.PI * (double)x * 2.0D);
   public static GunAnimation genericRecoil = (new GunAnimation()).addTranslate(0.0F, 0.0F, 1.0F).addRotate(1.0F, 1.0F, 0.0F, 0.0F).addSegment(0.0F, 0.25F, C_FAST, 0.0F, 1.0F).addSegment(0.25F, 1.0F, C_SMOOTH, 1.0F, 0.0F);
   public static GunAnimation swayRecoil = (new GunAnimation()).addTranslate(1.0F, 0.75F, 0.5F).addRotate(1.0F, 1.0F, -0.75F, 0.25F).addSegment(0.0F, 1.0F, C_SINUS, 0.0F, 1.0F);
   public static GunAnimation genericReload = (new GunAnimation()).addTranslate(-0.15F, -0.05F, 0.15F).addRotate(1.0F, -0.5F, 1.0F, -0.25F).addSegment(0.0F, 0.2F, C_SMOOTH, 0.0F, 1.0F).addSegment(0.2F, 0.8F, C_LINEAR, 1.0F, 1.0F).addSegment(0.8F, 1.0F, C_SMOOTH, 1.0F, 0.0F);
   public static GunAnimation breechReload = (new GunAnimation()).addTranslate(0.0F, 1.0F, -1.0F).addRotate(1.0F, -1.0F, 0.0F, 0.0F).addSegment(0.0F, 0.25F, C_SMOOTH, 0.0F, 1.0F).addSegment(0.25F, 0.5F, C_LINEAR, 1.0F, 1.0F).addSegment(0.5F, 0.85F, C_SMOOTH, 1.0F, -0.25F).addSegment(0.85F, 1.0F, C_SMOOTH, -0.25F, 0.0F);
   public static GunAnimation scopeRecoil = (new GunAnimation()).addTranslate(0.25F, 1.0F, 0.75F).addRotate(1.0F, 1.0F, 0.0F, 0.0F).addSegment(0.0F, 0.25F, C_FAST, 0.0F, 1.0F).addSegment(0.25F, 1.0F, C_SMOOTH, 1.0F, 0.0F);
   public static GunAnimation pulseRifleRecoil = (new GunAnimation()).addTranslate(0.0F, 0.0F, 1.0F).addRotate(1.0F, 1.0F, 0.0F, 0.0F).addSegment(0.0F, 0.1F, C_FAST, 0.0F, 0.4F).addSegment(0.1F, 0.2F, C_SMOOTH, 0.4F, 0.3F).addSegment(0.2F, 0.4F, C_FAST, 0.3F, 1.0F).addSegment(0.4F, 1.0F, C_SMOOTH, 1.0F, 0.0F);
   private final List segments = new ArrayList();
   private final List transformations = new ArrayList();

   public GunAnimation addSegment(float start, float end, GunAnimation.ICurveType curve, float val1, float val2) {
      this.segments.add(new GunAnimation.AnimationSegment(start, end, curve, val1, val2));
      return this;
   }

   public GunAnimation addTranslate(float x, float y, float z) {
      this.transformations.add(new GunAnimation.Translate(x, y, z));
      return this;
   }

   public GunAnimation addRotate(float angle, float x, float y, float z) {
      this.transformations.add(new GunAnimation.Rotate(angle, x, y, z));
      return this;
   }

   public void play(float progress, boolean mirror, float... magnitudes) {
      float prev = 0.0F;
      float value = 0.0F;

      for(GunAnimation.AnimationSegment segment : this.segments) {
         if (progress > segment.start && progress <= segment.end) {
            float p = (progress - prev) / (segment.end - segment.start);
            value += segment.getValue(p);
         }

         prev = segment.end;
      }

      int i = 0;

      for(GunAnimation.Transformation trans : this.transformations) {
         float v = value;
         if (magnitudes.length > i) {
            v = value * magnitudes[i++];
         }

         trans.apply(v, mirror);
      }

   }
}
