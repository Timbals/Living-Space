package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class TargetComponent implements Component, Pool.Poolable {

    public float x = 0;
    public float y = 0;
    public float speed = 1000;
    public boolean removeOnTarget = true;

    @Override
    public void reset() {
        x = 0;
        y = 0;
        speed = 1000;
        removeOnTarget = true;
    }
}
