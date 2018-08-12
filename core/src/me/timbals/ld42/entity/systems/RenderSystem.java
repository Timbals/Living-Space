package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.sun.glass.ui.Size;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

public class RenderSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;
    private SpriteBatch spriteBatch;

    public RenderSystem(SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class, TextureComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        spriteBatch.begin();

        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            if(Mappers.visibility.has(e)) {
                VisibilityComponent visibilityComponent = Mappers.visibility.get(e);
                if(!visibilityComponent.visible) {
                    continue;
                }
            }

            PositionComponent position = Mappers.position.get(e);
            TextureComponent textureComponent = Mappers.texture.get(e);

            if(textureComponent.sprite == null) {
                textureComponent.sprite = new Sprite(textureComponent.texture);
            }
            textureComponent.sprite.setRegion(textureComponent.texture);

            // the entity has an animation that should be displayed instead of the texture
            if(Mappers.animation.has(e) && !textureComponent.priority) {
                AnimationComponent animationComponent = Mappers.animation.get(e);

                if(animationComponent.enabled) {
                    animationComponent.stateTime += deltaTime;
                    TextureRegion frame = animationComponent.animation.getKeyFrame(animationComponent.stateTime, true);
                    textureComponent.sprite.setRegion(frame);
                }
            }

            if(Mappers.visibility.has(e)) {
                VisibilityComponent visibilityComponent = Mappers.visibility.get(e);
                textureComponent.sprite.setAlpha(visibilityComponent.alpha);
            }

            textureComponent.sprite.setFlip(textureComponent.flipX, textureComponent.flipY);
            textureComponent.sprite.setPosition(position.x, position.y);

            if(Mappers.size.has(e)) {
                SizeComponent sizeComponent = Mappers.size.get(e);
                textureComponent.sprite.setSize(sizeComponent.width, sizeComponent.height);

                textureComponent.sprite.setOrigin(sizeComponent.width / 2, 0);
            }

            if(Mappers.rotation.has(e)) {
                RotationComponent rotationComponent = Mappers.rotation.get(e);
                textureComponent.sprite.setRotation(rotationComponent.rotation);
            }

            textureComponent.sprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }
}
