package me.timbals.ld42;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import me.timbals.ld42.Game;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;

import java.util.ArrayList;
import java.util.List;

public class SimplePeopleBehavior {

    private PooledEngine entityEngine;
    private AssetManager assetManager;

    private float elapsedTime = 0;

    // makes all new simple people collect rocks
    public boolean collector = false;

    public SimplePeopleBehavior(PooledEngine entityEngine, AssetManager assetManager) {
        this.entityEngine = entityEngine;
        this.assetManager = assetManager;
    }

    public void addSimplePeople() {
        addSimplePeople(Game.random.nextFloat());
    }

    public void addSimplePeople(float planetPositionX) {
        Entity simplePeople = entityEngine.createEntity();

        simplePeople.add(new PositionComponent());

        if(collector) {
            simplePeople.add(new CollectComponent());
        }

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.x = planetPositionX;
        simplePeople.add(planetPositionComponent);

        simplePeople.add(new RotationComponent());
        simplePeople.add(new VelocityComponent());

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = 32;
        sizeComponent.height = 32;
        simplePeople.add(sizeComponent);

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = assetManager.get("simplepeople.png");
        simplePeople.add(textureComponent);

        // first use the spawn animation, it will later get replaced by the walk animation
        AnimationComponent animationComponent = new AnimationComponent();
        animationComponent.animation = Utils.createAnimation(assetManager.get("simplepeople_spawn.png", Texture.class), 16, 16, 1, 8, 0.2f);
        simplePeople.add(animationComponent);

        TagComponent tagComponent = new TagComponent();
        tagComponent.tag = "simplePeople";
        simplePeople.add(tagComponent);

        entityEngine.addEntity(simplePeople);
    }

    public void update(float deltaTime) {
        elapsedTime += deltaTime;

        int elapsedMs = (int) (elapsedTime / 0.001);
        elapsedTime = (float) (elapsedTime % 0.001);

        ImmutableArray<Entity> entities = entityEngine.getEntitiesFor(Family.all(TagComponent.class).get());
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);

            TagComponent tagComponent = Mappers.tag.get(e);
            if(tagComponent.tag.equals("simplePeople")) {
                AnimationComponent animationComponent = Mappers.animation.get(e);
                TextureComponent textureComponent = Mappers.texture.get(e);

                // replace the spawn animation with the walk animation after it has played
                if(animationComponent.stateTime >= animationComponent.animation.getAnimationDuration()) {
                    if(animationComponent.animation.getFrameDuration() != 0.1f) {
                        VelocityComponent velocityComponent = Mappers.velocity.get(e);
                        velocityComponent.x = Game.random.nextBoolean() ? -20 : 20;
                        animationComponent.animation = Utils.createAnimation(assetManager.get("simplepeople_walk.png", Texture.class), 16, 16, 1, 6, 0.1f);
                    }
                }

                e.flags -= elapsedMs;
                if(e.flags <= 0) {
                    VelocityComponent velocityComponent = Mappers.velocity.get(e);
                    velocityComponent.x = -velocityComponent.x;
                    e.flags = (int) ((2 + Game.random.nextFloat() * 5) * 1000);

                    textureComponent.flipX = velocityComponent.x < 0;
                }
            }
        }
    }

}
