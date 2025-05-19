package net.peachybutt.AlyxAwakened.entity.custom.sub;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class AlyxWalkNodeEval extends WalkNodeEvaluator {
    public AlyxWalkNodeEval() {
        super();
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        BlockPathTypes currentPathType = evaluateBlock(level, pos);

        // If current position is walkable or otherwise acceptable, return it.
        if (currentPathType == BlockPathTypes.WALKABLE ||
                currentPathType == BlockPathTypes.OPEN ||
                currentPathType == BlockPathTypes.DOOR_OPEN ||
                currentPathType == BlockPathTypes.FENCE) {
            return currentPathType;
        }

        BlockPathTypes original = super.getBlockPathType(level, x, y, z);

        BlockPathTypes overridden = AlyxPathLogic.overridePathType(original, state);
        return overridden;
    }

    private BlockPathTypes evaluateBlock(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof FenceBlock) {
            boolean north = state.getValue(FenceBlock.NORTH);
            boolean south = state.getValue(FenceBlock.SOUTH);
            boolean west = state.getValue(FenceBlock.WEST);
            boolean east = state.getValue(FenceBlock.EAST);

            // Consider walkable if there's an open side and space above
            BlockState above = level.getBlockState(pos.above());
            if ((!north || !south || !west || !east) && above.isAir()) {
                return BlockPathTypes.WALKABLE;
            }
            return BlockPathTypes.FENCE;
        }

        // Let AlyxPathLogic modify other block types
        BlockPathTypes original = super.getBlockPathType(level, pos.getX(), pos.getY(), pos.getZ());
        return AlyxPathLogic.overridePathType(original, state);
    }


    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        return AlyxPathLogic.overridePathType(original, state);
    }
}
