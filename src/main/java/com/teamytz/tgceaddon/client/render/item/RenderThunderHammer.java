package com.teamytz.tgceaddon.client.render.item;

import com.teamytz.tgceaddon.client.models.guns.ModelThunderHammer;
import techguns.client.render.item.RenderGunBase90;

public class RenderThunderHammer extends RenderGunBase90 {

    public RenderThunderHammer() {
        super(new ModelThunderHammer(), 2);
        // 完全禁用后坐力动画
        this.recoilParams = new float[] {0.0f, 0.0f};
    }
}