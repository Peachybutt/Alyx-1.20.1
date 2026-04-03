package net.peachybutt.AlyxAwakened.entity.custom.sub.brain;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.schedule.Activity;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;
import net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors.*;

import java.util.Optional;


public class AlyxAi {
    public static final int EXAMPLE_VARIABLE = 8;


    public static Brain<?> makeBrain(AlyxEntity alyx, Brain<AlyxEntity> brain) {
        //Initialize activity sets
        initCoreActivities(brain);
        initIdleActivity(brain);
        initFightActivity(alyx, brain);


        //Set activity sets
        brain.setActiveActivityIfPossible(Activity.CORE);
        brain.setActiveActivityIfPossible(Activity.IDLE);

        //Excess
        brain.useDefaultActivity();
        return brain;
    }

    //Activity sets

    private static void initCoreActivities(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.CORE, 0,
                ImmutableList.of(
                        new LookAtTargetSink(45, 90),
                        new AlyxMoveToTarget(),
                        InteractWithDoor.create()
                ));
    }

    private static void initFightActivity(AlyxEntity alyx, Brain<AlyxEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create((
                                target) -> !isNearestValidAttackTarget(alyx, target)),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                        //MeleeAttack.create(20)
                        new AlyxMeleeAttack(20)
                ), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initIdleActivity(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.IDLE,
                ImmutableList.of());
    }

    // Bla bla bla

    public static void updateActivity(AlyxEntity alyx) {
        Brain<AlyxEntity> brain = alyx.getBrain();
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT,
                Activity.IDLE
        ));
    }

    private static boolean isNearestValidAttackTarget(AlyxEntity alyx, LivingEntity pTarget) {
        return findNearestValidAttackTarget(alyx).filter((livingEntity) -> {
            return livingEntity == pTarget;
        }).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AlyxEntity alyx) {
            Brain<AlyxEntity> brain = alyx.getBrain();

            return brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    .flatMap(entities -> entities.findClosest(
                            e -> isValidTarget(alyx, e)
                    ));
        }

        private static boolean isValidTarget(AlyxEntity alyx, LivingEntity target) {
            if (!Sensor.isEntityAttackable(alyx, target)) return false;

            if (target instanceof Creeper) return true;

            // can get nerdy and write more targets/conditions here

            return false;
        }
    }
