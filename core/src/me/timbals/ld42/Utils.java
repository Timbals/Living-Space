package me.timbals.ld42;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Utils {

    public static Animation<TextureRegion> createAnimation(Texture spriteSheet, int tileWidth, int tileHeight, int rows, int columns, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, tileWidth, tileHeight);

        TextureRegion[] frames = new TextureRegion[columns * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        return new Animation(frameDuration, (Object[]) frames);
    }

}
