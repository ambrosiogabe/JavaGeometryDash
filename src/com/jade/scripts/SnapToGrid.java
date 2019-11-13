package com.jade.scripts;

import com.jade.components.BoxBounds;
import com.jade.components.SubSprite;
import com.jade.components.TriangleBounds;
import com.jade.dataStructures.Vector2;
import com.jade.jade.GameObject;
import com.jade.jade.Transform;
import com.jade.components.Component;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;
import com.jade.main.Window;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SnapToGrid extends Component {

    public int gridWidth, gridHeight;

    private double debounce = 0.0;
    private double debounceTime = 0.02;

    public SnapToGrid(int gridWidth, int gridHeight) {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
    }

    @Override
    public void update(double dt) {
        debounce -= dt;

        if (this.parent.getComponent(SubSprite.class) != null) {
            parent.getComponent(CursorScript.class).deselectAll();
            float x = (float)Math.floor((Window.mouseListener.x + LevelEditorScene.camera.transform.position.x + Window.mouseListener.dx) / gridWidth);
            float y = (float)Math.floor((Window.mouseListener.y + LevelEditorScene.camera.transform.position.y + Window.mouseListener.dy) / gridHeight);
            this.parent.transform.position.x = x * gridWidth - LevelEditorScene.camera.transform.position.x;
            this.parent.transform.position.y = y * gridHeight - LevelEditorScene.camera.transform.position.y;
            parent.getComponent(CursorScript.class).isPlacing = true;

            if (Window.mouseListener.mousePressed && debounce < 0 && y * gridHeight < Constants.GROUND_HEIGHT && Window.mouseListener.mouseButton == MouseEvent.BUTTON1) {
                debounce = debounceTime;
                if (LevelEditorScene.getScene().raycastMouseclick((int)x, (int)y) == null) {
                    GameObject obj = new GameObject("Generated", new Transform(new Vector2(x * gridWidth, y * gridHeight)));
                    SubSprite tmp = this.parent.getComponent(SubSprite.class);
                    obj.addComponent(new SubSprite(tmp.spriteParent.pictureFile, tmp.imgX, tmp.imgY, tmp.width, tmp.height, tmp.row, tmp.column));
                    if (parent.getComponent(BoxBounds.class) != null) obj.addComponent(parent.getComponent(BoxBounds.class).clone());
                    else if (parent.getComponent(TriangleBounds.class) != null) obj.addComponent(parent.getComponent(TriangleBounds.class).clone());
                    if (parent.getComponent(Portal.class) != null) obj.addComponent(parent.getComponent(Portal.class).clone());
                    LevelEditorScene.getScene().safeAddGameObject(obj);
                }
            }

            if (Window.keyListener.isKeyPressed(KeyEvent.VK_ESCAPE)) {
                switchToEdit();
            }
        } else {
            parent.getComponent(CursorScript.class).isPlacing = false;
        }
    }

    public void switchToEdit() {
        this.parent.safeRemoveComponent(SubSprite.class);
        this.parent.getComponent(CursorScript.class).isEditing = true;
    }

    @Override
    public SnapToGrid clone() {
        return new SnapToGrid(this.gridWidth, this.gridHeight);
    }

    @Override
    public void draw(Graphics2D g2) {
        SubSprite sprite = parent.getComponent(SubSprite.class);
        if (sprite != null && this.parent.transform.position.y + Constants.TILE_WIDTH < Constants.MENU_CONTAINER_Y) {
            float alpha = 0.5f; //draw half transparent
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2.setComposite(ac);
            g2.drawImage(sprite.subImg.image, (int) parent.transform.position.x, (int) parent.transform.position.y, sprite.width, sprite.height, null);
            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
            g2.setComposite(ac);
        }
    }
}
