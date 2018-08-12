package me.timbals.ld42;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.*;
import me.timbals.ld42.entity.systems.*;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends ApplicationAdapter {

    public static Random random;

    // game states
    public boolean gameOver = false;
    public boolean won = false;
    public boolean resume = false;
    public boolean tutorial = true;
    public float winningRadius = 2048f;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Music music;
    private Sound asteroidSound;
    private Sound pickupSound;
    private Sound pewSound;

	private OrthographicCamera camera;
    private OrthographicCamera backgroundCamera;

	public float viewportWidth;
	public float viewportHeight;

	public HUD hud;

	// planet texturing
	private Texture planetTexture;
	private TextureData planetTextureData;
	private Pixmap planetPixmap;
    private int planetTexturePower = 8;
    private Texture maskedPlanetTexture;

	private PooledEngine entityEngine;
	public AssetManager assetManager;

	private MovementSystem movementSystem;
	public float planetRadius = 128f;

    public int rockCount = 10;
    private float insertTimer = 0;
    private float insertDelay = 0.025f;

    private float asteroidTimer = 0;
    public float asteroidDelay = 7f;

    private float ufoDelay = 10f;
    private float ufoTimer = ufoDelay;
    private float ufoMinRadius = 196f;

    private float personTimer = 0;
    private float personDelayMin = 3f;
    private float personDelayMax = 10f;
    private float personDelay = personDelayMax;
    public float freeSpace = 1f;
    private float spacePerPerson = 100;
    public float capacityLeft = 1f;

    public boolean hasGun = false;
    private float gunTimer = 0f;
    private float gunDelay = 0.25f;

    // entities
    private Entity player;
    private Entity device;
    private Entity insertRocks;

    // entity behaviors
    public SimplePeopleBehavior simplePeopleBehavior;
    public RocketBehavior rocketBehavior;
    public UFOBehavior ufoBehavior;

	@Override
	public void create () {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		assetManager = new AssetManager();

		music = Gdx.audio.newMusic(Gdx.files.internal("song.mp3"));
		music.setLooping(true);
		music.setVolume(0.5f);
		music.play();

        asteroidSound = Gdx.audio.newSound(Gdx.files.internal("asteroid.mp3"));
        pickupSound = Gdx.audio.newSound(Gdx.files.internal("pickup.mp3"));
        pewSound = Gdx.audio.newSound(Gdx.files.internal("pew.mp3"));

		long seed = new Random().nextLong();
		System.out.println("starting new world; seed: " + seed);
        random = new Random(seed);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(1000, 1000 * (h / w));
		camera.position.set(0f, 0f, 0f);
        camera.update();

        viewportWidth = camera.viewportWidth;
        viewportHeight = camera.viewportHeight;

        backgroundCamera = new OrthographicCamera(viewportWidth, viewportHeight);
        backgroundCamera.position.set(viewportWidth / 2 + 1024, viewportHeight / 2 + 1024, 0);
        backgroundCamera.update();

        assetManager.load("simplepeople.png", Texture.class);
		assetManager.load("player.png", Texture.class);
		assetManager.load("asteroid.png", Texture.class);
        assetManager.load("rock.png", Texture.class);
        assetManager.load("player_walk.png", Texture.class);
        assetManager.load("freelivingspace.png", Texture.class);
        assetManager.load("insert_rocks.png", Texture.class);
        assetManager.load("device.png", Texture.class);
        assetManager.load("rocket.png", Texture.class);
        assetManager.load("launch_pad.png", Texture.class);
        assetManager.load("outline.png", Texture.class);
        assetManager.load("rocket_launching.png", Texture.class);
        assetManager.load("education.png", Texture.class);
        assetManager.load("background.png", Texture.class);
        assetManager.load("boots.png", Texture.class);
        assetManager.load("shop.png", Texture.class);
        assetManager.load("asteroid_wide.png", Texture.class);
        assetManager.load("soldout.png", Texture.class);
        assetManager.load("gun.png", Texture.class);
        assetManager.load("player_gun.png", Texture.class);
        assetManager.load("ufo.png", Texture.class);
        assetManager.load("simplepeople_walk.png", Texture.class);
        assetManager.load("gameover.png", Texture.class);
        assetManager.load("youwin.png", Texture.class);
        assetManager.load("simplepeople_spawn.png", Texture.class);
        assetManager.load("bullet.png", Texture.class);
        assetManager.load("beam.png", Texture.class);
        assetManager.load("logo.png", Texture.class);
        assetManager.load("planet.png", Texture.class);

        assetManager.finishLoading();

        planetTexture = assetManager.get("planet.png", Texture.class);
        planetTextureData = planetTexture.getTextureData();
        if(!planetTextureData.isPrepared()) {
            planetTextureData.prepare();
        }
        planetPixmap = planetTextureData.consumePixmap();
        recreatePlanetTexture();

		// TODO create loading screen for assets

        Gdx.gl.glClearColor(0, 0, 0, 1);

        hud = new HUD(this, batch, shapeRenderer);

		entityEngine = new PooledEngine();

        entityEngine.addSystem(new ControlSystem());
        entityEngine.addSystem(new CameraFollowSystem(camera));
        movementSystem = new MovementSystem();
        movementSystem.setPlanetRadius(planetRadius);
        entityEngine.addSystem(movementSystem);
        entityEngine.addSystem(new RenderSystem(batch));
        entityEngine.addSystem(new SmashToRocksSystem(assetManager));
        entityEngine.addSystem(new CollisionSystem(this));
        entityEngine.addSystem(new TargetSystem(camera));

        createPlayer();
        createDevice();

        simplePeopleBehavior = new SimplePeopleBehavior(entityEngine, assetManager);
        simplePeopleBehavior.addSimplePeople();
        simplePeopleBehavior.addSimplePeople();

        rocketBehavior = new RocketBehavior(this, entityEngine, assetManager);

        ufoBehavior = new UFOBehavior(this, entityEngine);
	}

	public void createPlayer() {
	    player = entityEngine.createEntity();

        player.add(new PositionComponent());
        player.add(new VelocityComponent());
        player.add(new RotationComponent());
        player.add(new CameraFollowComponent());
        player.add(new CollectComponent());

        GravityComponent gravityComponent = new GravityComponent();
        gravityComponent.strength = 250f;
        player.add(gravityComponent);

        // make sure controls are disabled until the player is on the planet
        ControlComponent controlComponent = new ControlComponent();
        controlComponent.enabled = false;
        player.add(controlComponent);

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        // spawn the player above the planet for a nicer entry
        planetPositionComponent.y = planetRadius * 2 + viewportHeight / 2;
        // don't start above the device
        planetPositionComponent.x = 0.25f;
        player.add(planetPositionComponent);

        SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = 64;
        sizeComponent.height = 64;
        player.add(sizeComponent);

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = assetManager.get("player.png");
        player.add(textureComponent);

        AnimationComponent animationComponent = new AnimationComponent();

        TextureRegion[][] tmp = TextureRegion.split(assetManager.get("player_walk.png", Texture.class), 32, 32);

        int cols = 6;
        int rows = 1;
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        animationComponent.animation = new Animation(0.1f, (Object[]) frames);
        player.add(animationComponent);

        entityEngine.addEntity(player);
    }

    public Entity getPlayer() {
	    return player;
    }

    public void enableSimplePeopleCollector() {
	    simplePeopleBehavior.collector = true;

	    ImmutableArray<Entity> entities = entityEngine.getEntitiesFor(Family.all(TagComponent.class).get());
	    for(int i = 0; i < entities.size(); i++) {
	        Entity e = entities.get(i);
	        TagComponent tagComponent = Mappers.tag.get(e);

	        if(tagComponent.tag.equals("simplePeople")) {
	            e.add(new CollectComponent());
            }
        }
    }

    boolean deviceTouched = false;

    public void createDevice() {
	    device = entityEngine.createEntity();

	    device.add(new PositionComponent());
	    device.add(new PlanetPositionComponent());
	    device.add(new RotationComponent());

	    CollectibleComponent collectibleComponent = new CollectibleComponent();
	    collectibleComponent.type = 1;
	    device.add(collectibleComponent);

	    TextureComponent textureComponent = new TextureComponent();
	    textureComponent.texture = assetManager.get("device.png");
	    device.add(textureComponent);

	    SizeComponent sizeComponent = new SizeComponent();
	    sizeComponent.width = 48;
	    sizeComponent.height = 48;
	    device.add(sizeComponent);

	    entityEngine.addEntity(device);

	    insertRocks = entityEngine.createEntity();

	    insertRocks.add(new PositionComponent());
	    insertRocks.add(new RotationComponent());

	    TextureComponent textureComponentRocks = new TextureComponent();
	    textureComponentRocks.texture = assetManager.get("insert_rocks.png");
	    insertRocks.add(textureComponentRocks);

	    SizeComponent sizeComponentRocks = new SizeComponent();
	    sizeComponentRocks.width = 87 * 2;
	    sizeComponentRocks.height = 9 * 2;
	    insertRocks.add(sizeComponentRocks);

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.y = sizeComponent.height + sizeComponentRocks.height + planetRadius;
        insertRocks.add(planetPositionComponent);

        VisibilityComponent visibilityComponent = new VisibilityComponent();
        visibilityComponent.visible = false;
        insertRocks.add(visibilityComponent);

	    entityEngine.addEntity(insertRocks);
    }

    public void addAsteroid() {
	    // choose a random place for the asteroid to come down
	    float planetXPosition = random.nextFloat();
	    // choose a random scale between 0.5 and 2
        float scale = 0.5f + random.nextFloat() * 1.5f;

	    addAsteroid(planetXPosition, scale);
    }

    public void addAsteroid(float planetXPosition, float scale) {
	    Entity asteroid = entityEngine.createEntity();

        asteroid.add(new PositionComponent());
        asteroid.add(new RotationComponent());
        asteroid.add(new SmashToRocksComponent());

        PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
        planetPositionComponent.x = planetXPosition;
        planetPositionComponent.y = 1000 + planetRadius;
        asteroid.add(planetPositionComponent);

	    VelocityComponent velocityComponent = new VelocityComponent();
	    velocityComponent.y = -250;
	    asteroid.add(velocityComponent);

	    SizeComponent sizeComponent = new SizeComponent();
        sizeComponent.width = (int) (32 * scale);
        sizeComponent.height = (int) (64 * scale);
	    asteroid.add(sizeComponent);

        TextureComponent textureComponent = new TextureComponent();
        textureComponent.texture = assetManager.get("asteroid.png");
        asteroid.add(textureComponent);

        TagComponent tagComponent = new TagComponent();
        tagComponent.tag = "asteroid";
        asteroid.add(tagComponent);

	    entityEngine.addEntity(asteroid);
    }

    public void addRocket() {
        rocketBehavior.addRocket(Mappers.planetPosition.get(player).x);
    }

    public boolean addCollectible(int type, Entity collector) {
	    switch (type) {
            case 0:
                pickupSound.play();
                rockCount++;
                return true;
            case 1:
                deviceTouched = true;
                return false;
            case 2:
                if(Mappers.tag.has(collector)) {
                    TagComponent tagComponent = Mappers.tag.get(collector);
                    if(tagComponent.tag.equals("ufo")) {
                        ufoBehavior.hit(collector);
                        return true;
                    }
                }
                return false;
            default:
                return true;
        }
    }

    public void increasePlanetRadius(float radius) {
        ImmutableArray<Entity> entities = entityEngine.getEntitiesFor(Family.all(PlanetPositionComponent.class).get());
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);
            planetPositionComponent.y += radius;
        }

        planetRadius += radius;
    }

    private void handleInputs(float delta) {
	    if(insertTimer > 0) {
	        insertTimer -= delta;
        }

        if(hud.isBuyMenuOpen()) {
            if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                hud.selectPrevious();
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                hud.selectNext();
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)) {
	        if(tutorial) {
	            tutorial = false;
            } else if(hud.isBuyMenuOpen()) {
	            if(Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    hud.buyItem();
                }
            } else if(rockCount > 0 && insertTimer <= 0) {
                PositionComponent posPlayer = Mappers.position.get(player);
                SizeComponent sizePlayer = Mappers.size.get(player);

                Rectangle rectanglePlayer = new Rectangle((int) posPlayer.x, (int) posPlayer.y, sizePlayer.width, sizePlayer.height);

                PositionComponent posDevice = Mappers.position.get(device);
                SizeComponent sizeDevice = Mappers.size.get(device);
                Rectangle rectangleDevice = new Rectangle((int) posDevice.x, (int) posDevice.y, sizeDevice.width, sizeDevice.height);

                if(rectanglePlayer.overlaps(rectangleDevice)) {
                    rockCount--;
                    insertTimer += insertDelay;
                    increasePlanetRadius(1f);
                }
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
	        if(hasGun) {
	            if(gunTimer <= 0) {
                    gunTimer = gunDelay;

                    pewSound.play();

                    TextureComponent textureComponentPlayer = Mappers.texture.get(player);
                    textureComponentPlayer.texture = assetManager.get("player_gun.png");
                    textureComponentPlayer.priority = true;

                    SizeComponent sizeComponentPlayer = Mappers.size.get(player);

                    // bullet
                    Entity bullet = entityEngine.createEntity();

                    bullet.add(new PositionComponent());
                    bullet.add(new RotationComponent());

                    PlanetPositionComponent planetPositionComponent = new PlanetPositionComponent();
                    PlanetPositionComponent planetPositionComponentPlayer = Mappers.planetPosition.get(player);

                    float xOffset = (((float) sizeComponentPlayer.width / 4f) / (planetRadius * (float) Math.PI * 2f));

                    if(textureComponentPlayer.flipX) {
                        xOffset = -xOffset;
                    }

                    planetPositionComponent.x = planetPositionComponentPlayer.x + xOffset;
                    planetPositionComponent.y = planetPositionComponentPlayer.y + sizeComponentPlayer.height;
                    bullet.add(planetPositionComponent);

                    VelocityComponent velocityComponent = new VelocityComponent();
                    velocityComponent.y = 500;
                    bullet.add(velocityComponent);

                    TextureComponent textureComponent = new TextureComponent();
                    textureComponent.texture = assetManager.get("bullet.png");
                    bullet.add(textureComponent);

                    CollectibleComponent collectibleComponent = new CollectibleComponent();
                    collectibleComponent.type = 2;
                    bullet.add(collectibleComponent);

                    SizeComponent sizeComponent = new SizeComponent();
                    sizeComponent.width = 8;
                    sizeComponent.height = 8;
                    bullet.add(sizeComponent);

                    TagComponent tagComponent = new TagComponent();
                    tagComponent.tag = "bullet";
                    bullet.add(tagComponent);

                    entityEngine.addEntity(bullet);
                }
            } else {
                hud.sendMessage("purchase a gun first");
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.C)) {
	        hud.toggleBuyMenu();
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
	        if(gameOver) {
	            // if the player has won, he can continue playing
                // otherwise we exit the game
	            if(won) {
                    resume = true;
                    gameOver = false;
                } else {
	                Gdx.app.exit();
                }
            }
        }
    }

	@Override
	public void render () {
        // get the time that has passed since the last frame
        float delta = Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw the background
        backgroundCamera.update();
        batch.setProjectionMatrix(backgroundCamera.combined);
        batch.begin();
        batch.draw(assetManager.get("background.png", Texture.class), 0, 0, 2048, 2048);
        batch.end();

        // tick
        // delta is used to make the game logic independent of FPS
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        handleInputs(delta);

        movementSystem.setPlanetRadius(planetRadius);

        if(tutorial || gameOver) {
            hud.render(delta);
            return;
        }

        shapeRenderer.setProjectionMatrix(camera.combined);
        /*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BROWN);
        shapeRenderer.circle(0, 0, planetRadius, (int) Math.sqrt(planetRadius) * 6);
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.circle(0, 0, planetRadius / 2,(int) Math.sqrt(planetRadius) * 6);
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(0, 0, planetRadius / 6,(int) Math.sqrt(planetRadius) * 6);
        shapeRenderer.end();*/

        if(getPlanetTextureSize() < planetRadius * 2) {
            planetTexturePower++;
            System.out.println("masked planet texture not the correct size, new size: " + getPlanetTextureSize());
            recreatePlanetTexture();
        }

        Gdx.gl.glClearDepthf(1f);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glColorMask(false, false, false, false);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(0, 0, planetRadius, (int) Math.sqrt(planetRadius) * 6);
        shapeRenderer.end();

        batch.begin();
        batch.draw(maskedPlanetTexture, -planetRadius, -planetRadius);

        Gdx.gl.glColorMask(true, true, true, true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_EQUAL);

        batch.end();

        Gdx.gl.glColorMask(true, true, true, true);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        simplePeopleBehavior.update(delta);
        rocketBehavior.update(delta);
        ufoBehavior.update(delta);

        deviceTouched = false;
        entityEngine.update(delta);
        Mappers.visibility.get(insertRocks).visible = deviceTouched;

        updateTimer(delta);

        updateFreeSpace();
        checkAsteroidSound();
        updateBackgroundCamera(delta);

        cleanUp();

        // game title
        int FPS = Math.round(1f / delta);
        Gdx.graphics.setTitle("Living Space - " + FPS);

        // draw UI elements
        // use a different camera to not be affected by normal camera transformations
        hud.render(delta);
	}

	private int getPlanetTextureSize() {
        return (int) Math.pow(2, planetTexturePower);
    }

	private void recreatePlanetTexture() {
        // TODO clean this up more by using texture regions
        int scale = 10;

        int width = getPlanetTextureSize();
        int height = getPlanetTextureSize();
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int clampedX = (x / scale) % planetPixmap.getWidth();
                int clampedY = (y / scale) % planetPixmap.getHeight();
                int color = planetPixmap.getPixel(clampedX, clampedY);
                pixmap.drawPixel(x, y, color);
            }
        }

        maskedPlanetTexture = new Texture(pixmap);
    }

	private void cleanUp() {
        ImmutableArray<Entity> entities = entityEngine.getEntitiesFor(Family.all(TagComponent.class).get());
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            TagComponent tagComponent = Mappers.tag.get(e);
            if(tagComponent.tag.equals("bullet")) {
                PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);

                // remove the bullet when it is off the screen
                if(planetPositionComponent.y >= planetRadius + viewportHeight * 5) {
                    entityEngine.removeEntity(e);
                }
            }
        }

        // TODO put this into a different place
        personDelay = personDelayMin + (personDelayMax - personDelayMin) * (1 - (planetRadius / (winningRadius / 2)));
        personDelay = Math.max(personDelay, personDelayMin);
    }

	private void updateTimer(float delta) {
	    asteroidTimer -= delta;
	    if(asteroidTimer <= 0) {
	        asteroidTimer += asteroidDelay;

            addAsteroid();
        }

        personTimer -= delta;
	    if(personTimer <= 0) {
	        personTimer += personDelay;

	        simplePeopleBehavior.addSimplePeople();
        }

        if(gunTimer > 0) {
	        gunTimer -= delta;
	        if(gunTimer <= 0) {
	            Mappers.texture.get(player).texture = assetManager.get("player.png");
	            Mappers.texture.get(player).priority = false;
            }
        }

        if(planetRadius >= ufoMinRadius && ufoTimer > 0) {
	        ufoTimer -= delta;
	        while(ufoTimer <= 0) {
	            ufoTimer += ufoDelay;
	            ufoBehavior.createUFO();

	            if(!ufoTip) {
	                ufoTip = true;
	                hud.sendMessage("Shoot the alien ships with a gun before they steal the mass of the planet");
                }
	        }
        }
    }

    private boolean ufoTip = false;

    private void updateBackgroundCamera(float delta) {
        timer += delta;

        backgroundCamera.position.add((float) Math.sin(timer / 10f) * 10 * delta, (float) Math.cos(timer / 10f) * 10 * delta, 0);

        if(backgroundCamera.position.x > 2048 - viewportWidth / 2) {
            backgroundCamera.position.x = 2048 - viewportWidth / 2;
        } else if(backgroundCamera.position.x < 0 + viewportWidth / 2) {
            backgroundCamera.position.x = 0 + viewportWidth / 2;
        }

        if(backgroundCamera.position.y > 2048 - viewportHeight / 2) {
            backgroundCamera.position.y = 2048 - viewportHeight / 2;
        } else if(backgroundCamera.position.y < 0 + viewportHeight / 2) {
            backgroundCamera.position.y = 0 + viewportHeight / 2;
        }
    }

    private float timer = 0f;

	private void updateFreeSpace() {
	    int personCount = 0;
        ImmutableArray<Entity> entities = entityEngine.getEntitiesFor(Family.all(TagComponent.class).get());
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            TagComponent tagComponent = Mappers.tag.get(e);
            if(tagComponent.tag.equals("simplePeople")) {
                personCount++;
            }
        }

	    float surface = planetRadius * (float) Math.PI * 2f;
	    freeSpace = (surface - spacePerPerson * personCount) / surface;

	    float surfaceLeft = surface - spacePerPerson * personCount;
	    capacityLeft = surfaceLeft / spacePerPerson;

	    // if there isn't enough free space left, the player looses
        // if the win condition is met and the player hasn't been shown the win screen yet, show it.
	    if(freeSpace <= 0) {
	        gameOver = true;
        } else if(!resume && planetRadius >= winningRadius) {
            gameOver = true;
            won = true;
        }
    }

    private void checkAsteroidSound() {
        ImmutableArray<Entity> entities = entityEngine.getEntitiesFor(Family.all(TagComponent.class).get());
        for(int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            TagComponent tagComponent = Mappers.tag.get(e);
            if(tagComponent.tag.equals("asteroid")) {
                PlanetPositionComponent planetPositionComponent = Mappers.planetPosition.get(e);
                if(planetPositionComponent.y - planetRadius < 600) {
                    if(e.flags == 0) {
                        e.flags = 1;
                        asteroidSound.play();
                    }
                }
            }
        }
    }

	@Override
	public void dispose () {
		batch.dispose();
		assetManager.dispose();
		planetTexture.dispose();
		maskedPlanetTexture.dispose();
	}
}
