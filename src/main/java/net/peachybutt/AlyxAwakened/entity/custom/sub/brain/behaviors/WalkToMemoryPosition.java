package net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

public class WalkToMemoryPosition<E extends PathfinderMob> extends Behavior<E> {
    private final MemoryModuleType<BlockPos> memory;
    private final float speed;

    public WalkToMemoryPosition(MemoryModuleType<BlockPos> memory, float speed) {
        super(ImmutableMap.of(memory, MemoryStatus.VALUE_PRESENT));
        this.memory = memory;
        this.speed = speed;
    }

    @Override
    protected void start(ServerLevel level, E entity, long gameTime) {
        BlockPos pos = entity.getBrain().getMemory(memory).get();
        entity.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), speed);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        return !entity.getNavigation().isDone();
    }

    @Override
    protected void stop(ServerLevel level, E entity, long gameTime) {
        entity.getNavigation().stop();
    }
}