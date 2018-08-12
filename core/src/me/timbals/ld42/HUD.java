package me.timbals.ld42;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import me.timbals.ld42.entity.Mappers;
import me.timbals.ld42.entity.components.ControlComponent;

public class HUD {

    private Game game;
    private SpriteBatch batch;

    private BitmapFont font;

    private OrthographicCamera uiCamera;

    private ShapeRenderer shapeRenderer;

    // message system
    private String message;
    private float messageTimer;
    private float messageDelay = 5f;

    // buy menu
    private boolean buyOpen = false;
    private int selectedItem = 0;

    private int amountItems = 5;

    private Texture[] textures;
    private String[] descriptions;
    private int[] prices;
    boolean[] soldOut;
    private Texture soldOutTexture;

    private final String tutorialText =
            "You run your own planet. Your goal is to make the planet bigger by injecting rocks into it." +
                    "The people that live on this planet require living space to be happy. You loose if you run out of space that the people can use." +
                    "You gain resources by picking up rocks left behind by asteroids and sending rockets to harvest resources." +
                    "You have to purchase a gun and shoot the alien spaceships to prevent them from stealing mass.\n\n" +
                    "Controls\n" +
                    "A - move to left\n" +
                    "D - move to right\n" +
                    "E - interact/purchase item\n" +
                    "C - open shop\n" +
                    "SPACE - shoot upwards\n\n" +
                    "Press E to start";

    public HUD(Game game, SpriteBatch batch, ShapeRenderer shapeRenderer) {
        this.game = game;
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = new BitmapFont();

        uiCamera = new OrthographicCamera(game.viewportWidth, game.viewportHeight);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0f);

        textures = new Texture[] {
                game.assetManager.get("rocket.png", Texture.class),
                game.assetManager.get("education.png", Texture.class),
                game.assetManager.get("boots.png", Texture.class),
                game.assetManager.get("asteroid_wide.png", Texture.class),
                game.assetManager.get("gun.png", Texture.class)
        };
        soldOutTexture = game.assetManager.get("soldout.png");

        descriptions = new String[] {
                "Rocket\ncollects rocks from outer space and brings them back",
                "Education\nmakes the people smart enough to pick up rocks for you",
                "Hermes Boots\nincreases your walking speed",
                "Gravitational Anomaly\nincreases the number of asteroids that hit the planet",
                "Gun\ndefend yourself! (Press SPACE to shoot)"
        };

        for(int i = 0; i < descriptions.length; i++) {
            descriptions[i] += "\nPress E to buy";
        }

        prices = new int[] {
                25, 250, 15, 10, 5
        };

