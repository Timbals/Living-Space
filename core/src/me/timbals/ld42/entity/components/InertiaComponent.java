package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class InertiaComponent implements Component, Pool.Poolable {

    public float factor = 0.7f;

    @Override
    public void reset() {
        factor = 0.7f;
    }
}
