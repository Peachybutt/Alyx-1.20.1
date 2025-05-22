package net.peachybutt.AlyxAwakened.entity.custom;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
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

public class AlyxEntity extends PathfinderMob implements GeoEntity, NeutralMob {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int remainingPersistentAngerTime;
    private int goalCount;
    private static final UniformInt PERSISTENT_ANGER_TIME;
    @Nullable
    private UUID persistentAngerTarget;

    public AlyxEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) { //This is our super that constructs the entity
        super(pEntityType, pLevel);
        this.setPathfindingMalus(BlockPathTypes.COCOA, 0.0F); //Defined in AlyxPathLogic
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F); //Defined in AlyxPathLogic
    }

    @Override
    protected Brain.Provider<AlyxEntity> brainProvider() {
        return Brain.provider(
                Set.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.NEAREST_VISIBLE_PLAYER),
                Set.of(SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_LIVING_ENTITIES)
        );
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return AlyxBrain.makeBrain(this, this.brainProvider().makeBrain(dynamic));
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
    protected void registerGoals() {
        this.goalSelector.addGoal(goalCount, new FloatGoal(this));
        this.goalSelector.addGoal(goalCount++, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(goalCount++, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(goalCount++, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(goalCount++, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(goalCount++, new NearestAttackableTargetGoal<>(this, Creeper.class, true));
    }

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

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "controller", 0, this::attackPredicate));
    }

    @Override // This is only necessary if the Generic Attack Animation is not swinging.
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
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
