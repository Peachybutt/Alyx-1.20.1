package net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

import java.util.function.Predicate;

import static net.minecraft.world.entity.ai.behavior.BehaviorUtils.canSee;

public class AlyxMeleeAttack extends Behavior<AlyxEntity> {
    private final int attackInterval;
    private long lastAttackTime;

    public AlyxMeleeAttack(int attackInterval) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.attackInterval = attackInterval;
        this.lastAttackTime = 0L;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, AlyxEntity alyx) {
        LivingEntity target = alyx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && alyx.getSensing().hasLineOfSight(target) && alyx.distanceToSqr(target) < 4.0D;
    }

    @Override
    protected void start(ServerLevel level, AlyxEntity alyx, long gameTime) {
        this.lastAttackTime = gameTime;
        System.out.println("MeleeAttack Called");
        alyx.getNavigation().stop();
        alyx.swing(InteractionHand.MAIN_HAND);
        alyx.doHurtTarget(alyx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null));
    }

    @Override
    protected boolean canStillUse(ServerLevel level, AlyxEntity alyx, long gameTime) {
        LivingEntity target = alyx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && alyx.distanceToSqr(target) < 4.0D;
    }

    @Override
    protected void tick(ServerLevel level, AlyxEntity alyx, long gameTime) {
        LivingEntity target = alyx.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null && gameTime - lastAttackTime >= attackInterval && alyx.getSensing().hasLineOfSight(target)) {
            alyx.swing(InteractionHand.MAIN_HAND);
            alyx.doHurtTarget(target);
            this.lastAttackTime = gameTime;
        }
    }

    @Override
    protected void stop(ServerLevel level, AlyxEntity alyx, long gameTime) {
        alyx.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}