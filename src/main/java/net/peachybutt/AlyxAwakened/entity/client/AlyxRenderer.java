package net.peachybutt.AlyxAwakened.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.entity.custom.AlyxEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AlyxRenderer extends GeoEntityRenderer<AlyxEntity> {

    public AlyxRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AlyxModel());
    }

    @Override
    public ResourceLocation getTextureLocation(AlyxEntity animatable) {
        return new ResourceLocation(AlyxAwakened.MOD_ID, "textures/entity/alyx.png");
    }

    @Override
    public void render(AlyxEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.4f, 0.4f, 0.4f);
        }


        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
