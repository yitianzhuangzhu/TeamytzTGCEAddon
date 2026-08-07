package com.teamytz.tgceaddon.client.render.item;

import com.teamytz.tgceaddon.client.models.guns.ModelPowerSword;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import techguns.client.render.item.RenderGunBase90;

/**
 * 动力剑渲染器
 * 仿照雷霆锤的实现
 */
public class RenderPowerSword extends RenderGunBase90 {

    public RenderPowerSword() {
        super(new ModelPowerSword(), 2);
        // 禁用后坐力动画
        this.recoilParams = new float[] {0.0f, 0.0f};
    }
}
