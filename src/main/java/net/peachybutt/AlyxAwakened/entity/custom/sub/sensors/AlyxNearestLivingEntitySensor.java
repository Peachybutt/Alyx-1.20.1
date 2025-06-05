package net.peachybutt.AlyxAwakened.entity.custom.sub.sensors;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.monster.Creeper;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

public class AlyxNearestLivingEntitySensor extends NearestVisibleLivingEntitySensor {
    @Override
    protected boolean isMatchingEntity(LivingEntity self, LivingEntity other) {
        return other instanceof Creeper&& other.isAlive(); // or some custom logic
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.ATTACK_TARGET;
    }
}