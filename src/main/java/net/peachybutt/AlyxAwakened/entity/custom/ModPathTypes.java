package net.peachybutt.AlyxAwakened.entity.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class ModPathTypes {
    public static final BlockPathTypes PARTIAL_PASSABLE = BlockPathTypes.create("PARTIAL_PASSABLE",1.0F);
}
