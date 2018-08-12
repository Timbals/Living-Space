package me.timbals.ld42.entity.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetManager;
import me.timbals.ld42.Game;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

public class SmashToRocksSystem extends EntitySystem {

    private static final int baseAmountRocks = 8;

    private ImmutableArray<Entity> entities;

    private AssetManager assetManager;

    public SmashToRocksSystem(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlanetPositionComponent.class, SmashToRocksComponent.class, VelocityComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            VelocityComponent velocityComponent = Mappers.velocity.get(e);
            // when the planet is touched, the y velocity will be set to 0
            if(velocityComponent.y == 0) {
                PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);
                SizeComponent sizeComponent = Mappers.size.get(e);

                // scale the amount of rocks that drop to the size of the asteroid
                float scale = (float) sizeComponent.width / 32f;
                int amountRocks = (int) (baseAmountRocks * scale);

                for(int j = 0; j < amountRocks; j++) {
                    addRock(planetPositionComponent.x, planetPositionComponent.y);
                }

                getEngine().removeEntity(e);
            }
        }
    }

    public void addRock(float x, float y) {
        addRock(x, y, (PooledEngine) getEngine(), assetManager);
    }

    public static void addRock(float x, float y, PooledEngine engine, AssetManager assetManager) {
        Entity e = engine.createEntity();

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.x = x;
        // spawn the rocks above the ground so they can fly in random directions
        planetPositionComponent.y = y;
        e.add(planetPositionComponent);

        VelocityComponent velocityComponent = new VelocityComponent();
        velocityComponent.x = -70 + Game.random.nextFloat() * 140;
        velocityComponent.y = 10 + Game.random.nextFloat() * 20;
        e.add(velocityComponent);

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = 16;
        sizeComponent.height = 16;
        e.add(sizeComponent);

        e.add(new GravityComponent());
        e.add(new PositionComponent());
        e.add(new RotationComponent());
        e.add(new InertiaComponent());
        e.add(new CollectibleComponent());

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = assetManager.get("rock.png");
        e.add(textureComponent);

        engine.addEntity(e);
    }

}
