package com.teamytz.tgceaddon.client.render.item;

import com.teamytz.tgceaddon.client.models.guns.ModelChainsword;
import techguns.client.render.item.RenderGunBase90;
import net.minecraft.client.renderer.GlStateManager;

public class RenderChainsword extends RenderGunBase90 {

    public RenderChainsword() {
        super(new ModelChainsword(), 2);
        // 完全禁用父类的后坐力动画
        this.recoilParams = new float[] {0.0f, 0.0f};
    }

    @Override
    protected void transformFirstPerson(float fireProgress, float reloadProgress, float chargeProgress, boolean left, boolean shouldLowerWeapon) {
        if (fireProgress > 0) {
            // 振动效果 - 轻微抖动
            // 使用正弦函数创建振动效果
            float vibrationSpeed = 12.0f; // 振动频率
            float vibrationAmplitude = 0.02f; // 振动幅度
            
            // 水平方向振动
            float horizontalVibration = (float) Math.sin(fireProgress * Math.PI * vibrationSpeed) * vibrationAmplitude;
            // 垂直方向振动
            float verticalVibration = (float) Math.cos(fireProgress * Math.PI * vibrationSpeed * 1.2f) * vibrationAmplitude * 0.5f;
            
            // 应用振动变换
            GlStateManager.translate(horizontalVibration, verticalVibration, 0.0f);
            
            // 轻微的向前伸出
            float forwardExtend = fireProgress < 0.3f ? 
                    fireProgress * 0.2f :  // 前半段：逐渐向前伸
                    (1.0f - fireProgress) * 0.1f;  // 后半段：快速收回
            
            GlStateManager.translate(0.0f, 0.0f, forwardExtend);
        }
        
        // 调用父类方法（不会有后坐力因为参数已设为0）
        super.transformFirstPerson(fireProgress, reloadProgress, chargeProgress, left, shouldLowerWeapon);
    }
}
