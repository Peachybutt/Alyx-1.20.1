package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class AlyxPathLogic { //This class is to introduce additional blocks as friendly and not so friendly.
    public static final BlockPathTypes ALYX_HAPPY = BlockPathTypes.COCOA;
    public static final BlockPathTypes ALYX_MAD = BlockPathTypes.DANGER_FIRE;

    public static final BlockPathTypes PARTIAL_OPEN_NORTH = BlockPathTypes.create("PARTIAL_OPEN_NORTH", 1.0F);
    public static final BlockPathTypes PARTIAL_OPEN_SOUTH = BlockPathTypes.create("PARTIAL_OPEN_SOUTH", 1.0F);
    public static final BlockPathTypes PARTIAL_OPEN_EAST = BlockPathTypes.create("PARTIAL_OPEN_EAST", 1.0F);
    public static final BlockPathTypes PARTIAL_OPEN_WEST = BlockPathTypes.create("PARTIAL_OPEN_WEST",1.0F);

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