package com.jade.scripts;

import com.jade.components.BoxBounds;
import com.jade.components.TriangleBounds;
import com.jade.dataStructures.Vector2;
import com.jade.components.Component;
import com.jade.jade.GameObject;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;
import com.jade.main.Window;

import java.awt.event.KeyEvent;
import java.util.List;

public class EditorKeyShortcuts extends Component {

    private float debounceTime = 0.1f;
    private float debounceTimeLeft = 0.0f;

    @Override
    public void update(double dt) {
        debounceTimeLeft -= dt;

        if (parent.getComponent(CursorScript.class).isEditing && debounceTimeLeft <= 0.0f) {
            if (!Window.keyListener.isKeyPressed(KeyEvent.VK_SHIFT)) {
                if (Window.keyListener.isKeyPressed(KeyEvent.VK_UP)) {
                    moveGameObjects(new Vector2(0.0f, -Constants.TILE_WIDTH));
                } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_DOWN)) {
                    moveGameObjects(new Vector2(0.0f, Constants.TILE_WIDTH));
                } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
                    moveGameObjects(new Vector2(Constants.TILE_WIDTH, 0.0f));
                } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_LEFT)) {
                    moveGameObjects(new Vector2(-Constants.TILE_WIDTH, 0.0f));
                }
            } else {
                if (Window.keyListener.isKeyPressed(KeyEvent.VK_UP)) {
                    moveGameObjects(new Vector2(0.0f, -Constants.ONE_TENTH_TILE_WIDTH));
                } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_DOWN)) {
                    moveGameObjects(new Vector2(0.0f, Constants.ONE_TENTH_TILE_WIDTH));
                } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
                    moveGameObjects(new Vector2(Constants.ONE_TENTH_TILE_WIDTH, 0.0f));
                } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_LEFT)) {
                    moveGameObjects(new Vector2(-Constants.ONE_TENTH_TILE_WIDTH, 0.0f));
                }
            }

            if (Window.keyListener.isKeyPressed(KeyEvent.VK_DELETE)) {
                LevelEditorScene.getScene().deleteSelected();
            } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_CONTROL) && Window.keyListener.isKeyPressed(KeyEvent.VK_D)) {
                List<GameObject> dup = LevelEditorScene.getScene().duplicateSelected();
                for (GameObject go : dup) {
                    if (go.getComponent(BoxBounds.class) != null) go.getComponent(BoxBounds.class).isSelected = true;
                    else if (go.getComponent(TriangleBounds.class) != null) go.getComponent(TriangleBounds.class).isSelected = true;
                }
            } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_PAGE_UP)) {
                for (GameObject go : LevelEditorScene.getScene().getSelectedGameObjects()) {
                    LevelEditorScene.getScene().renderingEngine.remove(go);
                    go.setZIndex(go.zIndex + 1);
                    LevelEditorScene.getScene().renderingEngine.submit(go);
                }
            } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_PAGE_DOWN)) {
                for (GameObject go : LevelEditorScene.getScene().getSelectedGameObjects()) {
                    LevelEditorScene.getScene().renderingEngine.remove(go);
                    go.setZIndex(go.zIndex - 1);
                    LevelEditorScene.getScene().renderingEngine.submit(go);
                }
            }
            debounceTimeLeft = debounceTime;
        }
    }

    private void moveGameObjects(Vector2 distance) {
        for (GameObject go : LevelEditorScene.getScene().getSelectedGameObjects()) {
            go.transform.position = Vector2.add(go.transform.position, distance);
            float gridX = (float)(Math.floor(go.transform.position.x / Constants.TILE_WIDTH) * Constants.TILE_WIDTH);
            float gridY = (float)(Math.floor(go.transform.position.y / Constants.TILE_WIDTH) * Constants.TILE_WIDTH);

            if (go.transform.position.x < gridX + 3 && go.transform.position.x > gridX - 3) {
                go.transform.position.x = gridX;
            }

            if (go.transform.position.y < gridY + 3 && go.transform.position.y > gridY - 3) {
                go.transform.position.y = gridY;
            }
        }
    }

    @Override
    public EditorKeyShortcuts clone() {
        return new EditorKeyShortcuts();
    }
}
