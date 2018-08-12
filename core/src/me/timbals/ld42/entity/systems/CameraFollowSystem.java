package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

public class CameraFollowSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private OrthographicCamera camera;

    private float currentRotation = 0f;

    public CameraFollowSystem(OrthographicCamera camera) {
        this.camera = camera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class, CameraFollowComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            // CameraFollowComponent cameraFollowComponent = Mappers.cameraFollow.get(e);
            PositionComponent positionComponent = Mappers.position.get(e);

            // TODO make tracking more accurate by taking rotation into account

            SizeComponent sizeComponent = Mappers.size.get(e);

            PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);

            camera.position.set(
                    positionComponent.x,
                    positionComponent.y,
                    0);

            if(Mappers.rotation.has(e)) {
                RotationComponent rotationComponent = Mappers.rotation.get(e);

                float rotationDiff = rotationComponent.rotation - currentRotation;
                camera.rotate(-rotationDiff);
                currentRotation += rotationDiff;
            }

            camera.update();
        }
    }
}
