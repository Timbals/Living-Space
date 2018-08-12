package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;

public class AnimationComponent implements Component, Pool.Poolable {

    public Animation<TextureRegion> animation;
    public float stateTime = 0;
    public boolean enabled = true;

    @Override
    public void reset() {
        animation = null;
        stateTime = 0;
        enabled = true;
    }
}
