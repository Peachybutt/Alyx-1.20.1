package net.peachybutt.AlyxAwakened.entity.custom.sub.sensors;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Creeper;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AlyxNearestLivingEntitySensor extends Sensor<AlyxEntity> {
    public AlyxNearestLivingEntitySensor() {

        super(20); // Tick interval
    }

    @Override
    protected void doTick(ServerLevel level, AlyxEntity alyx) {
        System.out.println("[SENSOR] doTick firing");
        List<LivingEntity> visibleEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                alyx.getBoundingBox().inflate(16),
                e -> e != alyx && alyx.getSensing().hasLineOfSight(e)
        );

        alyx.getBrain().setMemory(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                new NearestVisibleLivingEntities(alyx, visibleEntities)
        );
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES
        );
    }
}