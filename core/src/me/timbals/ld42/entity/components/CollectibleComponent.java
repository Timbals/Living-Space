package me.timbals.ld42.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class CollectibleComponent implements Component, Pool.Poolable {

    // 0=rock, 1=insertRocksText, 2=bullet
    public int type = 0;

    @Override
    public void reset() {
        type = 0;
    }
}
