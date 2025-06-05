package net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class WalkToTarget<E extends PathfinderMob> extends Behavior<E> {
    private final float speedModifier;
    private final int closeEnoughDist;

    public WalkToTarget() {
        this(1.0F, 2);
    }

    public WalkToTarget(float speedModifier, int closeEnoughDist) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
        this.speedModifier = speedModifier;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && entity.distanceToSqr(target) > closeEnoughDist * closeEnoughDist;
    }

    @Override
    protected void start(ServerLevel level, E entity, long gameTime) {
        LivingEntity target = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            System.out.println("WalkToTarget called");
            entity.getBrain().setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(target, true), speedModifier, closeEnoughDist)
            );
        }
    }
}