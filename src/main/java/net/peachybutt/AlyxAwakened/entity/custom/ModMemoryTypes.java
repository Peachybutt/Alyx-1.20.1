package net.peachybutt.AlyxAwakened.entity.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.UUID;

public class ModMemoryTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES =
            DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, "alyx_awakened");

    public static final RegistryObject<MemoryModuleType<BlockPos>> EXAMPLE_BLOCK_MEM_MOD =
            MEMORY_TYPES.register("example_block_memory", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    //No entity codec is available, UUID's should be used instead.
    public static final RegistryObject<MemoryModuleType<UUID>> EXAMPLE_UUID_MEM_MOD =
            MEMORY_TYPES.register("example_uuid_memory", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC)));

    //General empty codec, this is used in a lot of vanilla behavior so don't get scared to use it you P.A.B
    public static final RegistryObject<MemoryModuleType<Void>> IS_RETREATING =
            MEMORY_TYPES.register("is_retreating", () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> EXAMPLE_BOOLEAN_MEM_MOD =
            MEMORY_TYPES.register("example_boolean_memory", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final RegistryObject<MemoryModuleType<Integer>> EXAMPLE_INT_MEM_MOD =
            MEMORY_TYPES.register("example_int_memory", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final RegistryObject<MemoryModuleType<String>> EXAMPLE_STRING_MEM_MOD =
            MEMORY_TYPES.register("example_string_memory", () -> new MemoryModuleType<>(Optional.of(Codec.STRING)));

    //Necessary event bus for hooking back into AlyxAwakened class
    public static void register(IEventBus bus) {
        MEMORY_TYPES.register(bus);
    }
}


