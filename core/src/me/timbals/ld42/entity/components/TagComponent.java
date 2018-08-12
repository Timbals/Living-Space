package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class TagComponent implements Component, Pool.Poolable {

    public String tag;

    @Override
    public void reset() {
        tag = null;
    }
}
