package net.peachybutt.AlyxAwakened.entity.custom.sub;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class AlyxGroundPathNav extends GroundPathNavigation {
    private AlyxWalkNodeEval alyxNodeEvaluator;
    private AlyxPathLogic alyxPathLogic;

    public AlyxGroundPathNav(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public void setCanFloat(boolean canFloat) {
        if (this.alyxNodeEvaluator != null) {
            this.alyxNodeEvaluator.setCanFloat(canFloat);
        }
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.alyxNodeEvaluator = new AlyxWalkNodeEval();
        this.alyxNodeEvaluator.setCanPassDoors(true); //IDK config lol
        this.alyxNodeEvaluator.setCanOpenDoors(true);
        this.alyxNodeEvaluator.setCanFloat(true);

        this.nodeEvaluator = this.alyxNodeEvaluator;

        this.alyxPathLogic = new AlyxPathLogic();
        return new PathFinder(this.alyxNodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected Vec3 getTempMobPos() {
        Vec3 basePos = super.getTempMobPos();
        BlockPos blockPos = BlockPos.containing(basePos);

        if (this.level.getBlockState(blockPos).getBlock() instanceof FenceBlock fence) {
            BlockState state = this.level.getBlockState(blockPos);

            double dx = 0.5, dz = 0.5;

            if (!state.getValue(FenceBlock.NORTH)) dz = 0.25;
            else if (!state.getValue(FenceBlock.SOUTH)) dz = 0.75;
            else if (!state.getValue(FenceBlock.WEST)) dx = 0.25;
            else if (!state.getValue(FenceBlock.EAST)) dx = 0.75;

            return new Vec3(blockPos.getX() + dx, blockPos.getY(), blockPos.getZ() + dz);
        }

        return basePos;
    }
}
