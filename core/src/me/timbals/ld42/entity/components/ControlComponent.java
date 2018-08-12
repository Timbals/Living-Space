package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ControlComponent implements Component, Pool.Poolable {

    public boolean enabled = true;
    public float baseSpeed = 500f;
    public float maxSpeed = 100f;
    public float speedMultiplier = 1f;

    @Override
    public void reset() {
        enabled = true;
        baseSpeed = 500f;
        speedMultiplier = 1f;
        maxSpeed = 100f;
    }

}
