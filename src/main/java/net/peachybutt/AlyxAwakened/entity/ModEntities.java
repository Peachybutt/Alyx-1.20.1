package net.peachybutt.AlyxAwakened.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

public class ModEntities {
        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
                DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AlyxAwakened.MOD_ID);

        public static final RegistryObject<EntityType<AlyxEntity>> ALYX =
                ENTITY_TYPES.register("alyx",
                        () -> EntityType.Builder.of(AlyxEntity::new, MobCategory.MISC)
                                .sized(0.4f, 1.0f)
                                .build(new ResourceLocation(AlyxAwakened.MOD_ID, "alyx").toString()));

        public static void register(IEventBus eventBus) {
            ENTITY_TYPES.register(eventBus);
        }
}
