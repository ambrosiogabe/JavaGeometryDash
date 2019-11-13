package com.jade.main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.jade.components.BoxBounds;
import com.jade.components.Sprite;
import com.jade.dataStructures.Vector2;
import com.jade.jade.Camera;
import com.jade.jade.GameObject;
import com.jade.jade.Transform;
import com.jade.scripts.Ground;
import com.jade.scripts.Music;
import com.jade.scripts.ParallaxBackground;
import com.jade.scripts.Player;

import com.jade.file.Parser;
import com.jade.jade.Renderer;
import com.jade.ui.Grid;

public class LevelScene extends Scene {
    public List<GameObject> gameObjects;
    public Renderer renderingEngine;

    private static LevelScene scene = null;
    private Music stereoMadness = null;

    public GameObject player;
    Sprite playerLayerOneSpritesheet, playerLayerTwoSpritesheet, playerLayerThreeSpritesheet;

    public LevelScene(String name) {
        super.Scene(name);
        gameObjects = new ArrayList<>();

        camera = new Camera(new Transform(new Vector2(0.0f, 0.0f)));
        renderingEngine = new Renderer(camera);
    }

    public static LevelScene getScene() {
        if (LevelScene.scene == null) {
            LevelScene.scene = new LevelScene("Level 1");
        }

        return LevelScene.scene;
    }

    public static void deleteScene() {
        if (LevelScene.getScene() != null && LevelScene.getScene().stereoMadness != null)
            LevelScene.getScene().stereoMadness.stop();
        LevelScene.scene = null;
    }

    @Override
    public void init() {
        initPlayer();
        initBackgrounds();
        importLevel("levelOutput");
        stereoMadness = new Music("assets/stereoMadness.wav");
    }

    public void addGameObject(GameObject gameObj) {
        gameObjects.add(gameObj);
        renderingEngine.submit(gameObj);
        gameObj.parent = null;
    }

