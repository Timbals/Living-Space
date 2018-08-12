package me.timbals.ld42;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetManager;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;
import me.timbals.ld42.entity.systems.SmashToRocksSystem;

public class RocketBehavior {

    private Game game;
    private PooledEngine engine;
    private AssetManager assetManager;

    private float rocketTimer = 0;
    private float rocketDelay = 10f;

    public RocketBehavior(Game game, PooledEngine engine, AssetManager assetManager) {
        this.game = game;
        this.engine = engine;
        this.assetManager = assetManager;
    }

    public void update(float delta) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(TagComponent.class).get());

        rocketTimer += delta;
        int deltaTime = (int) rocketTimer;
        if(deltaTime > 0) {
            rocketTimer = rocketTimer % 1;
        }

        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            TagComponent tagComponent = Mappers.tag.get(e);
            if(tagComponent.tag.equals("rocket")) {
                SizeComponent sizeComponent = Mappers.size.get(e);
                VelocityComponent velocityComponent = Mappers.velocity.get(e);

                if(deltaTime >= 0) {
                    // only launch rockets that are on the stationary on the ground
                    if(sizeComponent.height == 32) {
                        e.flags -= deltaTime;

                        if(e.flags <= 0) {
                            // launch rocket
                            TextureComponent textureComponent = Mappers.texture.get(e);
                            textureComponent.texture = assetManager.get("rocket_launching.png");

                            sizeComponent.height = 64;

                            velocityComponent.y = 50;

                            // reset the flag for the next use
                            e.flags = (int) rocketDelay;
                        }
                    }
                }

                // check if the rocket is launched from the planet already
                if(sizeComponent.height == 64) {
                    PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);

                    // check if the rocket is still flying upwards or if it has already landed
                    if(velocityComponent.y > 0) {
                        // flying upwards
                        // if the rocket is high enough we reverse it's velocity so it returns to the planet
                        if(planetPositionComponent.y >= 1000 + game.planetRadius) {
                            velocityComponent.y = -velocityComponent.y;
                        }
                    } else if(velocityComponent.y == 0) {
                        // if the rocket has touched the planet, we change it back to its original form
                        TextureComponent textureComponent = Mappers.texture.get(e);
                        textureComponent.texture = assetManager.get("rocket.png");

                        sizeComponent.height = 32;

                        // make sure the rocket is standing on top of its launch pad
                        planetPositionComponent.y = game.planetRadius + 8;

                        // spawn some rocks that the rocket has brought from outer space
                        int amountRocks = 10 + (int) (Game.random.nextFloat() * 10f);
                        for(int j = 0; j < amountRocks; j++) {
                            SmashToRocksSystem.addRock(planetPositionComponent.x, planetPositionComponent.y, engine, assetManager);
                        }
                    }
                }
            }
        }
    }

    public void addRocket(float planetPositionX) {
        // spawn a rocket with a launch pad
        Entity rocket = engine.createEntity();

        rocket.add(new PositionComponent());
        rocket.add(new RotationComponent());
        rocket.add(new VelocityComponent());

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.x = planetPositionX;
        planetPositionComponent.y = game.planetRadius + 8;
        rocket.add(planetPositionComponent);

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = assetManager.get("rocket.png");
        rocket.add(textureComponent);

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = 32;
        sizeComponent.height = 32;
        rocket.add(sizeComponent);

        TagComponent tagComponent = new TagComponent();
        tagComponent.tag = "rocket";
        rocket.add(tagComponent);

        rocket.flags = (int) rocketDelay;

        engine.addEntity(rocket);

        Entity launchPad = engine.createEntity();

        launchPad.add(new PositionComponent());
        launchPad.add(new RotationComponent());

        PlanetPositionComponent planetPositionComponentPad = new PlanetPositionComponent();
        planetPositionComponentPad.x = planetPositionComponent.x;
        launchPad.add(planetPositionComponentPad);

        TextureComponent textureComponentPad = new TextureComponent();
        textureComponentPad.texture = assetManager.get("launch_pad.png");
        launchPad.add(textureComponentPad);

        SizeComponent sizeComponentPad = new SizeComponent();
        sizeComponentPad.width = 32;
        sizeComponentPad.height = 8;
        launchPad.add(sizeComponentPad);

        engine.addEntity(launchPad);
    }

}
