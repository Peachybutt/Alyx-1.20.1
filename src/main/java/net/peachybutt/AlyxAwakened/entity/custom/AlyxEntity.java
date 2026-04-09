package net.peachybutt.AlyxAwakened.entity.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.peachybutt.AlyxAwakened.entity.animations.ModAnimationsDefinitions;
import net.peachybutt.AlyxAwakened.entity.custom.sub.brain.AlyxAi;
import net.peachybutt.AlyxAwakened.entity.custom.sub.pathnavigation.AlyxGroundPathNav;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

//Current ideas for to do in alyx; Generate seeded data, particularly a PERSONALITY and mood values that alter what she does (including reputation and similar values).
//To go above and beyond for this we should be making custom animations for each PERSONALITY whereas the mood values will alter what she actually can do.




public class AlyxEntity extends PathfinderMob implements GeoEntity, NeutralMob {

    //Generic referenced variables, etc

    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int remainingPersistentAngerTime;
    private int goalCount;
    private static final UniformInt PERSISTENT_ANGER_TIME;
    @Nullable
    private UUID persistentAngerTarget;
    private int personality;

    static {
        SENSOR_TYPES = ImmutableList.of(
                ModSensorTypes.ALYX_NEAREST_LIVING_ENTITIES.get());
        MEMORY_TYPES = ImmutableList.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                MemoryModuleType.LOOK_TARGET,
                MemoryModuleType.WALK_TARGET,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                MemoryModuleType.PATH);
        PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(1, 99); //Random angry time, grrr
    }

    protected static final ImmutableList<SensorType<? extends Sensor<? super AlyxEntity>>> SENSOR_TYPES;
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;

    public AlyxEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) { //This is our super that constructs the entity
        super(pEntityType, pLevel);
        this.moveControl = new MoveControl(this);
        this.refreshDimensions();
        this.setPathfindingMalus(BlockPathTypes.COCOA, 0.0F); //Defined in AlyxPathLogic
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F); //Defined in AlyxPathLogic
    }



    //Brain




    @Override
    protected Brain.Provider<AlyxEntity> brainProvider() {
        return Brain.provider( MEMORY_TYPES, SENSOR_TYPES); //These memory/sensor types are provided within static variables above
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
        return AlyxAi.makeBrain(this, this.brainProvider().makeBrain(pDynamic));
    }

    @Override
    public Brain<AlyxEntity> getBrain() {
        return (Brain<AlyxEntity>) super.getBrain();
    }


    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        //return new AlyxGroundPathNav(this, pLevel);
        return new GroundPathNavigation(this, pLevel);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("alyxBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("alyxActivityUpdate");
        AlyxAi.updateActivity(this);
        this.level().getProfiler().pop();
    }

    @Override // This is only necessary if the Generic Attack Animation is not swinging.
    public void aiStep() {
        super.aiStep();
        this.updateSwingTime();
    }


    //Memory, saved data, generated data, etc

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Personality", this.personality);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setPersonality(pCompound.getInt("Personality"));
    }

    private void setPersonality(int pDemeanor) {
        this.personality = pDemeanor;
    }



    //General




    public static AttributeSupplier.Builder createAttributes() { //these are the stats for our entity
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.4f);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(0.6F, 1.95F); //Avg player height
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




    //Graphics, sound, animation, etc



    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attack_controller", 0, this::attackPredicate));
    }

    protected <E extends PathfinderMob> PlayState predicate(AnimationState<AlyxEntity> alyxEntityAnimationState) {
        if (alyxEntityAnimationState.isMoving()) {
            alyxEntityAnimationState.getController().setAnimation(ModAnimationsDefinitions.ALYX_WALK);
            return PlayState.CONTINUE;
        }
        alyxEntityAnimationState.getController().setAnimation(ModAnimationsDefinitions.ALYX_IDLE);
        return PlayState.CONTINUE;
    }

    private PlayState attackPredicate(AnimationState<AlyxEntity> alyxEntityAnimationState) {
        if (this.swinging) {
            alyxEntityAnimationState.getController().setAnimation(ModAnimationsDefinitions.ALYX_ATTACK);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
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
