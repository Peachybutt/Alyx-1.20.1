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
        BlockPos pos = new BlockPos(x, y - 1, z);
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof FenceBlock) {
            boolean north = state.getValue(FenceBlock.NORTH);
            boolean south = state.getValue(FenceBlock.SOUTH);
            boolean west = state.getValue(FenceBlock.WEST);
            boolean east = state.getValue(FenceBlock.EAST);

            // If any side is open, consider it walkable
            if (!north || !south || !west || !east) {
                return BlockPathTypes.WALKABLE;
            } else {
                return BlockPathTypes.FENCE;
            }
        }

        BlockPathTypes original = super.getBlockPathType(level, x, y, z);

        BlockPathTypes overridden = AlyxPathLogic.overridePathType(original, state);
        return overridden;
    }


    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        return AlyxPathLogic.overridePathType(original, state);
    }
}
