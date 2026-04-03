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

import java.util.Collections;
import java.util.List;

public class AlyxGroundPathNav extends GroundPathNavigation {
    private AlyxWalkNodeEval alyxNodeEvaluator;
    private AlyxPathLogic alyxPathLogic;
    private List<BlockPos> customPath = Collections.emptyList();

    public AlyxGroundPathNav(Mob mob, Level level) { //Constructor
        super(mob, level);
    }

    @Override
    public void setCanFloat(boolean canFloat) { //Can float goal, necessary
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

        //this.alyxPathLogic = new AlyxPathLogic(); - Useless
        return new PathFinder(this.alyxNodeEvaluator, 500);


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
    protected Vec3 getTempMobPos() { //Temp displacement for partial passable movement, remove

        if (this.path == null || this.path.isDone()) return super.getTempMobPos();

        Node currentNode = path.getNextNode();

        BlockPos pos = new BlockPos(currentNode.x, currentNode.y, currentNode.z);
        BlockPathTypes type = this.nodeEvaluator.getBlockPathType(this.level, pos.getX(), pos.getY(), pos.getZ());

        Vec3 destination = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);

        if (type == ModPathTypes.PARTIAL_PASSABLE) {
                Direction openDir = getOpenFenceSide(pos);
                Vec3 offset = switch (openDir) {
                    case NORTH -> new Vec3(0, 0, -0.9);
                    case SOUTH -> new Vec3(0, 0, 0.425);
                    case WEST -> new Vec3(-0.425, 0, 0);
                    case EAST -> new Vec3(0.425, 0, 0);
                    default -> Vec3.ZERO;
                };
                destination = destination.add(offset);
            }

            return destination;
    }

    @Override
    public boolean isStableDestination(BlockPos pos) { //Debug code to force Partial Passable movement, remove
        BlockPathTypes type = this.nodeEvaluator.getBlockPathType(this.level, pos.getX(), pos.getY(), pos.getZ());
        return type == ModPathTypes.PARTIAL_PASSABLE || super.isStableDestination(pos);
    }





    //Custom path shi


    public void followCustomPath(List<BlockPos> path) {
        if (this.path == null || this.path.isDone()) return;
        System.out.println("followCustomPath called");
        Node currentNode = this.path.getNextNode();
        BlockPos pos = new BlockPos(currentNode.x, currentNode.y, currentNode.z);
        BlockPathTypes type = this.nodeEvaluator.getBlockPathType(this.level, pos.getX(), pos.getY(), pos.getZ());

        Vec3 destination = new Vec3(currentNode.x + 0.5, currentNode.y, currentNode.z + 0.5);

        if (type == ModPathTypes.PARTIAL_PASSABLE) {
            Direction openDir = getOpenFenceSide(pos);
            Vec3 offset = switch (openDir) {
                case NORTH -> new Vec3(0, 0, -0.3);
                case SOUTH -> new Vec3(0, 0, 0.3);
                case WEST -> new Vec3(-0.3, 0, 0);
                case EAST -> new Vec3(0.3, 0, 0);
                default -> Vec3.ZERO;
            };
            destination = destination.add(offset);
        }

        this.mob.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, this.speedModifier);
    }


     public void setCustomPath(List<BlockPos> path) {
        this.customPath = path;
        if (!path.isEmpty()) {
            this.stop(); // Stop built-in path
            followCustomPath(path); // Manual movement
        }
    }

    @Override
    public void tick() {
        if (!customPath.isEmpty()) {
         followCustomPath(customPath); //Custom pathfinding behavior
        } else {
        super.tick(); //Fallback to vanilla

            // DEBUG CAN DELETE LATER
        }
        if (this.isInProgress()) {
            System.out.println("[NAV TICK] Moving to: " + this.getTargetPos());
        }
        if (this.path != null) {
            System.out.println("[DEBUG] Current path length: " + this.path.getNodeCount());
            for (int i = 0; i < this.path.getNodeCount(); i++) {
                Node node = this.path.getNode(i);
                System.out.println("[DEBUG] Node " + i + ": " + node.asBlockPos());
            }
        }
        //END OF DEBUG
    }

}
