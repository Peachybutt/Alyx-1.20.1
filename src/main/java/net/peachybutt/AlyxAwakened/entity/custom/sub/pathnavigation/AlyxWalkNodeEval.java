package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.peachybutt.AlyxAwakened.entity.custom.ModPathTypes;

public class AlyxWalkNodeEval extends WalkNodeEvaluator {
    public AlyxWalkNodeEval() {
        super();
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState stateBelow = level.getBlockState(pos.below());
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        BlockPathTypes original = super.getBlockPathType(level, x, y, z);


        if (block instanceof FenceBlock) {

            boolean northConnected = state.getValue(FenceBlock.NORTH);
            boolean southConnected = state.getValue(FenceBlock.SOUTH);
            boolean eastConnected = state.getValue(FenceBlock.EAST);
            boolean westConnected = state.getValue(FenceBlock.WEST);


            if (northConnected || southConnected || eastConnected || westConnected) return ModPathTypes.PARTIAL_PASSABLE;
        }


        // This section basically introduces AlyxPathLogic blocks telling Alyx where to walk and prefer to walk.
        return AlyxPathLogic.overridePathType(original, stateBelow);
    }

    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        Block block = state.getBlock();
        return AlyxPathLogic.overridePathType(original, state);
    }

}
