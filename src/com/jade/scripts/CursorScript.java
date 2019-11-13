package com.jade.scripts;

import com.jade.components.BoxBounds;
import com.jade.components.Component;
import com.jade.components.TriangleBounds;
import com.jade.jade.GameObject;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;
import com.jade.main.Window;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class CursorScript extends Component {
    public boolean isEditing = true;
    public boolean isPlacing = false;

    private float debounceTime = 0.1f;
    private float debounceTimeLeft = 0.0f;
    private boolean wasDragged = false;
    private float dragX, dragY, dragWidth, dragHeight;

    @Override
    public void update(double dt) {
        if (debounceTimeLeft > 0.0f) debounceTimeLeft -= dt;
        if (isEditing) {
            if (Window.mouseListener.mousePressed && Window.mouseListener.mouseButton == MouseEvent.BUTTON1 && debounceTimeLeft <= 0.0f) {
                // MOUSE PRESS LEFT BUTTON
                if (Window.mouseListener.y > Constants.BUTTON_OFFSET_Y) return;

                debounceTimeLeft = debounceTime;
                GameObject selected = LevelEditorScene.getScene().raycastMouseclick((int)Window.mouseListener.x, (int)Window.mouseListener.y);
                if (selected != null && Window.keyListener.isKeyPressed(KeyEvent.VK_SHIFT)) {
                    LevelEditorScene.getScene().addOrRemoveSelectedGameObject(selected);
                } else if (selected != null) {
                    deselectAll();
                    selectObj(selected, true);
                    LevelEditorScene.getScene().addOrRemoveSelectedGameObject(selected);
                }  else {
                    deselectAll();
                }
            } else if (!Window.mouseListener.mousePressed && wasDragged) {
                // DRAG EXIT LEFT
                wasDragged = false;
                deselectAll();
                List<GameObject> objs = LevelEditorScene.getScene().addBoxSelect(dragX, dragY, dragWidth, dragHeight);
                for (GameObject go : objs) {
                    selectObj(go, true);
                }
            } else if (Window.keyListener.isKeyPressed(KeyEvent.VK_ESCAPE)) {
                deselectAll();
            }
        }
    }

    @Override
    public CursorScript clone() {
        return new CursorScript();
    }

    @Override
    public void draw(Graphics2D g2) {
        if (Window.mouseListener.mouseDragged && Window.mouseListener.mouseButton == MouseEvent.BUTTON1 && !isPlacing) {
            wasDragged = true;
            g2.setColor(new Color(1, 1, 1, 0.3f));
            dragX = Window.mouseListener.x;
            dragY = Window.mouseListener.y;
            dragWidth = Window.mouseListener.dx;
            dragHeight = Window.mouseListener.dy;
            if (dragWidth < 0) {
                dragWidth *= -1;
                dragX -= dragWidth;
            }
            if (dragHeight < 0) {
                dragHeight *= -1;
                dragY -= dragHeight;
            }
            g2.fillRect((int)dragX, (int)dragY, (int)dragWidth, (int)dragHeight);
        }
    }

    public void deselectAll() {
        for (GameObject go : LevelEditorScene.getScene().getSelectedGameObjects()) {
            selectObj(go, false);
        }
        LevelEditorScene.getScene().getSelectedGameObjects().clear();
    }

    private void selectObj(GameObject obj, boolean val) {
        BoxBounds bounds = obj.getComponent(BoxBounds.class);
        TriangleBounds tBounds;
        if (bounds != null) bounds.isSelected = val;
        else if ((tBounds = obj.getComponent(TriangleBounds.class)) != null) tBounds.isSelected = val;
    }
}
