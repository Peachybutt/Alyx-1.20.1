package net.peachybutt.AlyxAwakened.entity.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.peachybutt.AlyxAwakened.entity.custom.sub.brain.AlyxBrain;
import net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation.AlyxGroundPathNav;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Set;
import java.util.UUID;

//Current ideas for to do in alyx; Generate seeded data, particularly a PERSONALITY and mood values that alter what she does (including reputation and similar values).
//To go above and beyond for this we should be making custom animations for each PERSONALITY whereas the mood values will alter what she actually can do.




public class AlyxEntity extends PathfinderMob implements GeoEntity, NeutralMob {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int remainingPersistentAngerTime;
    private int goalCount;
    private static final UniformInt PERSISTENT_ANGER_TIME;
    @Nullable
    private UUID persistentAngerTarget;

    public AlyxEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) { //This is our super that constructs the entity
        super(pEntityType, pLevel);
        this.moveControl = new MoveControl(this);
        this.refreshDimensions();
        this.setPathfindingMalus(BlockPathTypes.COCOA, 0.0F); //Defined in AlyxPathLogic
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F); //Defined in AlyxPathLogic
    }

    @Override
    protected Brain.Provider<AlyxEntity> brainProvider() {
        return Brain.provider(
                ImmutableList.of(
                        MemoryModuleType.ATTACK_TARGET,
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryModuleType.PATH
                ),
                ImmutableList.of(
                        ModSensorTypes.ALYX_NEAREST_LIVING_ENTITIES.get()
                )
        );
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain.Provider<AlyxEntity> provider = this.brainProvider();
        Brain<AlyxEntity> brain = provider.makeBrain(dynamic);
        return AlyxBrain.makeBrain(this, brain);
    }


    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new AlyxGroundPathNav(this, pLevel);
    }

    public static AttributeSupplier setAttributes() { //these are the stats for our entity
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.4f).build();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.6F, 1.95F); //Avg player height
    }


    /*@Override
    protected void registerGoals() {
        this.goalSelector.addGoal(goalCount, new FloatGoal(this));
        this.goalSelector.addGoal(goalCount++, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(goalCount++, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(goalCount++, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(goalCount++, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(goalCount++, new NearestAttackableTargetGoal<>(this, Creeper.class, true));
    }*/

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData groupData,
            @Nullable CompoundTag tag
    ) {
        this.getNavigation().setCanFloat(true); // Navigation is fully initialized here
        return super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
    }

    @Override // This is only necessary if the Generic Attack Animation is not swinging.
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
    }

    @Override
    public void travel(Vec3 travelVec) {
        System.out.println("[TRAVEL] Vector: " + travelVec + " | Delta: " + this.getDeltaMovement());
        if (!this.isEffectiveAi()) return;

        double gravity = 0.08;
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -gravity, 0));
        }

        if (this.onGround()) {
            // Use entity's move speed
            float speed = this.getSpeed(); // gets movement speed attribute
            this.moveRelative(speed, travelVec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.91, 0.98, 0.91));
        } else {
            // Air movement
            this.moveRelative(0.02f, travelVec);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.91, 0.98, 0.91));
        }

        this.calculateEntityAnimation(false);
    }


    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.level().getProfiler().push("alyxBrain");
        ((Brain<AlyxEntity>) this.getBrain()).tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.getNavigation().tick();    //Nav tick, could bog down stuff so maybe delete if unnecessary.
        this.getMoveControl().tick();
        this.navigation.tick();
        System.out.println("[MOVE CONTROL] delta = " + getDeltaMovement());
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "controller", 0, this::attackPredicate));
    }


    private void updateSwingTime(int i) {
    }

    protected <E extends PathfinderMob> PlayState predicate(AnimationState<AlyxEntity> alyxEntityAnimationState) {
        if(alyxEntityAnimationState.isMoving()) {
            alyxEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.alyx.walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        alyxEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.alyx.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    private PlayState attackPredicate(AnimationState<AlyxEntity> alyxEntityAnimationState) {
        alyxEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.alyx.attack", Animation.LoopType.PLAY_ONCE));
        return PlayState.CONTINUE;
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.ARROW_HIT_PLAYER,0.1F, 4.0F);
    }

    protected SoundEvent getAmbientSound() {return SoundEvents.ZOMBIE_ATTACK_IRON_DOOR;}

    protected SoundEvent getHurtSound() {return SoundEvents.ZOMBIE_HURT;}

    protected SoundEvent getDeathSound() {return SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR;}

    protected float getSoundVolume() {return 0.2F;}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    static {
        PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(1, 99); //Random angry time, grrr
    }

    //Bla bla bla shes mad, you haven't touched this

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        this.remainingPersistentAngerTime = pTime;
    }

    @Override
    public @Nullable UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }
}
