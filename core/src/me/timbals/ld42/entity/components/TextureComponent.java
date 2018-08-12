package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool;

public class TextureComponent implements Component, Pool.Poolable {

    public Texture texture;
    public boolean flipX = false;
    public boolean flipY = false;
    // if enabled, will take priority over animations
    public boolean priority = false;

    // only used by the render system and should not be edited by anything else
    public Sprite sprite;

    @Override
    public void reset() {
        texture = null;
        flipX = false;
        flipY = false;
        priority = false;
        sprite = null;
    }
}
