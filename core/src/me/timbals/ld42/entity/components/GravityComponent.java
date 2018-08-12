package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class GravityComponent implements Component, Pool.Poolable {

    public float strength = 25f;

    @Override
    public void reset() {
        strength = 25f;
    }
}
