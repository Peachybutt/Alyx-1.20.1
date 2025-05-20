package net.peachybutt.AlyxAwakened.entity.custom.sub;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
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
        BlockState stateBelow = level.getBlockState(pos.below());
        BlockState state = level.getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(level, x, y, z);

        BlockPathTypes overridden = AlyxPathLogic.overridePathType(original, stateBelow);

        return overridden;
    }


    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        return AlyxPathLogic.overridePathType(original, state);
    }
}
