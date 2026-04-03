package net.peachybutt.AlyxAwakened.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class AlyxModel extends GeoModel<AlyxEntity> {
    private final ResourceLocation model = new ResourceLocation(AlyxAwakened.MOD_ID, "geo/alyx.geo.json");
    private final ResourceLocation texture = new ResourceLocation(AlyxAwakened.MOD_ID, "textures/entity/alyx.png");
    private final ResourceLocation animations = new ResourceLocation(AlyxAwakened.MOD_ID, "animations/alyx.animation.json");

    @Override
    public ResourceLocation getModelResource(AlyxEntity alyxEntity) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(AlyxEntity alyxEntity) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(AlyxEntity alyxEntity) {
        return this.animations;
    }

    @Override
    public void setCustomAnimations(AlyxEntity animatable, long instanceId, AnimationState<AlyxEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("Root");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}