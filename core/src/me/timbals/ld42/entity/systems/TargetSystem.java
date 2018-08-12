package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.PositionComponent;
import me.timbals.ld42.entity.components.TargetComponent;
import me.timbals.ld42.entity.components.VelocityComponent;

public class TargetSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private OrthographicCamera camera;

    public TargetSystem(OrthographicCamera camera) {
        this.camera = camera;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class, VelocityComponent.class, TargetComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            PositionComponent positionComponent = Mappers.position.get(e);
            VelocityComponent velocityComponent = Mappers.velocity.get(e);
            TargetComponent targetComponent = Mappers.target.get(e);

            Vector3 positionVec = camera.project(new Vector3(positionComponent.x, positionComponent.y, 0));
            Vector3 targetVec = new Vector3(targetComponent.x, targetComponent.y, 0);

            float diffX = targetVec.x - positionVec.x;
            float diffY = targetVec.y - positionVec.y;

            Vector2 vector = new Vector2(diffX, diffY);
            vector.nor();

            velocityComponent.x = vector.x * targetComponent.speed;
            velocityComponent.y = vector.y * targetComponent.speed;
        }
    }
}
