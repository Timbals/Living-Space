package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class CameraFollowComponent implements Component, Pool.Poolable {

    // not used right now because it doesn't work for rotation
    public float tween = 0.9f;

    @Override
    public void reset() {
        tween = 0.9f;
    }
}
