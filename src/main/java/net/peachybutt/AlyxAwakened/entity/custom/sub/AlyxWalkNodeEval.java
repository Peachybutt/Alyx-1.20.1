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
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos posBelow = pos.below();

        BlockState state = level.getBlockState(pos);
        BlockState stateBelow = level.getBlockState(posBelow);

        BlockPathTypes type = evaluateBlock(level, pos);
        BlockPathTypes typeBelow = evaluateBlock(level, posBelow);

        System.out.println("Current pos " + pos + " type: " + type
                + " | Below pos " + posBelow + " type: " + typeBelow);

        // Determine the final path type based on both evaluations
        BlockPathTypes finalType = (typeBelow != BlockPathTypes.WALKABLE) ? typeBelow : type;

        // Apply AlyxPathLogic override to the block below
        BlockPathTypes overridden = AlyxPathLogic.overridePathType(finalType, stateBelow);

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
        return AlyxPathLogic.overridePathType(original, state);
    }


    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        return AlyxPathLogic.overridePathType(original, state);
    }
}
