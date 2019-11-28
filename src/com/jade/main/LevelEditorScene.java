package com.jade.main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.jade.components.BoxBounds;
import com.jade.components.Sprite;
import com.jade.components.TriangleBounds;
import com.jade.dataStructures.*;
import com.jade.jade.Camera;
import com.jade.jade.GameObject;
import com.jade.jade.Transform;
import com.jade.scripts.*;

import com.jade.file.Parser;
import com.jade.jade.Renderer;
import com.jade.ui.Grid;
import com.jade.ui.MainContainer;

public class LevelEditorScene extends Scene {
    private List<GameObject> gameObjects;
    public Renderer renderingEngine;

    private static LevelEditorScene scene;
    private GameObject player = null;
    private Grid grid = new Grid(Constants.GRID_WIDTH);

    private List<GameObject> selectedObjects = new ArrayList<>();
    private List<GameObject> objsToRemove = new ArrayList<>();
    private List<GameObject> objsToAdd = new ArrayList<>();

    public static GameObject cursor;
    private GameObject ground;

    SpecieManager specieManager;
    Genome currentGenome;
    float evaluation;
    float fitness;

    private float debounceTime = 0.01f;
    private float debounceLeft = 0.1f;

    public LevelEditorScene(String name) {
        super.Scene(name);
        gameObjects = new ArrayList<>();

        camera = new Camera(new Transform(new Vector2(0.0f, 0.0f)));
        renderingEngine = new Renderer(camera);
    }

    @Override
    public void init() {
        specieManager = new SpecieManager(3, 1);
        requestNext();

        initPlayer();
        initBackgrounds();
        LevelEditorScene.cursor = new GameObject("Cursor", new Transform(new Vector2()));
        cursor.addComponent(new CursorScript());
        cursor.addComponent(new EditorKeyShortcuts());

        Sprite menuContainerBg = new Sprite("assets/ui/menuContainerBackground.png", 1280, 180);
        GameObject mainContainer = new GameObject("Nonserialize", new Transform(new Vector2(0, Constants.MENU_CONTAINER_Y)));
        mainContainer.setUi(true);
        mainContainer.addComponent(menuContainerBg);
        mainContainer.addComponent(new MainContainer());
        mainContainer.setZIndex(100);
        addGameObject(mainContainer);

        GameObject cameraHelper = new GameObject("Nonserialize", new Transform(new Vector2(0.0f, 0.0f)));
        cameraHelper.addComponent(new CameraControls());
        addGameObject(cameraHelper);
        camera.transform.position = new Vector2(player.transform.position.x + Constants.CAMERA_OFFSET_X, player.transform.position.y + Constants.CAMERA_OFFSET_Y + 30);
        grid.start();
        importLevel("levelOutput");
    }

    public void requestNext() {
        fitness = 0.0f;
        Genome nextAi = specieManager.nextGenome();
        currentGenome = nextAi;
    }

    public void safeAddGameObject(GameObject go) {
        objsToAdd.add(go);
    }

    private void addGameObject(GameObject gameObj) {
        gameObjects.add(gameObj);
        renderingEngine.submit(gameObj);
        gameObj.parent = null;
    }

    public void safeRemoveGameObject(GameObject go) {
        objsToRemove.add(go);
    }

    private void removeGameObject(GameObject go) {
        gameObjects.remove(go);
        renderingEngine.remove(go);
    }

    public static LevelEditorScene getScene() {
        if (LevelEditorScene.scene == null) {
            LevelEditorScene.scene = new LevelEditorScene("Level Editor");
        }

        return LevelEditorScene.scene;
    }

    public static void deleteScene() {
        LevelEditorScene.cursor = null;
        LevelEditorScene.scene = null;
    }

    public float eval(float f1, float f2, boolean shouldBeTrue) {
        currentGenome.getInputs().get(0).setValue(f1);
        currentGenome.getInputs().get(1).setValue(f2);
        currentGenome.getInputs().get(2).setValue(1.0f);
        currentGenome.run();
        while (!currentGenome.isReady) {}
        List<NodeGene> output = currentGenome.outputs;
        if (shouldBeTrue) {
            return 1.0f - output.get(0).getValue();
        } else {
            return output.get(0).getValue();
        }
    }

    public void resetGenome() {
        for (NodeGene gene : currentGenome.getNodeGenes()) {
            gene.setValue(0.0f);
        }
    }

