package com.teamytz.tgceaddon.init;

import com.teamytz.tgceaddon.TGCEAddon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 本模组音效注册
 * 声音文件位于 assets/tgceaddon/sounds/（Ogg Vorbis .ogg 格式）
 */
@Mod.EventBusSubscriber(modid = TGCEAddon.MODID)
public class ModSounds {

    /** 待注册音效列表（必须在 createSoundEvent 调用之前初始化，否则静态初始化 NPE） */
    private static final List<SoundEvent> EVENTS = new ArrayList<>();

    /** 机炮开火音（连射期间每发播放） */
    public static final SoundEvent CANNON_FIRE = createSoundEvent("cannon_fire");
    /** 机炮停止开火结尾音（连射停止时播放一次） */
    public static final SoundEvent CANNON_FIRE_END = createSoundEvent("cannon_fire_end");

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation loc = new ResourceLocation(TGCEAddon.MODID, name);
        SoundEvent event = new SoundEvent(loc);
        event.setRegistryName(loc);
        EVENTS.add(event);
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        EVENTS.forEach(e -> event.getRegistry().register(e));
    }
}
