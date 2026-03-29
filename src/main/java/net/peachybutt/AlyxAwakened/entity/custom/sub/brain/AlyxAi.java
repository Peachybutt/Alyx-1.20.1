package net.peachybutt.AlyxAwakened.entity.custom.sub.brain;

import com.google.common.collect.ImmutableList;
import javafx.scene.shape.MoveTo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.RememberIfHoglinWasKilled;
import net.minecraft.world.entity.player.Player;
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

        //Debug
        System.out.println("Brain called");


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
                        new MoveToTargetSink(),
                        InteractWithDoor.create()
                ));
    }

    private static void initFightActivity(AlyxEntity alyx, Brain<AlyxEntity> pBrain) {
        pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create((target) -> !isNearestValidAttackTarget(alyx, target)),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                        MeleeAttack.create(20)), MemoryModuleType.ATTACK_TARGET);
    }

    private static void initIdleActivity(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.IDLE,
                ImmutableList.of());
    }

    // Bla bla bla

    public static void updateActivity(AlyxEntity pAlyx) {
        pAlyx.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    private static boolean isNearestValidAttackTarget(AlyxEntity alyx, LivingEntity pTarget) {
        return findNearestValidAttackTarget(alyx).filter((livingEntity) -> {
            return livingEntity == pTarget;
        }).isPresent();
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AlyxEntity alyx) {
        Brain<AlyxEntity> brain = alyx.getBrain();
        Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(alyx, MemoryModuleType.ANGRY_AT);
            if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(alyx, (LivingEntity)optional.get())) {
                return optional;
            } else {
                Optional optional3;
                if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                    optional3 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                    if (optional3.isPresent()) {
                        return optional3;
                    }
                }

                optional3 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
                if (optional3.isPresent()) {
                    return optional3;
                } else {
                    Optional<Player> optional2 = brain.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
                    return optional2.isPresent() && Sensor.isEntityAttackable(alyx, (LivingEntity)optional2.get()) ? optional2 : Optional.empty();
                }
            }
        }
    }