    @Override
    public void update(double dt) {
        player.update(dt);
        player.getComponent(BoxBounds.class).onGround = false;
        for (GameObject go : gameObjects) {

            if ((go.transform.position.x > camera.transform.position.x - Constants.TILE_WIDTH * 2 && go.transform.position.x < camera.transform.position.x + Constants.SCREEN_WIDTH) ||
                    go.getComponent(ParallaxBackground.class) != null) {
                go.update(dt);
            }
        }

        camera.transform.position.x = player.transform.position.x + Constants.CAMERA_OFFSET_X;

        float cameraDistanceY = player.transform.position.y - camera.transform.position.y;
        if (cameraDistanceY < Constants.CAMERA_BOX_TOP_Y || cameraDistanceY > Constants.CAMERA_BOX_BOTTOM_Y ) {
            if (cameraDistanceY < Constants.CAMERA_BOX_TOP_Y && player.getComponent(Player.class).state == 0) {
                camera.transform.position.y -= Constants.CAMERA_BOX_TOP_Y - cameraDistanceY;
            } else if (cameraDistanceY > Constants.CAMERA_BOX_BOTTOM_Y && player.getComponent(Player.class).state == 0){
                camera.transform.position.y += cameraDistanceY - Constants.CAMERA_BOX_BOTTOM_Y;
            }
        }

        if (camera.transform.position.y > Constants.CAMERA_OFFSET_Y) {
            camera.transform.position.y = Constants.CAMERA_OFFSET_Y;
        }

        if (Window.keyListener.isKeyPressed(KeyEvent.VK_F1)) {
            Window.getWindow().changeScene(0);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        renderingEngine.draw(g2);
    }

    private void importLevel(String filename) {
        File tmp = new File("levels/" + filename + ".zip");
        if (!tmp.exists()) return;

        try {
            ZipFile zipFile = new ZipFile("levels/" + filename + ".zip");
            ZipEntry jsonFile = zipFile.getEntry(filename + ".json");
            InputStream stream = zipFile.getInputStream(jsonFile);
            byte[] bytes = stream.readAllBytes();
            Parser.init(0, bytes);
            GameObject go = Parser.getNextGameObject();

            while (go != null) {
                addGameObject(go);
                go = Parser.getNextGameObject();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private void initPlayer() {
        playerLayerOneSpritesheet = new Sprite("assets/player/layerOne.png", true, 42, 42, 13, 2, 13 * 5);
        playerLayerOneSpritesheet.loadSpritesheet();

        playerLayerTwoSpritesheet = new Sprite("assets/player/layerTwo.png", true, 42, 42, 13, 2, 13 * 5);
        playerLayerTwoSpritesheet.loadSpritesheet();

        playerLayerThreeSpritesheet = new Sprite("assets/player/layerThree.png", true, 42, 42, 13, 2, 13 * 5);
        playerLayerThreeSpritesheet.loadSpritesheet();

        Sprite spaceship = new Sprite("assets/player/spaceship.png", true, 42, 42, 1, 2, 1);
        spaceship.loadSpritesheet();

        int playerImg = 20;
        player = new GameObject("Player", new Transform(new Vector2(-90.0f, 0)));
        BoxBounds bounds = new BoxBounds(Constants.PLR_WIDTH, Constants.PLR_HEIGHT, true, false);
        player.addComponent(bounds);
        player.setZIndex(1);
        player.addComponent(new Player(playerLayerOneSpritesheet.sprites.get(playerImg).subImg.image, playerLayerTwoSpritesheet.sprites.get(playerImg).subImg.image,
                playerLayerThreeSpritesheet.sprites.get(playerImg).subImg.image, Color.GREEN, Color.YELLOW, spaceship.sprites.get(0).subImg.image, true));
        renderingEngine.submit(player);
    }

    private void initBackgrounds() {
        Sprite blocks = new Sprite("assets/spritesheet.png", true, Constants.TILE_WIDTH, Constants.TILE_WIDTH, 6, 2, 12);
        blocks.loadSpritesheet();

        Sprite spikes = new Sprite("assets/spikes.png", true, Constants.TILE_WIDTH, Constants.TILE_WIDTH, 6, 2, 4);
        spikes.loadSpritesheet();

        Sprite bigSprites = new Sprite("assets/bigSprites.png", true, Constants.TILE_WIDTH * 2, Constants.TILE_WIDTH * 2, 2, 2, 2);
        bigSprites.loadSpritesheet();

        Sprite smallBlocks = new Sprite("assets/smallBlocks.png", true, Constants.TILE_WIDTH, Constants.TILE_WIDTH, 6, 2, 1);
        smallBlocks.loadSpritesheet();

        Sprite portal = new Sprite("assets/portal.png", true, 44, 85, 2, 2, 2);
        portal.loadSpritesheet();

        GameObject ground = new GameObject("Ground", new Transform(new Vector2(0.0f, Constants.GROUND_HEIGHT)));
        ground.addComponent(new BoxBounds(Constants.SCREEN_WIDTH, 300.0f, true, false));
        Ground groundScript = new Ground(player);
        ground.addComponent(groundScript);
        addGameObject(ground);

        int numBackgrounds = 7;
        GameObject[] backgrounds = new GameObject[numBackgrounds];
        GameObject[] groundBgs = new GameObject[numBackgrounds];
        for (int i=0; i < numBackgrounds; i++) {
            int x = i * Constants.BG_WIDTH;
            int y = 0;
            GameObject go = new GameObject("Background", new Transform(new Vector2(x, y)));
            go.setUi(true);
            ParallaxBackground bg = new ParallaxBackground(Constants.BG_WIDTH, Constants.BG_HEIGHT,
                    "assets/backgrounds/bg01.png", backgrounds, true, ground.getComponent(Ground.class), false);
            go.addComponent(bg);
            go.setZIndex(-101);
            backgrounds[i] = go;

            x = i * Constants.GROUND_BG_WIDTH;
            y = Constants.BG_HEIGHT;
            GameObject groundGo = new GameObject("GroundBg", new Transform(new Vector2(x, y)));
            groundGo.setUi(true);
            ParallaxBackground groundBg = new ParallaxBackground(Constants.GROUND_BG_WIDTH, Constants.GROUND_BG_HEIGHT,
                    "assets/grounds/ground01.png", groundBgs, true, ground.getComponent(Ground.class), true);
            groundGo.addComponent(groundBg);
            groundGo.setZIndex(-100);
            groundBgs[i] = groundGo;

            addGameObject(go);
            addGameObject(groundGo);
        }
    }
}
