package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.peachybutt.AlyxAwakened.entity.custom.ModPathTypes;
import org.jetbrains.annotations.NotNull;

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

    public Direction getOpenFenceSide(BlockPos pos) { //Partial passable general code, remove?
        BlockState state = this.level.getBlockState(pos);

        if (!(state.getBlock() instanceof FenceBlock fenceBlock)) {
            return null;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = this.level.getBlockState(neighborPos);

            boolean connects = fenceBlock.connectsTo(
                    neighborState,
                    neighborState.isFaceSturdy(this.level, neighborPos, direction.getOpposite()),
                    direction
            );

            if (!connects) {
                return direction; //Returns open side
            }
        }

        return null; //All sides are connected so no dice
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node currentNode) {
        int count = 0;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int dx = currentNode.x + dir.getStepX();
            int dy = currentNode.y;
            int dz = currentNode.z + dir.getStepZ();

            BlockPathTypes pathType = this.getBlockPathType(this.level, dx, dy, dz);

            if (pathType == ModPathTypes.PARTIAL_PASSABLE && currentNode.g < 5) {
                Direction openDir = getOpenFenceSide(new BlockPos(dx, dy, dz));
                if (openDir != null) {
                    int sideX = dx + openDir.getStepX();
                    int sideZ = dz + openDir.getStepZ();

                    Node entryNode = this.getNode(sideX, dy, sideZ);
                    entryNode.type = this.getBlockPathType(this.level, sideX, dy, sideZ);

                    if (!entryNode.closed && entryNode.type != BlockPathTypes.BLOCKED)
                        neighbors[count++] = entryNode;
                }
            }
        }
        return count;
        // Add vertical movement if needed (stairs, slabs, etc)
        // Possibly add diagonal or jump-over logic if you're extending support
    }

    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        Block block = state.getBlock();
        return AlyxPathLogic.overridePathType(original, state);
    }

}
