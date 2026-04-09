package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.peachybutt.AlyxAwakened.entity.custom.ModPathTypes;
import org.jetbrains.annotations.NotNull;

public class AlyxWalkNodeEval extends WalkNodeEvaluator {
    //The purpose of this class is to classify blocks and shi, not make pathfinding decisions
    public AlyxWalkNodeEval() {
        super();
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        BlockPathTypes original = super.getBlockPathType(level, x, y, z);

        if (block instanceof FenceGateBlock) {
            // Open fence gates are walkable, closed ones are blocked
            return state.getValue(FenceGateBlock.OPEN)
                    ? BlockPathTypes.WALKABLE
                    : BlockPathTypes.BLOCKED;
        }

        //Pass around fences
        if (block instanceof FenceBlock) {
            boolean north = state.getValue(FenceBlock.NORTH);
            boolean south = state.getValue(FenceBlock.SOUTH);
            boolean east  = state.getValue(FenceBlock.EAST);
            boolean west  = state.getValue(FenceBlock.WEST);

            if (north || south || east || west) {
                boolean hasOpenSide = !north || !south || !east || !west;
                if (hasOpenSide) return ModPathTypes.PARTIAL_PASSABLE;
            }
        }

        BlockState stateBelow = level.getBlockState(pos.below());
        return AlyxPathLogic.overridePathType(original, stateBelow);
    }

    public Direction getOpenFenceSide(BlockPos pos) {
        if (this.level == null) return null; //Crash protection

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
        int count = super.getNeighbors(neighbors, currentNode);

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int nx = currentNode.x + dir.getStepX();
            int ny = currentNode.y;
            int nz = currentNode.z + dir.getStepZ();

            BlockPathTypes pathType = this.getBlockPathType(this.level, nx, ny, nz);

            if (pathType != ModPathTypes.PARTIAL_PASSABLE) continue;

            // Found a partial passable fence — find its open side
            Direction openDir = getOpenFenceSide(new BlockPos(nx, ny, nz));
            if (openDir == null) continue;

            // The gap node is one block past the fence on its open side
            int gx = nx + openDir.getStepX();
            int gz = nz + openDir.getStepZ();

            BlockPathTypes gapType = this.getBlockPathType(this.level, gx, ny, gz);
            if (gapType == BlockPathTypes.BLOCKED) continue;

            Node gapNode = this.getNode(gx, ny, gz);
            if (gapNode == null || gapNode.closed) continue;

            gapNode.type = gapType;

            // Only add if not already in the neighbor list
            boolean alreadyAdded = false;
            for (int i = 0; i < count; i++) {
                if (neighbors[i] != null && neighbors[i].x == gx
                        && neighbors[i].y == ny && neighbors[i].z == gz) {
                    alreadyAdded = true;
                    break;
                }
            }

            if (!alreadyAdded) {
                neighbors[count++] = gapNode;
            }
        }
        // Add vertical movement if needed (stairs, slabs, etc)
        // Possibly add diagonal or jump-over logic if you're billy (billy badass that is)
        return count;
    }


    @Override
    public BlockPathTypes getBlockPathType(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        BlockPathTypes original = super.getBlockPathType(mob, pos);
        Block block = state.getBlock();
        return AlyxPathLogic.overridePathType(original, state);
    }

}
