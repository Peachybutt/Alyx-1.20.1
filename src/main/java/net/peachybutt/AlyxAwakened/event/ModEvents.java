package net.peachybutt.AlyxAwakened.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.entity.ModEntities;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

@Mod.EventBusSubscriber(modid = AlyxAwakened.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ALYX.get(), AlyxEntity.createAttributes().build());

    }
}
