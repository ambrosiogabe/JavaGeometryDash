package com.jade.main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.jade.components.BoxBounds;
import com.jade.components.Sprite;
import com.jade.components.TriangleBounds;
import com.jade.dataStructures.Genome;
import com.jade.dataStructures.NodeGene;
import com.jade.dataStructures.SpecieManager;
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

public class LevelScene extends Scene {
    public List<GameObject> gameObjects;
    public Renderer renderingEngine;

    private static LevelScene scene = null;
    private Music stereoMadness = null;

    public GameObject player;
    public Sprite playerLayerOneSpritesheet, playerLayerTwoSpritesheet, playerLayerThreeSpritesheet, spaceship;

    public boolean testingAi = true;
    public SpecieManager specieManager;
    public Genome currentGenome = null;
    public float distance = 0.0f;
    float evaluation = 0.0f;

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
        if (testingAi) {
            specieManager = new SpecieManager(81, 1);
            resetAi();
        }
        initBackgrounds();
        importLevel("levelOutput");
        Collections.sort(gameObjects);
        if (!testingAi)
            stereoMadness = new Music("assets/stereoMadness.wav");
    }

    public void resetAi() {
        if (currentGenome != null)
            currentGenome.setFitness(distance);
        Genome nextAi = specieManager.nextGenome();
        distance = 0;
        currentGenome = nextAi;
        player.transform = new Transform(new Vector2(-90.0f, 0));
    }

    public void addGameObject(GameObject gameObj) {
        gameObjects.add(gameObj);
        renderingEngine.submit(gameObj);
        gameObj.parent = null;
    }

    public List<Integer> getAllObjectsInView(GameObject me) {
        List<Integer> res = new ArrayList<>();
        Vector2 startPos = new Vector2((((float)(Math.floor(camera.transform.position.x / 42.0f)) + 9) * 42.0f) + 3,
                (((float)(Math.floor(camera.transform.position.y / 42.0f)) + 7) * 42.0f) + 3);
        float initialX = startPos.x;
        BoxBounds boundChecker = new BoxBounds(36, 36, 0, false, false, true);

        for (int i=0; i < 8; i++) {
            for (int j=0; j < 10; j++) {
                boolean added = false;
                if (startPos.y > Constants.CAMERA_OFFSET_Y + 500) {
                    added = true;
                    res.add(1);
                }

                if (!added) {
                    for (GameObject go : gameObjects) {
                        if (go.name.compareTo("Nonserialize") == 0 || go.name.compareTo("Player") == 0
                                || go.name.compareTo("Ground") == 0 || go.name.compareTo("Background") == 0) continue;
                        if (go.transform.position.x > startPos.x + 42 || go.transform.position.y > startPos.y + 42)
                            continue;

                        if (go.getComponent(BoxBounds.class) != null || go.getComponent(TriangleBounds.class) != null) {
                            if (go.getComponent(BoxBounds.class) != null) {
                                BoxBounds bounds = go.getComponent(BoxBounds.class);
                                if (go.transform.position.x + bounds.width + bounds.xBuffer < startPos.x) continue;
                                else if (go.transform.position.y + bounds.height + bounds.yBuffer < startPos.y)
                                    continue;
                                if (bounds.isColliding(boundChecker, startPos)) {
                                    if (go.getComponent(Genome.class) != null)
                                        res.add(3);
                                    else if (bounds.isDeathBox)
                                        res.add(2);
                                    else if (bounds.canCollide)
                                        res.add(1);
                                    else
                                        res.add(0);
                                    added = true;
                                    break;
                                }
                            } else {
                                TriangleBounds tBounds = go.getComponent(TriangleBounds.class);
                                if (go.transform.position.x + tBounds.base + tBounds.xBuffer < startPos.x) continue;
                                else if (go.transform.position.y + tBounds.height + tBounds.yBuffer < startPos.y)
                                    continue;
                                if (tBounds.isColliding(boundChecker, startPos)) {
                                    res.add(2);
                                    added = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!added) res.add(0);
                startPos.x += 42;
            }
            startPos.x = initialX;
            startPos.y += 42;
        }

        for (int i=0; i < res.size(); i++) {
            if (i % 10 == 0) System.out.print("\n");
            System.out.print("" + res.get(i) + " ");
        }
        System.out.println();
        return res;
    }

    private boolean intersection(BoxBounds b, Vector2 p1, Vector2 p2, double invX, double invY) {
        double tx1 = (b.parent.transform.position.x + b.xBuffer - p1.x) * invX;
        double tx2 = (b.parent.transform.position.x + b.xBuffer + b.width - p1.x) * invX;

        double tmin = Math.min(tx1, tx2);
        double tmax = Math.max(tx1, tx2);

        double ty1 = (b.parent.transform.position.y + b.yBuffer - p1.y) * invY;
        double ty2 = (b.parent.transform.position.y + b.yBuffer + b.height - p1.y) * invY;

        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        return tmax >= tmin;
    }

    @Override
    public void update(double dt) {
        if (Window.keyListener.isKeyPressed(KeyEvent.VK_SPACE)) return;

        distance += dt;
        player.update(dt);
        player.getComponent(BoxBounds.class).onGround = false;
        for (GameObject go : gameObjects) {
            if ((go.transform.position.x > camera.transform.position.x - Constants.TILE_WIDTH * 2 && go.transform.position.x < camera.transform.position.x + Constants.SCREEN_WIDTH) ||
                    go.getComponent(ParallaxBackground.class) != null) {
                go.update(dt);
            }
        }

        if (testingAi && currentGenome.isReady) {
            if (currentGenome.outputs.size() != 1) {
                System.out.println("UH OH: The output is not of size 1! It is size: " + currentGenome.outputs.size());
                System.exit(-1);
            }
            evaluation = currentGenome.outputs.get(0).getValue();
            if (currentGenome.outputs.get(0).getValue() > 0.85f) {
                player.getComponent(Player.class).jump(player.getComponent(BoxBounds.class));
            }

            List<Integer> objs = getAllObjectsInView(player);
            List<NodeGene>  inputs = currentGenome.getInputs();
            if (objs.size() != inputs.size() - 1) {
                System.out.println("UH OH: The inputs and objects in view are different lengths!");
                System.exit(-1);
            }
            for (int i=0; i < objs.size(); i++) {
                if (objs.get(i) == 0) {
                    inputs.get(i).setValue(0.0f);
                } else if (objs.get(i) == 1) {
                    inputs.get(i).setValue(0.3f);
                } else if (objs.get(i) == 2) {
                    inputs.get(i).setValue(0.6f);
                } else if (objs.get(i) == 3) {
                    inputs.get(i).setValue(0.0f);
                }
            }
            if (player.getComponent(Player.class).state == 0) {
                inputs.get(80).setValue(0.3f);
            } else {
                inputs.get(80).setValue(0.6f);
            }

            currentGenome.run();
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
        specieManager.draw(g2, (int)(distance * 100), evaluation);
        currentGenome.draw(g2);


//        Vector2 startPos = new Vector2((((float)(Math.floor(player.transform.position.x / 42.0f)) + 1) * 42.0f) + 3,
//                (((float)(Math.floor(player.transform.position.y / 42.0f)) - 4) * 42.0f) + 3);
//        float initialX = startPos.x;
//        BoxBounds boundChecker = new BoxBounds(36, 36, 0, false, false, true);
//
//        g2.setStroke(new BasicStroke(1.0f));
//        for (int i=0; i < 8; i++) {
//            for (int j=0; j < 10; j++) {
//                g2.setColor(Color.GREEN);
//                g2.drawRect((int)(startPos.x - camera.transform.position.x),
//                        (int)(startPos.y - camera.transform.position.y), (int)boundChecker.width, (int)boundChecker.height);
//                startPos.x += 42;
//            }
//            startPos.x = initialX;
//            startPos.y += 42;
//        }
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

        spaceship = new Sprite("assets/player/spaceship.png", true, 42, 42, 1, 2, 1);
        spaceship.loadSpritesheet();

        int playerImg = 20;
        player = new GameObject("Player", new Transform(new Vector2(-90.0f, 0)));
        BoxBounds bounds = new BoxBounds(Constants.PLR_WIDTH, Constants.PLR_HEIGHT, true, false);
        player.addComponent(bounds);
        player.setZIndex(1);
        player.addComponent(new Player(playerLayerOneSpritesheet.sprites.get(playerImg).subImg.image, playerLayerTwoSpritesheet.sprites.get(playerImg).subImg.image,
                playerLayerThreeSpritesheet.sprites.get(playerImg).subImg.image, Color.GREEN, Color.YELLOW, spaceship.sprites.get(0).subImg.image, true));

        if (testingAi)
            player.getComponent(Player.class).isAi = true;
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
