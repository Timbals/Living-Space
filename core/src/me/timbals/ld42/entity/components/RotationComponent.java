package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class RotationComponent implements Component, Pool.Poolable {

    /**
     * rotation in degrees
     */
    public float rotation = 0.0f;
    public boolean lockedToPlanet = true;

    @Override
    public void reset() {
        rotation = 0.0f;
        lockedToPlanet = true;
    }
}
