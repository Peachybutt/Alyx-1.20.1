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
            if (!alyx.getNavigation().isInProgress()) {
                System.out.println("[DEBUG] Alyx navigation is not in progress!");
            }
            System.out.println("[DEBUG] Current active activities: " + alyx.getBrain().getActiveActivities());


            BlockPos targetPos = target.blockPosition(); //Debugging
            BlockPathTypes type = alyx.getNavigation().getNodeEvaluator().getBlockPathType( //Debugging
                    alyx.level(), targetPos.getX(), targetPos.getY(), targetPos.getZ()); //Debugging
                                                                                        //Debugging
            System.out.println("[DEBUG] BlockPathType at creeper position: " + type); //Debugging

        });
    }
}