    public void fullEval() {
        fitness = 0.0f;
        float error = 0.0f;
        error += eval(0.0f, 0.0f, false);
        error += eval(0.0f, 1.0f, true);
        error += eval(1.0f, 0.0f, true);
        error += eval(1.0f, 1.0f, false);
        fitness = 4.0f - error;
        fitness *= fitness * fitness;
        currentGenome.setFitness(fitness);
        resetGenome();
    }

    @Override
    public void update(double dt) {
        debounceLeft -= dt;
        if (debounceLeft < 0.0f) {
            if (Window.keyListener.isKeyPressed(KeyEvent.VK_SPACE)) {
                fullEval();
                requestNext();
            } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_ENTER)) {
                fullEval();
            }
            debounceLeft = debounceTime;
        }

        cursor.update(dt);
        grid.update(dt);

        for (GameObject go : gameObjects) {
            if (go.transform.position.x > camera.transform.position.x - Constants.TILE_WIDTH * 2 && go.transform.position.x < camera.transform.position.x + Constants.SCREEN_WIDTH);
                go.update(dt);
        }

        ground.transform.position.x = camera.transform.position.x;

        if (objsToRemove.size() > 0) {
            for (GameObject obj : objsToRemove) {
                removeGameObject(obj);
            }
            objsToRemove.clear();
        }

        if (objsToAdd.size() > 0) {
            for (GameObject go : objsToAdd) {
                addGameObject(go);
            }
            objsToAdd.clear();
        }

        if (Window.keyListener.isKeyPressed(KeyEvent.VK_F1)) {
            export("levelOutput");
        } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_F2)) {
            export("levelOutput");
            Window.getWindow().changeScene(1);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        grid.draw(g2);
        renderingEngine.draw(g2);
        cursor.getComponent(SnapToGrid.class).draw(g2);
        cursor.getComponent(CursorScript.class).draw(g2);;
        specieManager.draw(g2, (int)(fitness * 10000.0f), evaluation);
        currentGenome.draw(g2);
    }

    public GameObject raycastMouseclick(int x, int y) {
        float newX = (Window.mouseListener.x + camera.transform.position.x + Window.mouseListener.dx);
        float newY = (Window.mouseListener.y + camera.transform.position.y + Window.mouseListener.dy);

        for (GameObject go : gameObjects) {
            if (go.getComponent(BoxBounds.class) != null && go.getComponent(BoxBounds.class).pointInSquare(newX, newY) && go.name.compareTo("Nonserialize") != 0) {
                return go;
            } else if (go.getComponent(TriangleBounds.class) != null && go.getComponent(TriangleBounds.class).pointInTriangle(newX, newY)) {
                return go;
            }
        }

        return null;
    }

    public List<GameObject> addBoxSelect(float x, float y, float width, float height) {
        float x0 = x + camera.transform.position.x;
        float y0 = y + camera.transform.position.y;

        List<GameObject> objs = new ArrayList<>();
        for (GameObject go : gameObjects) {
            if (go.getComponent(BoxBounds.class) != null && go.getComponent(BoxBounds.class).isContainedInRectangle(x0, y0, width ,height) && go.name.compareTo("Nonserialize") != 0) {
                objs.add(go);
                selectedObjects.add(go);
            } else if (go.getComponent(TriangleBounds.class) != null && go.getComponent(TriangleBounds.class).isContainedInRectangle(x0, y0, width, height)) {
                objs.add(go);
                selectedObjects.add(go);
            }
        }

        return objs;
    }

    public List<GameObject> duplicateSelected() {
        List<GameObject> objs = new ArrayList<>();
        for (GameObject go : selectedObjects) {
            GameObject clone = go.clone();
            objs.add(go);
            safeAddGameObject(clone);

            if (go.getComponent(BoxBounds.class) != null) go.getComponent(BoxBounds.class).isSelected = false;
            else if (go.getComponent(TriangleBounds.class) != null) go.getComponent(TriangleBounds.class).isSelected = false;
        }

        selectedObjects.clear();
        selectedObjects.addAll(objs);
        return objs;
    }

    public void deleteSelected() {
        for (GameObject go : selectedObjects) {
            removeGameObject(go);
        }
        selectedObjects.clear();
    }

    public void addOrRemoveSelectedGameObject(GameObject go) {
        TriangleBounds tBounds = go.getComponent(TriangleBounds.class);
        BoxBounds bounds = go.getComponent(BoxBounds.class);
        if (selectedObjects.contains(go)) {
            selectedObjects.remove(go);
            if (bounds != null) bounds.isSelected = false;
            else if (tBounds != null) tBounds.isSelected = false;
        }
        else {
            selectedObjects.add(go);
            if (bounds != null) bounds.isSelected = true;
            else if (tBounds != null) tBounds.isSelected = true;
        }
    }

    public void rotateSelected(double angle) {
        for (GameObject obj : this.selectedObjects) {
            BoxBounds bBounds =  obj.getComponent(BoxBounds.class);
            TriangleBounds tBounds= obj.getComponent(TriangleBounds.class);

            if (bBounds != null) {
                bBounds.angle += angle;
            } else if (tBounds != null) {
                tBounds.angle += angle;
            }
        }
    }

    public List<GameObject> getSelectedGameObjects() { return this.selectedObjects; }

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

    private void export(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream("levels/" + filename + ".zip");
            ZipOutputStream  zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(filename + ".json"));

            int i = 0;
            for (GameObject go : gameObjects) {
                if (go.name == "Nonserialize") {
                    i++;
                    continue;
                }
                zos.write(go.serialize(0).getBytes());
                if (i != gameObjects.size() - 1)
                    zos.write(",\n".getBytes());
                i++;
            }

            zos.closeEntry();
            zos.close();
            fos.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void initPlayer() {
        Sprite playerLayerOneSpritesheet = new Sprite("assets/player/layerOne.png", true, 42, 42, 13, 2, 13 * 5);
        playerLayerOneSpritesheet.loadSpritesheet();

        Sprite playerLayerTwoSpritesheet = new Sprite("assets/player/layerTwo.png", true, 42, 42, 13, 2, 13 * 5);
        playerLayerTwoSpritesheet.loadSpritesheet();

        Sprite playerLayerThreeSpritesheet = new Sprite("assets/player/layerThree.png", true, 42, 42, 13, 2, 13 * 5);
        playerLayerThreeSpritesheet.loadSpritesheet();

        Sprite spaceship = new Sprite("assets/player/spaceship.png", true, 42, 42, 1, 2, 1);
        spaceship.loadSpritesheet();

        int playerImg = 20;
        player = new GameObject("Nonserialize", new Transform(new Vector2(200.0f, 0)));
        BoxBounds bounds = new BoxBounds(Constants.PLR_WIDTH, Constants.PLR_HEIGHT, false, false);
        player.addComponent(bounds);
        player.addComponent(new Player(playerLayerOneSpritesheet.sprites.get(playerImg).subImg.image, playerLayerTwoSpritesheet.sprites.get(playerImg).subImg.image,
                playerLayerThreeSpritesheet.sprites.get(playerImg).subImg.image, Color.PINK, Color.ORANGE, spaceship.sprites.get(0).subImg.image, false));
        addGameObject(player);
    }

    private void initBackgrounds() {
        ground = new GameObject("Nonserialize", new Transform(new Vector2(0.0f, Constants.GROUND_HEIGHT)));
        ground.addComponent(new BoxBounds(Constants.SCREEN_WIDTH, 300.0f, false, false));
        Ground groundScript = new Ground(player);
        ground.addComponent(groundScript);
        addGameObject(ground);

        int numBackgrounds = 7;
        GameObject[] backgrounds = new GameObject[numBackgrounds];
        GameObject[] groundBgs = new GameObject[numBackgrounds];
        for (int i=0; i < numBackgrounds; i++) {
            int x = i * Constants.BG_WIDTH;
            int y = 0;
            GameObject go = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
            go.setUi(true);
            ParallaxBackground bg = new ParallaxBackground(Constants.BG_WIDTH, Constants.BG_HEIGHT, "" +
                    "assets/backgrounds/bg01.png", backgrounds, false, ground.getComponent(Ground.class), false);
            go.addComponent(bg);
            go.setZIndex(-100);
            backgrounds[i] = go;

            x = i * Constants.GROUND_BG_WIDTH;
            y = Constants.BG_HEIGHT;
            GameObject groundGo = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
            groundGo.setUi(true);
            ParallaxBackground groundBg = new ParallaxBackground(Constants.GROUND_BG_WIDTH, Constants.GROUND_BG_HEIGHT,
                    "assets/grounds/ground01.png", groundBgs, false, ground.getComponent(Ground.class), true);
            groundGo.addComponent(groundBg);
            groundBgs[i] = groundGo;
            groundGo.setZIndex(-99);

            addGameObject(go);
            addGameObject(groundGo);
        }
    }
}
