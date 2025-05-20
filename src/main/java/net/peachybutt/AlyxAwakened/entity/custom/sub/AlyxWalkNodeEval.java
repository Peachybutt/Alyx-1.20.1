package net.peachybutt.AlyxAwakened.entity.custom.sub;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class AlyxWalkNodeEval extends WalkNodeEvaluator {
    public AlyxWalkNodeEval() {
        super();
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        //Evaluate current block pos
        BlockPos pos = new BlockPos(x, y, z);
        BlockPathTypes currentType = evaluateBlock(level, pos);

        //Evaluate block below (ordinarily for AlyxPathLogic)
        BlockPos posBelow = pos.below();
        BlockPathTypes belowType = evaluateBlock(level, posBelow);

        // For debugging: print out the types for both levels.
        System.out.println("Current pos " + pos + " type: " + currentType + " | Below pos " + posBelow + " type: " + belowType);

        // Combine the evaluations:
        // Here, we assume that if the block below isn’t walkable, it’s a show-stopper.
        BlockPathTypes finalType = belowType != BlockPathTypes.WALKABLE ? belowType : currentType;

        // Finally, apply the AlyxPathLogic overrides to the chosen block state.
        // Note: You may adjust this order depending on whether the override should consider both levels.
        BlockState currentState = level.getBlockState(pos);
        BlockPathTypes overridden = AlyxPathLogic.overridePathType(finalType, currentState);

        System.out.println("Final override for pos " + pos + ": " + overridden);
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

            BlockState above = level.getBlockState(pos.above());
            if (!north || !south || !west || !east){
                return BlockPathTypes.WALKABLE;
            }
            return BlockPathTypes.FENCE;
        }

        BlockPathTypes original = super.getBlockPathType(level, pos.getX(), pos.getY(), pos.getZ());
        return original;
    }


    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        return AlyxPathLogic.overridePathType(original, state);
    }
}
