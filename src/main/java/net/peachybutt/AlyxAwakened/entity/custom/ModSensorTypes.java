package net.peachybutt.AlyxAwakened.entity.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.peachybutt.AlyxAwakened.entity.custom.sub.sensors.AlyxNearestLivingEntitySensor;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class ModSensorTypes {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES =
            DeferredRegister.create(Registries.SENSOR_TYPE, "alyx_awakened");

    //Here is our sensors, ngl i've got no clue where we actually use this lol
    public static final RegistryObject<SensorType<AlyxNearestLivingEntitySensor>> ALYX_NEAREST_LIVING_ENTITIES =
            SENSOR_TYPES.register("alyx_nearest_living_entities",
                    () -> new SensorType<>(AlyxNearestLivingEntitySensor::new)
            );

    public static void init(IEventBus eventBus) {
        SENSOR_TYPES.register(eventBus);
    }
}