        soldOut = new boolean[amountItems];
    }

    public void sendMessage(String message) {
        this.message = message;
        this.messageTimer = this.messageDelay;
    }

    public void toggleBuyMenu() {
        buyOpen = !buyOpen;
    }

    public void buyItem() {
        if(soldOut[selectedItem]) {
            return;
        }

        buyOpen = false;

        if(game.rockCount >= prices[selectedItem]) {
            game.rockCount -= prices[selectedItem];
            sendMessage("bought ");
        } else {
            sendMessage("Not enough rocks!");
            return;
        }

        switch (selectedItem) {
            case 0:
                // rocket
                game.addRocket();
                break;
            case 1:
                // education
                game.enableSimplePeopleCollector();
                soldOut[1] = true;
                break;
            case 2:
                // boots
                Entity player = game.getPlayer();
                ControlComponent controlComponent = Mappers.control.get(player);
                controlComponent.speedMultiplier += 0.5f;

                if(controlComponent.speedMultiplier >= 8f) {
                    controlComponent.speedMultiplier = 8f;
                    soldOut[2] = true;
                }
                break;
            case 3:
                // asteroid
                game.asteroidDelay -= 1f;
                if(game.asteroidDelay <= 2f) {
                    game.asteroidDelay = 2f;
                    soldOut[3] = true;
                }
                break;
            case 4:
                // gun
                game.hasGun = true;
                soldOut[4] = true;
                break;
        }
    }

    public boolean isBuyMenuOpen() {
        return buyOpen;
    }

    public void selectNext() {
        selectedItem = checkSelectedItemBounds(selectedItem + 1);
    }

    public void selectPrevious() {
        selectedItem = checkSelectedItemBounds(selectedItem - 1);
    }

    private int checkSelectedItemBounds(int selected) {
        while(selected >= amountItems) {
            selected -= amountItems;
        }
        while (selected < 0) {
            selected += amountItems;
        }

        return selected;
    }

    public void render(float delta) {
        // prep
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);

        if(game.gameOver) {
            batch.begin();

            String text = game.won ? "Your planet has reached the maximum size" : "You ran out of space for your people";
            String text2 = game.won ? "Press ESC to continue playing" : "Press ESC to exit";
            String textureName = game.won ? "youwin.png" : "gameover.png";

            // draw the game over sign
            float scale = 8;
            int width = 45;
            int height = 8;
            batch.draw(game.assetManager.get(
                    textureName, Texture.class),
                    uiCamera.viewportWidth / 2 - width * scale / 2,
                    uiCamera.viewportHeight / 2 - height * scale / 2,
                    width * scale,
                    height * scale);

            // draw an explanation message
            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, text);

            font.setColor(Color.WHITE);
            font.draw(
                    batch,
                    text,
                    uiCamera.viewportWidth / 2 - layout.width / 2,
                    uiCamera.viewportHeight / 2 - layout.height / 2 - height * scale);

            // draw a message that prompts further action
            layout.setText(font, text2);

            font.setColor(Color.WHITE);
            font.draw(
                    batch,
                    text2,
                    uiCamera.viewportWidth / 2 - layout.width / 2,
                    uiCamera.viewportHeight / 2 - layout.height / 2 - height * scale * 2);

            batch.end();
            return;
        } else if(game.tutorial) {
            // clear some of the background for better legibility
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.BLACK);
            float borderLeft = 128;
            float borderBottom = 32;
            shapeRenderer.rect(
                    borderLeft,
                    borderBottom,
                    uiCamera.viewportWidth - borderLeft * 2,
                    uiCamera.viewportHeight - borderBottom * 2);
            shapeRenderer.end();

            batch.begin();

            float scale = 12f;
            batch.draw(
                    game.assetManager.get("logo.png", Texture.class),
                    uiCamera.viewportWidth / 2 - 52 * scale / 2,
                    uiCamera.viewportHeight / 2 - 9 * scale / 2 + uiCamera.viewportHeight / 4,
                    52 * scale,
                    9 * scale);

            GlyphLayout layout = new GlyphLayout();
            layout.setText(
                    font,
                    tutorialText,
                    Color.WHITE,
                    uiCamera.viewportWidth / 2,
                    Align.center,
                    true);

            font.setColor(Color.WHITE);
            font.draw(
                    batch,
                    tutorialText,
                    uiCamera.viewportWidth / 2 - layout.width / 2,
                    uiCamera.viewportHeight / 2 + 48,
                    uiCamera.viewportWidth / 2,
                    Align.center,
                    true);

            batch.end();
            return;
        }

        // shapes
        // calculate an indicator that scales with the amount of space left on the planet
        // if there is space for 10 or more people on the planet, the indicator is at maximum
        float freeSpaceIndicator = game.capacityLeft / 10f;
        freeSpaceIndicator = Math.min(1f, freeSpaceIndicator);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(freeSpaceIndicator <= 0.2f ? Color.RED : Color.WHITE);
        shapeRenderer.rect(8, uiCamera.viewportHeight - 7 * 6 - 16, 63f * 3 * freeSpaceIndicator, 7f * 3);
        shapeRenderer.end();

        // batch
        batch.begin();

        String text = "" + game.rockCount;

        GlyphLayout layout = new GlyphLayout();
        font.setColor(Color.WHITE);

        if(messageTimer > 0) {
            messageTimer -= delta;

            layout.setText(font, message);
            font.draw(batch, message, uiCamera.viewportWidth / 2 - layout.width / 2, uiCamera.viewportHeight / 2 + 70 + 64 * (1 - (messageTimer / messageDelay)));
        }

        layout.setText(font, text);
        font.draw(batch, text, uiCamera.viewportWidth - layout.width - 32, uiCamera.viewportHeight - layout.height);

        batch.draw(game.assetManager.get("rock.png", Texture.class), uiCamera.viewportWidth - 32, uiCamera.viewportHeight - 32, 32f, 32f);

        batch.draw(game.assetManager.get("freelivingspace.png", Texture.class), 8, uiCamera.viewportHeight - 7 * 3 - 8, 63f * 3, 7f * 3);

        batch.draw(game.assetManager.get("shop.png", Texture.class), 0,0, 47 * 3, 11 * 3);

        if(buyOpen) {
            batch.draw(getTexture(checkSelectedItemBounds(selectedItem + 1)), game.viewportWidth / 2 + 56, game.viewportHeight / 2 + 48, 32, 32);
            batch.draw(getTexture(selectedItem), game.viewportWidth / 2, game.viewportHeight / 2 + 64, 32, 32);
            batch.draw(getTexture(checkSelectedItemBounds(selectedItem - 1)), game.viewportWidth / 2 - 56, game.viewportHeight / 2 + 48, 32, 32);
            batch.draw(game.assetManager.get("outline.png", Texture.class), game.viewportWidth / 2 - 8, game.viewportHeight / 2 + 56, 48, 48);

            String cost = "cost: " + prices[selectedItem];
            String description = descriptions[selectedItem];
            if(soldOut[selectedItem]) {
                cost = "";
                description = "sold out";
            }

            layout.setText(font, cost);
            font.draw(batch, cost, uiCamera.viewportWidth / 2 - layout.width / 2, uiCamera.viewportHeight / 2 + 128);
            layout.setText(font, description, Color.WHITE, 0f, Align.center, false);
            font.draw(batch, description, uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2 + 144 + layout.height, 0f, Align.center, false);
        }

        batch.end();
    }

    private Texture getTexture(int index) {
        if(soldOut[index]) {
            return soldOutTexture;
        } else {
            return textures[index];
        }
    }

}
