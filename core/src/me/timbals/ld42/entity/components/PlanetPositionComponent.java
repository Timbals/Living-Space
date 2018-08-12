package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PlanetPositionComponent implements Component, Pool.Poolable {

    // x is the position around the surface of the planet (0-1)
    public float x = 0;
    // y is the distance to the planet center
    public float y = 0;
    public float maxY = 0;

    @Override
    public void reset() {
        x = 0;
        y = 0;
        maxY = 0;
    }
}
