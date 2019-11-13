package com.jade.ui;

import com.jade.components.*;
import com.jade.dataStructures.Vector2;
import com.jade.jade.GameObject;
import com.jade.jade.Transform;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;
import com.jade.scripts.CursorScript;
import com.jade.scripts.Portal;
import com.jade.scripts.RotateButton;
import com.jade.scripts.SnapToGrid;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MainContainer extends Component {
    public List<GameObject> tabs;
    public Map<GameObject, List<GameObject>> tabObjects;

    private MenuItem currentSelection = null;
    private GameObject currentTab = null;
    private boolean deselectAll = false;

    public MainContainer() {
        this.tabs = new ArrayList<>();
        this.tabObjects = new HashMap<>();
    }

    @Override
    public void start() {
        Sprite buttonSprites = new Sprite("assets/ui/buttonSprites.png", true, 60, 60, 2, 2, 2);
        buttonSprites.loadSpritesheet();

        Sprite bigButtonSprites = new Sprite("assets/ui/bigButtons.png", true, 90, 90, 4, 2, 4);
        bigButtonSprites.loadSpritesheet();

        Sprite tabSprites = new Sprite("assets/ui/tabs.png", true, Constants.TAB_WIDTH, Constants.TAB_HEIGHT, 6, 2, 6);
        tabSprites.loadSpritesheet();

        LevelEditorScene.cursor.addComponent(new SnapToGrid(42, 42));
        for (int i=0; i < tabSprites.sprites.size(); i++) {
            SubSprite currentTab = tabSprites.sprites.get(i);
            int x = Constants.TAB_OFFSET_X + (currentTab.column * Constants.TAB_WIDTH) + (currentTab.column * Constants.TAB_HORIZONTAL_SPACING);
            int y = Constants.TAB_OFFSET_Y;
            GameObject obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
            TabItem item = new TabItem(Constants.TAB_WIDTH, Constants.TAB_HEIGHT);
            obj.addComponent(currentTab);
            obj.addComponent(item);

            this.tabs.add(obj);
            this.tabObjects.put(obj, new ArrayList<>());
            LevelEditorScene.getScene().safeAddGameObject(obj);
        }
        this.currentTab = this.tabs.get(0);
        currentTab.getComponent(TabItem.class).setSelected(true);

        addTabObjects(buttonSprites);
        addEditingButtons(bigButtonSprites);
    }

    @Override
    public void update(double dt) {
        if (!LevelEditorScene.cursor.getComponent(CursorScript.class).isEditing) {
            for (GameObject go : this.tabObjects.get(currentTab)) {
                MenuItem item = go.getComponent(MenuItem.class);
                if (currentSelection != null && item != currentSelection) {
                    if (item.isSelected) {
                        currentSelection.isSelected = false;
                        currentSelection = item;
                        break;
                    }
                } else if (currentSelection == null && item.isSelected) {
                    currentSelection = item;
                    break;
                }
            }
            deselectAll = true;
        } else if (deselectAll) {
            for (GameObject go : this.tabObjects.get(currentTab)) {
                go.getComponent(MenuItem.class).isSelected = false;
            }
            deselectAll = false;
        }

        for (GameObject go : this.tabs) {
            TabItem item = go.getComponent(TabItem.class);
            if (item.isSelected && go != currentTab) {
                currentTab.getComponent(TabItem.class).setSelected(false);
                removeAllGameObjects(tabObjects.get(currentTab));
                currentTab = go;
                addGameObjects(tabObjects.get(currentTab));
                break;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        for (GameObject go : this.tabs) {
            go.getComponent(TabItem.class).draw(g2);
        }

        for (GameObject go : this.tabObjects.get(currentTab)) {
            go.getComponent(MenuItem.class).draw(g2);
        }
    }

    @Override
    public MainContainer clone() {
        return new MainContainer();
    }

    private void removeAllGameObjects(List<GameObject> objs) {
        for (GameObject obj : objs) {
            LevelEditorScene.getScene().safeRemoveGameObject(obj);
        }
    }

    private void addGameObjects(List<GameObject> objs) {
        for (GameObject obj : objs) {
            LevelEditorScene.getScene().safeAddGameObject(obj);
        }
    }

    private void addEditingButtons(Sprite buttonSprites) {
        List<GameObject> buttons = new ArrayList<>();

        GameObject rotateRight = new GameObject("Nonserialize", new Transform(new Vector2(830, Constants.BUTTON_OFFSET_Y)));
        RotateButton rightRotate = new RotateButton(Constants.BIG_BUTTON_WIDTH, Constants.BIG_BUTTON_WIDTH, buttonSprites.sprites.get(2),
                buttonSprites.sprites.get(3), "", true);
        rotateRight.addComponent(rightRotate);
        rotateRight.setUi(true);
        rotateRight.setZIndex(100);
        buttons.add(rotateRight);

        GameObject rotateLeft = new GameObject("Nonserialize", new Transform(new Vector2(830 + Constants.BIG_BUTTON_WIDTH + 10, Constants.BUTTON_OFFSET_Y)));
        RotateButton leftRotate = new RotateButton(Constants.BIG_BUTTON_WIDTH, Constants.BIG_BUTTON_WIDTH, buttonSprites.sprites.get(0),
                buttonSprites.sprites.get(1), "", false);
        rotateLeft.addComponent(leftRotate);
        rotateLeft.setUi(true);
        rotateLeft.setZIndex(100);
        buttons.add(rotateLeft);

        addGameObjects(buttons);
    }

    private void addTabObjects(Sprite buttonSprites) {
        Sprite spritesheet = new Sprite("assets/spritesheet.png", true, 42, 42, 6, 2, 12);
        spritesheet.loadSpritesheet();

        Sprite spikeSprites = new Sprite("assets/spikes.png", true, 42, 42, 6, 2, 4);
        spikeSprites.loadSpritesheet();

        Sprite bigSprites = new Sprite("assets/bigSprites.png", true, 84, 84, 2, 2, 2);
        bigSprites.loadSpritesheet();

        Sprite smallBlocks = new Sprite("assets/smallBlocks.png", true, 42, 42, 6, 2, 1);
        smallBlocks.loadSpritesheet();

        Sprite portalSprites = new Sprite("assets/portal.png", true, 44, 85, 2, 2, 2);
        portalSprites.loadSpritesheet();

        // Add tab objects
        for (int i=0; i < spritesheet.sprites.size(); i++) {
            int x = Constants.BUTTON_OFFSET_X + (spritesheet.sprites.get(i).column * Constants.BUTTON_WIDTH) + (spritesheet.sprites.get(i).column * Constants.BUTTON_HORIZONTAL_SPACING);
            int y = Constants.BUTTON_OFFSET_Y + (spritesheet.sprites.get(i).row * Constants.BUTTON_WIDTH) + (spritesheet.sprites.get(i).row * 5);

            // Add first tab
            GameObject obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
            MenuItem menuItem = new MenuItem(x, y, Constants.BUTTON_WIDTH, Constants.BUTTON_WIDTH, buttonSprites.sprites.get(0), buttonSprites.sprites.get(1));
            obj.addComponent(spritesheet.sprites.get(i));
            obj.addComponent(menuItem);
            obj.addComponent(new BoxBounds(Constants.TILE_WIDTH, Constants.TILE_WIDTH, false, false));
            this.tabObjects.get(tabs.get(0)).add(obj);

            // Add second tab
            if (i < smallBlocks.sprites.size()) {
                obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
                menuItem = new MenuItem(x, y, Constants.BUTTON_WIDTH, Constants.BUTTON_WIDTH, buttonSprites.sprites.get(0), buttonSprites.sprites.get(1));
                obj.addComponent(smallBlocks.sprites.get(i));
                obj.addComponent(menuItem);

                if (i == 0) {
                    obj.addComponent(new BoxBounds(Constants.TILE_WIDTH, 16, 0, false, false, true));
                    this.tabObjects.get(tabs.get(1)).add(obj);
                }
            }

            // Add fourth tab
            if (i < spikeSprites.sprites.size()) {
                obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
                menuItem = new MenuItem(x, y, Constants.BUTTON_WIDTH, Constants.BUTTON_WIDTH, buttonSprites.sprites.get(0), buttonSprites.sprites.get(1));
                menuItem.isBox = false;
                obj.addComponent(spikeSprites.sprites.get(i));
                obj.addComponent(menuItem);

                double base = spikeSprites.sprites.get(i).width;
                double height = spikeSprites.sprites.get(i).height;
                if (i == 1) height = 13;
                else if (i == 2) {
                    base = 24;
                    height = 24;
                }
                TriangleBounds tBounds = new TriangleBounds(base, height, false);

                if (i < 3) {
                    obj.addComponent(tBounds);
                } else {
                    BoxBounds bounds = new BoxBounds(Constants.TILE_WIDTH, 24, false, true);
                    obj.addComponent(bounds);
                    menuItem.isBox = true;
                }
                this.tabObjects.get(tabs.get(3)).add(obj);
            }

            // Add fifth tab
            if ( i == 0) {
                obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
                menuItem = new MenuItem(x, y, Constants.BUTTON_WIDTH, Constants.BUTTON_WIDTH, buttonSprites.sprites.get(0), buttonSprites.sprites.get(1));
                obj.addComponent(bigSprites.sprites.get(1));
                obj.addComponent(menuItem);
                obj.addComponent(new BoxBounds(Constants.TILE_WIDTH * 2, 56, 0.0, false, false, false));
                this.tabObjects.get(tabs.get(4)).add(obj);
            }

            // Add sixth tab
            if (i == 0) {
                obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
                menuItem = new MenuItem(x, y, Constants.BUTTON_WIDTH, Constants.BUTTON_WIDTH, buttonSprites.sprites.get(0), buttonSprites.sprites.get(1));
                obj.addComponent(portalSprites.sprites.get(0));
                obj.addComponent(menuItem);;
                obj.addComponent(new BoxBounds(44, 85, 0.0, false, false, false));
                obj.addComponent(new Portal(44, 85, 1));
                this.tabObjects.get(tabs.get(5)).add(obj);
            } else if (i == 1) {
                obj = new GameObject("Nonserialize", new Transform(new Vector2(x, y)));
                menuItem = new MenuItem(x, y, Constants.BUTTON_WIDTH, Constants.BUTTON_WIDTH, buttonSprites.sprites.get(0), buttonSprites.sprites.get(1));
                obj.addComponent(portalSprites.sprites.get(1));
                obj.addComponent(menuItem);
                obj.addComponent(new BoxBounds(44, 85, 0.0, false, false, false));
                obj.addComponent(new Portal(44, 85, 0));
                this.tabObjects.get(tabs.get(5)).add(obj);
            }
        }
        addGameObjects(this.tabObjects.get(currentTab));
    }
}
