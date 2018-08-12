package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Texture;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

public class MovementSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;
    private float planetRadius = 0f;

    public void setPlanetRadius(float planetRadius) {
        this.planetRadius = planetRadius;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            PositionComponent positionComponent = Mappers.position.get(e);

            // update the position relative to the planet if the entity moves in relation to it
            // otherwise we interpret the velocity of the entity as movement in the normal coordinate system
            if(Mappers.planetPosition.has(e)) {
                PlanetPositionComponent planetPosition = Mappers.planetPosition.get(e);

                if(Mappers.velocity.has(e)) {
                    VelocityComponent velocityComponent = Mappers.velocity.get(e);

                    // calculate the fraction of the total planet surface the entity wants to move
                    float surfaceLength = planetRadius * 2 * (float) Math.PI;
                    float fraction = velocityComponent.x / surfaceLength;
                    planetPosition.x += fraction * deltaTime;

                    planetPosition.y += velocityComponent.y * deltaTime;

                    // apply inertia if applicable
                    if(Mappers.inertia.has(e)) {
                        InertiaComponent inertiaComponent = Mappers.inertia.get(e);
                        velocityComponent.x -= (velocityComponent.x * inertiaComponent.factor) * deltaTime;
                    }

                    // apply gravity if applicable
                    if(Mappers.gravity.has(e)) {
                        GravityComponent gravityComponent = Mappers.gravity.get(e);
                        velocityComponent.y -= gravityComponent.strength * deltaTime;
                    }
                }

                // make sure the entity isn't inside of the planet
                if(planetPosition.y < planetRadius + planetPosition.maxY) {
                    planetPosition.y = planetRadius + planetPosition.maxY;
                    if(Mappers.velocity.has(e)) {
                        Mappers.velocity.get(e).y = 0;

                        if(Mappers.control.has(e)) {
                            ControlComponent controlComponent = Mappers.control.get(e);
                            controlComponent.enabled = true;
                        }
                    }
                }

                // the world x position is modeled by sin
                double x = planetPosition.y * Math.sin(2 * Math.PI * planetPosition.x);
                // the world y position is modeled by cos
                double y = planetPosition.y * Math.cos(2 * Math.PI * planetPosition.x);

                if(Mappers.size.has(e)) {
                    SizeComponent sizeComponent = Mappers.size.get(e);
                    x -= sizeComponent.width / 2;
                }

                if(Mappers.rotation.has(e)) {
                    RotationComponent rotationComponent = Mappers.rotation.get(e);
                    if(rotationComponent.lockedToPlanet) {
                        rotationComponent.rotation = 360f * (1 - (planetPosition.x % 1));
                    }
                }

                positionComponent.x = (float) x;
                positionComponent.y = (float) y;
            } else if(Mappers.position.has(e) && Mappers.velocity.has(e)) {
                VelocityComponent velocityComponent = Mappers.velocity.get(e);

                positionComponent.x += velocityComponent.x * deltaTime;
                positionComponent.y += velocityComponent.y * deltaTime;
            }
        }
    }

}