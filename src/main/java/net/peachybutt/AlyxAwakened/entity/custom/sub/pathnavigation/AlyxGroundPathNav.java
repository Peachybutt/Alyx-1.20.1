package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import net.peachybutt.AlyxAwakened.entity.custom.ModPathTypes;

public class AlyxGroundPathNav extends GroundPathNavigation {
    private AlyxWalkNodeEval alyxNodeEvaluator;
    private AlyxPathLogic alyxPathLogic;

    public AlyxGroundPathNav(Mob mob, Level level) {
        super(mob, level);
        System.out.println("alyxGroundPathNav initialized!!!");
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
        this.alyxNodeEvaluator.setCanWalkOverFences(true);
        this.alyxNodeEvaluator.setCanFloat(true);

        this.nodeEvaluator = this.alyxNodeEvaluator;

        this.alyxPathLogic = new AlyxPathLogic();
        return new PathFinder(this.alyxNodeEvaluator, maxVisitedNodes);
    }


    private Direction getOpenFenceSide(BlockPos pos) {
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
    protected Vec3 getTempMobPos() {
        Node currentNode = path.getNextNode();


        BlockPos pos = new BlockPos(currentNode.x, currentNode.y, currentNode.z);
        BlockPathTypes type = this.nodeEvaluator.getBlockPathType(this.level, pos.getX(), pos.getY(), pos.getZ());

        Vec3 destination = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);

        if (type == ModPathTypes.PARTIAL_PASSABLE) {
            Direction openDir = getOpenFenceSide(pos);
            Vec3 offset = switch (openDir) {
                case NORTH -> new Vec3(0, 0, -1);
                case SOUTH -> new Vec3(0, 0, 0.425);
                case WEST -> new Vec3(-0.425, 0, 0);
                case EAST -> new Vec3(0.425, 0, 0);
                default -> Vec3.ZERO;
            };
            destination = destination.add(offset);
            System.out.println("BlockPathType at node: " + type + " for position: " + pos);
            System.out.println("Open side found: " + openDir);
            System.out.println("Final movement destination: " + destination);
        }

        return destination;
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        BlockPathTypes type = this.nodeEvaluator.getBlockPathType(this.level, pos.getX(), pos.getY(), pos.getZ());
        return type == ModPathTypes.PARTIAL_PASSABLE || super.isStableDestination(pos);
    }
}
