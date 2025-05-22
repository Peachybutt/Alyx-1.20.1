package net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

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
        this.alyxNodeEvaluator.setCanWalkOverFences(true);
        this.alyxNodeEvaluator.setCanFloat(true);

        this.nodeEvaluator = this.alyxNodeEvaluator;

        this.alyxPathLogic = new AlyxPathLogic();
        return new PathFinder(this.alyxNodeEvaluator, maxVisitedNodes);
    }
}
