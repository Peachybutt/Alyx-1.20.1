package net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public class AlyxMoveToTarget extends Behavior<Mob> {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;

    public AlyxMoveToTarget() {
        this(150, 250);
    }

    public AlyxMoveToTarget(int pMinDuration, int pMaxDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT
        ), pMinDuration, pMaxDuration);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        if (!(mob instanceof AlyxEntity alyx)) return false;

        if (this.remainingCooldown > 0) {
            --this.remainingCooldown;
            return false;
        }

        Brain<?> brain = alyx.getBrain();
        WalkTarget target = brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        if (target == null) return false;

        if (!this.reachedTarget(alyx, target) && tryComputePath(alyx, target, level.getGameTime())) {
            lastTargetPos = target.getTarget().currentBlockPosition();
            return true;
        } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            return false;
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        if (!(mob instanceof AlyxEntity alyx)) return false;

        if (this.path != null && this.lastTargetPos != null) {
            Optional<WalkTarget> targetOpt = alyx.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
            boolean isSpectator = targetOpt.map(AlyxMoveToTarget::alyxIsWalkTargetSpectator).orElse(false);
            return !alyx.getNavigation().isDone() && targetOpt.isPresent()
                    && !this.reachedTarget(alyx, targetOpt.get()) && !isSpectator;
        }

        return false;
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        if (!(mob instanceof AlyxEntity alyx)) return;

        alyx.getBrain().setMemory(MemoryModuleType.PATH, this.path);
        alyx.getNavigation().moveTo(this.path, this.speedModifier);
    }

    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        if (!(mob instanceof AlyxEntity alyx)) return;

        Path currentPath = alyx.getNavigation().getPath();
        if (this.path != currentPath) {
            this.path = currentPath;
            alyx.getBrain().setMemory(MemoryModuleType.PATH, currentPath);
        }

        if (currentPath != null && this.lastTargetPos != null) {
            WalkTarget target = alyx.getBrain().getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
            if (target != null && target.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0) {
                if (tryComputePath(alyx, target, level.getGameTime())) {
                    this.lastTargetPos = target.getTarget().currentBlockPosition();
                    this.start(level, alyx, gameTime);
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, Mob mob, long gameTime) {
        if (!(mob instanceof AlyxEntity alyx)) return;

        if (alyx.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
                && !this.reachedTarget(alyx, alyx.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get())
                && alyx.getNavigation().isStuck()) {
            this.remainingCooldown = level.getRandom().nextInt(MAX_COOLDOWN_BEFORE_RETRYING);
        }

        alyx.getNavigation().stop();
        alyx.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        alyx.getBrain().eraseMemory(MemoryModuleType.PATH);
        this.path = null;
    }

    private boolean tryComputePath(AlyxEntity alyx, WalkTarget target, long time) {
        BlockPos targetPos = target.getTarget().currentBlockPosition();
        this.path = alyx.getNavigation().createPath(targetPos, 0);
        this.speedModifier = target.getSpeedModifier();

        if (this.reachedTarget(alyx, target)) {
            alyx.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean reachable = this.path != null && this.path.canReach();
            if (reachable) {
                alyx.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!alyx.getBrain().hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                alyx.getBrain().setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time);
            }

            if (this.path != null) return true;

            Vec3 fallback = DefaultRandomPos.getPosTowards(alyx, 10, 7, Vec3.atBottomCenterOf(targetPos), Math.PI / 2);
            if (fallback != null) {
                this.path = alyx.getNavigation().createPath(fallback.x, fallback.y, fallback.z, 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(AlyxEntity alyx, WalkTarget target) {
        return target.getTarget().currentBlockPosition().distManhattan(alyx.blockPosition()) <= target.getCloseEnoughDist();
    }

    private static boolean alyxIsWalkTargetSpectator(WalkTarget target) {
        PositionTracker tracker = target.getTarget();
        return tracker instanceof EntityTracker entityTracker && entityTracker.getEntity().isSpectator();
    }
}