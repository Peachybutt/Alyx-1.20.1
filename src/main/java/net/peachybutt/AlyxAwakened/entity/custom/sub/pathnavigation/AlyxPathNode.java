package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class AlyxPathNode { //Custom node structure
    public final BlockPos pos;
    public final float cost;
    public final AlyxPathNode parent;

    public AlyxPathNode(BlockPos pos, float cost, AlyxPathNode parent) { //Constructor
        this.pos = pos;
        this.cost = cost;
        this.parent = parent;
    }

    public List<BlockPos> reconstructPath(AlyxPathNode endNode) {
        List<BlockPos> path = new ArrayList<>();
        AlyxPathNode current = endNode;

        while (current != null) {
            path.add(current.pos);
            current = current.parent;
        }

        Collections.reverse(path); // Start to goal
        return path;
    }


    public List<BlockPos> findCustomPath(BlockPos start, BlockPos goal, ServerLevel level) { //Custom pathfinder implementation
        Queue<AlyxPathNode> frontier = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        int maxNodes = 1000;
        int nodeCount = 0;
        System.out.println("findCustomPath called");
        frontier.add(new AlyxPathNode(start, 0, null));

        while (!frontier.isEmpty() && nodeCount < maxNodes) {
            AlyxPathNode current = frontier.poll();
            nodeCount++;

            if (current.pos.equals(goal)) {
                return reconstructPath(current);
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.pos.relative(dir);

                if (visited.contains(neighbor)) continue;

                BlockState state = level.getBlockState(neighbor);
                if (isWalkableOrPartialPassable(state, neighbor, level)) {
                    frontier.add(new AlyxPathNode(neighbor, current.cost + 1, current));
                    visited.add(neighbor);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    private boolean isWalkableOrPartialPassable(BlockState state, BlockPos pos, Level level) { //Partial passable identification (for fences, walls, etc)
        if (state.isAir()) return true;

        if (state.getBlock() instanceof FenceBlock fence) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighbor);

                boolean connects = fence.connectsTo(
                        neighborState,
                        neighborState.isFaceSturdy(level, neighbor, dir.getOpposite()),
                        dir
                );

                if (!connects) return true; // Open side exists
            }
        }

        return false;
    }

}
