package net.peachybutt.AlyxAwakened.entity.custom.sub.brain.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Creeper;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TargetEntity<E extends Mob, T extends LivingEntity> extends Behavior<E> {
    private final Class<T> targetClass;
    private final Predicate<T> filter;

    public TargetEntity(Class<T> targetClass, Predicate<T> filter) {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
        this.targetClass = targetClass;
        this.filter = filter;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        List<LivingEntity> visible = mob.getBrain()
                .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .map(list -> (List<LivingEntity>) list)
                .orElse(List.of());

        return visible.stream()
                .anyMatch(e -> targetClass.isInstance(e) && filter.test(targetClass.cast(e)));
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        List<LivingEntity> visible = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .map(list -> (List<LivingEntity>) list)
                .orElse(List.of());
        System.out.println("TargetEntity called");

        Optional<T> target = visible.stream()
                .filter(e -> targetClass.isInstance(e) && filter.test(targetClass.cast(e)))
                .map(e -> targetClass.cast(e))
                .findFirst();

        target.ifPresent(t -> {
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, t);
        });
    }
}