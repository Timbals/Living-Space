package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Rectangle;
import me.timbals.ld42.Game;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class CollisionSystem extends EntitySystem {

    private ImmutableArray<Entity> collectors;
    private ImmutableArray<Entity> collectibles;

    private Game game;

    public CollisionSystem(Game game) {
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        collectors = engine.getEntitiesFor(Family.all(PositionComponent.class, SizeComponent.class, CollectComponent.class).get());
        collectibles = engine.getEntitiesFor(Family.all(PositionComponent.class, SizeComponent.class, CollectibleComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(int i = 0; i < collectors.size(); i++) {
            Entity collector = collectors.get(i);
            PositionComponent positionComponent = Mappers.position.get(collector);
            SizeComponent sizeComponent = Mappers.size.get(collector);

            Rectangle rectangle = new Rectangle((int) positionComponent.x, (int) positionComponent.y, sizeComponent.width, sizeComponent.height);

            for(int j = 0; j < collectibles.size(); j++) {
                Entity collectible = collectibles.get(j);
                PositionComponent positionComponentCollectible = Mappers.position.get(collectible);
                SizeComponent sizeComponentCollectible = Mappers.size.get(collectible);

                Rectangle rectangleCollectible = new Rectangle((int) positionComponentCollectible.x, (int) positionComponentCollectible.y, sizeComponentCollectible.width, sizeComponentCollectible.height);

                if(rectangle.overlaps(rectangleCollectible)) {
                    if(!Mappers.collectible.has(collectible)) {
                        // the collectible was already collected
                        continue;
                    }
                    CollectibleComponent collectibleComponent = Mappers.collectible.get(collectible);

                    boolean remove = game.addCollectible(collectibleComponent.type, collector);

                    if(remove) {
                        getEngine().removeEntity(collectible);
                        // make sure it cannot be collected multiple times
                        collectible.remove(CollectibleComponent.class);

                        collectible.remove(GravityComponent.class);
                        collectible.remove(InertiaComponent.class);
                        collectible.remove(PlanetPositionComponent.class);

                        TargetComponent targetComponent = new TargetComponent();
                        targetComponent.x = game.viewportWidth;
                        targetComponent.y = game.viewportHeight;
                        collectible.add(targetComponent);
                    }
                }
            }
        }
    }
}