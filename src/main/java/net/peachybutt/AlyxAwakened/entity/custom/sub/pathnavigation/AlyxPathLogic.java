package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.EnumMap;
import java.util.Map;

public class AlyxPathLogic { //This class is to introduce additional blocks as friendly and not so friendly.
    public static final BlockPathTypes ALYX_HAPPY = BlockPathTypes.COCOA;
    public static final BlockPathTypes ALYX_MAD = BlockPathTypes.DANGER_FIRE;

    public static BlockPathTypes overridePathType(BlockPathTypes original, BlockState blockState) {

        if (blockState.is(Blocks.DEEPSLATE)) { // Checks for particular block type
            return ALYX_MAD; // Or any other similar danger listed within the Malus
        }
        if (blockState.is(Blocks.MOSS_BLOCK)) { // Likes to walk on moss blocks
            return ALYX_HAPPY;
        }

        return original;
    }

}