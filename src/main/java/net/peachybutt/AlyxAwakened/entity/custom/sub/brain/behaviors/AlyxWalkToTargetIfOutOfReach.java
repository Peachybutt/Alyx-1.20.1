package net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

import java.util.Map;
import java.util.Optional;


public class AlyxWalkToTargetIfOutOfReach extends Behavior<AlyxEntity> {
    private final float speed;

    public AlyxWalkToTargetIfOutOfReach(float speed) {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.speed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, AlyxEntity alyx) {
        Optional<LivingEntity> targetOpt = alyx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (targetOpt.isEmpty()) return false;

        LivingEntity target = targetOpt.get();
        if (!target.isAlive() || !alyx.hasLineOfSight(target)) return false;

        double distSqr = alyx.distanceToSqr(target);
        double attackRange = alyx.getBbWidth() * alyx.getBbWidth() + 4.0;

        return distSqr > attackRange;
    }

    @Override
    protected void start(ServerLevel level, AlyxEntity alyx, long gameTime) {
        alyx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
            alyx.getBrain().setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(target, true), speed, 2)
            );

            // ⬇️ Debugging
            System.out.println("[DEBUG] Attempting to set WALK_TARGET to: " + target.getName().getString());
            alyx.getBrain().getMemory(MemoryModuleType.WALK_TARGET)
                    .ifPresent(walkTarget -> System.out.println("[DEBUG] WALK_TARGET = " + walkTarget));

            if (!alyx.getNavigation().isInProgress()) {
                System.out.println("[DEBUG] Alyx navigation is NOT in progress after setting WALK_TARGET!");
            } else {
                System.out.println("[DEBUG] Alyx IS navigating to WALK_TARGET.");
            }

        });
    }
}



