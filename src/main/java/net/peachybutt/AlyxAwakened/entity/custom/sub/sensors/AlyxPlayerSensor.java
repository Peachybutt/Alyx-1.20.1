package net.peachybutt.AlyxAwakened.entity.custom.sub.sensors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AlyxPlayerSensor extends Sensor<Mob> {
    public static final MemoryModuleType<BlockPos> TARGET_POS = new MemoryModuleType<>(Optional.of(BlockPos.CODEC));
    public static final SensorType<AlyxPlayerSensor> NEARBY_PLAYERS = new SensorType<>(() -> new AlyxPlayerSensor());
    public static final MemoryModuleType<Player> NEAREST_VISIBLE_PLAYER = MemoryModuleType.NEAREST_VISIBLE_PLAYER;
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = MemoryModuleType.WALK_TARGET;

    @Override
    protected void doTick(ServerLevel level, Mob mob) {
        List<ServerPlayer> nearbyPlayers = level.players().stream()
                .filter(p -> p.distanceTo(mob) < 16)
                .toList();
        if (!nearbyPlayers.isEmpty()) {
            mob.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, nearbyPlayers.get(0));
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
    }
}