package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class VisibilityComponent implements Component, Pool.Poolable {

    public boolean visible = true;
    public float alpha = 1f;

    @Override
    public void reset() {
        visible = true;
        alpha = 1f;
    }
}
