package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

public class ControlSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(ControlComponent.class, PositionComponent.class, VelocityComponent.class, AnimationComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D);

        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            ControlComponent controlComponent = Mappers.control.get(e);
            VelocityComponent velocityComponent = Mappers.velocity.get(e);
            AnimationComponent animationComponent = Mappers.animation.get(e);
            TextureComponent textureComponent = Mappers.texture.get(e);

            // don't do anything if the component is disabled
            if(!controlComponent.enabled) {
                animationComponent.enabled = false;
                return;
            }

            if(right) {
                velocityComponent.x += controlComponent.baseSpeed * controlComponent.speedMultiplier * deltaTime;
                animationComponent.enabled = true;
                textureComponent.flipX = false;
            } else if(left) {
                velocityComponent.x -= controlComponent.baseSpeed * controlComponent.speedMultiplier * deltaTime;
                animationComponent.enabled = true;
                textureComponent.flipX = true;
            } else {
                animationComponent.enabled = false;
                if(velocityComponent.x > 0) {
                    velocityComponent.x -= controlComponent.baseSpeed * controlComponent.speedMultiplier * deltaTime;
                    if(velocityComponent.x < 0) {
                        velocityComponent.x = 0;
                    }
                } else if(velocityComponent.x < 0) {
                    velocityComponent.x += controlComponent.baseSpeed * controlComponent.speedMultiplier * deltaTime;
                    if(velocityComponent.x > 0) {
                        velocityComponent.x = 0;
                    }
                }
            }

            if(velocityComponent.x > controlComponent.maxSpeed * controlComponent.speedMultiplier) {
                velocityComponent.x = controlComponent.maxSpeed * controlComponent.speedMultiplier;
            } else if(velocityComponent.x < -controlComponent.maxSpeed * controlComponent.speedMultiplier) {
                velocityComponent.x = -controlComponent.maxSpeed * controlComponent.speedMultiplier;
            }
        }
    }
}
