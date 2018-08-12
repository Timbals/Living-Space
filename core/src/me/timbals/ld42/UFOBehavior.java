package me.timbals.ld42;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.PooledEngine;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;
import me.timbals.ld42.entity.systems.SmashToRocksSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UFOBehavior {

    private static final int UFO_SPAWN_HEIGHT = 512;
    private static final int UFO_HEIGHT = 196;

    private Game game;
    private PooledEngine engine;

    private List<Entity> entities = new ArrayList<Entity>();
    private HashMap<Entity, Entity> ufoBeamMap = new HashMap<Entity, Entity>();
    private List<Entity> fakeRocks = new ArrayList<Entity>();

    public UFOBehavior(Game game, final PooledEngine engine) {
        this.game = game;
        this.engine = engine;

        engine.addEntityListener(new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
            }
            @Override
            public void entityRemoved(Entity entity) {
                if(entities.contains(entity)) {
                    entities.remove(entity);
                    Entity beam = ufoBeamMap.get(entity);
                    engine.removeEntity(beam);
                    ufoBeamMap.remove(entity);
                }

                if(fakeRocks.contains(entity)) {
                    fakeRocks.remove(entity);
                }
            }
        });
    }

    public void createUFO() {
        Entity ufo = engine.createEntity();

        ufo.add(new PositionComponent());
        ufo.add(new RotationComponent());
        ufo.add(new GravityComponent());
        ufo.add(new CollectComponent());

        VelocityComponent velocityComponent = new VelocityComponent();
        velocityComponent.y = -0.1f;
        ufo.add(velocityComponent);

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = 64;
        sizeComponent.height = 32;
        ufo.add(sizeComponent);

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.x = game.random.nextFloat();
        planetPositionComponent.y = game.planetRadius + UFO_SPAWN_HEIGHT;
        planetPositionComponent.maxY = UFO_HEIGHT;
        ufo.add(planetPositionComponent);

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = game.assetManager.get("ufo.png");
        ufo.add(textureComponent);

        TagComponent tagComponent = new TagComponent();
        tagComponent.tag = "ufo";
        ufo.add(tagComponent);

        engine.addEntity(ufo);
        entities.add(ufo);

        Entity beam = engine.createEntity();

        beam.add(new PositionComponent());
        beam.add(new RotationComponent());

        VisibilityComponent visibilityComponent = new VisibilityComponent();
        visibilityComponent.alpha = 0f;
        beam.add(visibilityComponent);

        PlanetPositionComponent planetPositionComponentBeam = new PlanetPositionComponent();
        planetPositionComponentBeam.x = planetPositionComponent.x;
        beam.add(planetPositionComponentBeam);

        TextureComponent textureComponentBeam = new TextureComponent();
        textureComponentBeam.texture = game.assetManager.get("beam.png");
        beam.add(textureComponentBeam);

        SizeComponent sizeComponentBeam = new SizeComponent();
        sizeComponentBeam.width = 32;
        sizeComponentBeam.height = 196;
        beam.add(sizeComponentBeam);

        engine.addEntity(beam);
        ufoBeamMap.put(ufo, beam);
    }

    private float timer = 0f;

    public void update(float delta) {
        timer += delta;
        int deltaInt = 0;
        while(timer >= 0.1f) {
            deltaInt++;
            timer -= 0.1f;
        }

        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            // slowly make the beam visible if the ufo has reached it's final height
            VelocityComponent velocityComponent = Mappers.velocity.get(e);
            if(velocityComponent.y == 0) {
                Entity beam = ufoBeamMap.get(e);
                VisibilityComponent visibilityComponent = Mappers.visibility.get(beam);
                visibilityComponent.alpha += delta;
                visibilityComponent.alpha = Math.min(1f, visibilityComponent.alpha);

                e.flags -= deltaInt;
                if(e.flags <= 0) {
                    PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);

                    e.flags += 20; // 2 seconds
                    addFakeRock(planetPositionComponent.x);

                    game.increasePlanetRadius(-1f);
                }
            }

            // remove the ufo if it is flying high because it got shot
            PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);
            if(planetPositionComponent.y >= game.planetRadius + 1024) {
                engine.removeEntity(e);
            }
        }

        for(int i = 0; i < fakeRocks.size(); i++) {
            Entity e = fakeRocks.get(i);
            PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);

            if(planetPositionComponent.y >= game.planetRadius + UFO_HEIGHT) {
                engine.removeEntity(e);
            }
        }
    }

    public void addFakeRock(float planetPositionX) {
        Entity rock = engine.createEntity();

        rock.add(new PositionComponent());
        rock.add(new RotationComponent());

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.x = planetPositionX;
        planetPositionComponent.y = game.planetRadius + 8;
        rock.add(planetPositionComponent);

        VelocityComponent velocityComponent = new VelocityComponent();
        velocityComponent.y = 20;
        rock.add(velocityComponent);

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = game.assetManager.get("rock.png");
        rock.add(textureComponent);

        engine.addEntity(rock);
        fakeRocks.add(rock);
    }

    public void hit(Entity ufo) {
        VelocityComponent velocityComponent = Mappers.velocity.get(ufo);
        velocityComponent.y = 250;

        VisibilityComponent visibilityComponent = Mappers.visibility.get(ufoBeamMap.get(ufo));
        visibilityComponent.visible = false;

        // drop all the rocks that are currently being sucked in
        PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(ufo);
        for(int i = 0; i < fakeRocks.size(); i++) {
            Entity rock = fakeRocks.get(i);
            PlanetPositionComponent planetPositionComponentRock = Mappers.planetPosition.get(rock);
            if(planetPositionComponentRock.x == planetPositionComponent.x) {
                SmashToRocksSystem.addRock(planetPositionComponentRock.x, planetPositionComponentRock.y, engine, game.assetManager);

                engine.removeEntity(rock);
            }
        }
    }

}
