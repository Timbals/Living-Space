package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class SizeComponent implements Component, Pool.Poolable {

    public int width = 0;
    public int height = 0;

    @Override
    public void reset() {
        width = 0;
        height = 0;
    }
}
