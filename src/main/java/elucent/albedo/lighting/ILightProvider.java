package elucent.albedo.lighting;

import elucent.albedo.event.GatherLightsEvent;
import net.minecraft.entity.Entity;

/**
 * 编译期占位（stub）：albedo 是 techguns 的可选依赖，本模组不安装 albedo，
 * 但 javac 解析 techguns 的 RocketProjectile 时需加载其实现的接口类。
 * 运行时不会被使用（打包时已排除 elucent/**）。
 */
public interface ILightProvider {
    Light provideLight();

    default void gatherLights(GatherLightsEvent evt, Entity ent) {
    }
}
