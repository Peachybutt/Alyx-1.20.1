package net.peachybutt.AlyxAwakened.entity.animations;

import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

public class ModAnimationsDefinitions {

    // Standard anims
    public static final RawAnimation ALYX_IDLE = RawAnimation.begin()
            .then("animation.alyx.idle", Animation.LoopType.LOOP);

    public static final RawAnimation ALYX_WALK = RawAnimation.begin()
            .then("animation.alyx.walk", Animation.LoopType.LOOP);

    public static final RawAnimation ALYX_ATTACK = RawAnimation.begin()
            .then("animation.alyx.attack", Animation.LoopType.PLAY_ONCE);
}