package net.peachybutt.AlyxAwakened.entity.custom.sub.brain;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.Optional;


public class AlyxAi {
    private static final Logger LOGGER = LogUtils.getLogger(); // Debugging
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
        //brain.useDefaultActivity();
        return brain;
    }

    //Activity sets

    private static void initCoreActivities(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.CORE, 0,
                ImmutableList.of(
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        //new AlyxMoveToTarget(),  We will probably want to reimplement this later, movetotarget is already accomplishing what it wants to do tho
                        InteractWithDoor.create()
                ));
    }

    private static void initIdleActivity(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.IDLE, 10,
                ImmutableList.of(
                        StartAttacking.create(e -> true, AlyxAi::findNearestValidAttackTarget)
                ));
    }

    private static void initFightActivity(AlyxEntity alyx, Brain<AlyxEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10,
                ImmutableList.of(
                        StartAttacking.create(e -> true, AlyxAi::findNearestValidAttackTarget),
                        StopAttackingIfTargetInvalid.create((
                                target) -> !isNearestValidAttackTarget(alyx, target)),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                        new AlyxMeleeAttack(20)
                ), MemoryModuleType.ATTACK_TARGET);
    }

    // This sets the baseline activities, I think?

    public static void updateActivity(AlyxEntity alyx) {
        Brain<AlyxEntity> brain = alyx.getBrain();

        // 1. Is the sensor populating the memory?
        brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresentOrElse(
                entities -> LOGGER.info("[ALYX] Visible entities: {}", entities.findClosest(e -> true).map(e -> e.getClass().getSimpleName()).orElse("none")),
                () -> LOGGER.info("[ALYX] NEAREST_VISIBLE_LIVING_ENTITIES is empty")
        );

        // 2. Is findNearestValidAttackTarget finding a creeper?
        Optional<? extends LivingEntity> target = findNearestValidAttackTarget(alyx);
        LOGGER.info("[ALYX] findNearestValidAttackTarget result: {}",
                target.map(e -> e.getClass().getSimpleName()).orElse("none"));

        // 3. Is ATTACK_TARGET being set?
        brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresentOrElse(
                t -> LOGGER.info("[ALYX] ATTACK_TARGET present: {}", t.getClass().getSimpleName()),
                () -> LOGGER.info("[ALYX] ATTACK_TARGET is empty")
        );

        // 4. What activity is currently active?
        LOGGER.info("[ALYX] Active activities before update: {}", brain.getActiveActivities());

        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT,
                Activity.IDLE
        ));

        // 5. Did the activity switch?
        LOGGER.info("[ALYX] Active activities after update: {}", brain.getActiveActivities());
    }

    private static boolean isNearestValidAttackTarget(AlyxEntity alyx, LivingEntity pTarget) {
        return findNearestValidAttackTarget(alyx).filter((livingEntity) -> {
            return livingEntity == pTarget;
        }).isPresent();

    }

    //This is filling the ATTACK_TARGET variable

    public static Optional<? extends LivingEntity> findNearestValidAttackTarget(AlyxEntity alyx) {
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
