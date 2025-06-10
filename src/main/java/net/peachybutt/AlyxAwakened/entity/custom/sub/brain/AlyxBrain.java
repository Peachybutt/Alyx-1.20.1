package net.peachybutt.AlyxAwakened.entity.custom.sub.brain;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.warden.Digging;
import net.minecraft.world.entity.ai.behavior.warden.Emerging;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.schedule.Activity;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;
import net.peachybutt.AlyxAwakened.entity.custom.ModMemoryTypes;
import net.peachybutt.AlyxAwakened.entity.custom.sub.brain.activities.AlyxStandardActivities;
import net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors.*;


public class AlyxBrain {
    public static Brain<?> makeBrain(AlyxEntity alyx, Brain<AlyxEntity> brain) {
        initCoreActivities(brain);
        initFightActivity(brain);
        System.out.println("Brain called");
        brain.setActiveActivityIfPossible(Activity.CORE);
        brain.setActiveActivityIfPossible(Activity.FIGHT);
        return brain;
    }

    private static void initCoreActivities(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.CORE, 0,
                ImmutableList.of(
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new TargetEntity<>(Creeper.class, creeper -> true), //set visible creepers as target
                        //new WalkToTarget<>(),
                        new AlyxMeleeAttack(20),
                        new MoveToTargetSink()
                ));
    }

    public static void initStandardActivities(Brain<AlyxEntity> brain) {
        brain.addActivity(AlyxStandardActivities.PATROL, 1,
                ImmutableList.of(
                        new WalkToMemoryPosition<>(ModMemoryTypes.EXAMPLE_BLOCK_MEM_MOD.get(), 1.0F)
                ));
    }

    private static void initFightActivity(Brain<AlyxEntity> brain) {
        brain.addActivity(Activity.FIGHT, 10,
                ImmutableList.of(
                        new TargetEntity<>(Creeper.class, creeper -> true),
                        new MoveToTargetSink(),
                        new AlyxWalkToTargetIfOutOfReach(20),
                        new AlyxMeleeAttack(20)
                ));
    }
}