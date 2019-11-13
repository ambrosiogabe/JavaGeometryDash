package com.jade.ui;

import com.jade.components.BoxBounds;
import com.jade.components.Component;
import com.jade.components.SubSprite;
import com.jade.components.TriangleBounds;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;
import com.jade.main.Window;
import com.jade.scripts.CursorScript;
import com.jade.scripts.Portal;

import java.awt.*;
import java.awt.event.MouseEvent;

public class MenuItem extends Component {
    int x, y, width, height;
    SubSprite button, buttonHover;

    public boolean isSelected = false;
    public boolean isBox = true;

    private float buffer = 0.0f;

    public MenuItem(int x, int y, int width, int height, SubSprite button, SubSprite buttonHover) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.button = button;
        this.buttonHover = buttonHover;
        buffer = (width - Constants.TILE_WIDTH) / 2.0f;
    }

    @Override
    public void update(double dt) {
        if (Window.mouseListener.x > this.x && Window.mouseListener.x < this.x + this.width && Window.mouseListener.y > this.y && Window.mouseListener.y < this.y + this.height) {
            if (Window.mouseListener.mousePressed && Window.mouseListener.mouseButton == MouseEvent.BUTTON1) {
                LevelEditorScene.cursor.safeRemoveComponent(SubSprite.class);
                LevelEditorScene.cursor.safeRemoveComponent(BoxBounds.class);
                LevelEditorScene.cursor.safeRemoveComponent(TriangleBounds.class);
                SubSprite tmp = this.parent.getComponent(SubSprite.class);
                LevelEditorScene.cursor.addComponent(new SubSprite(tmp.spriteParent.pictureFile, tmp.imgX, tmp.imgY, tmp.width, tmp.height, tmp.row, tmp.column));

                if (isBox) {
                    BoxBounds bounds = parent.getComponent(BoxBounds.class).clone();
                    LevelEditorScene.cursor.addComponent(bounds);
                } else {
                    TriangleBounds tBounds = parent.getComponent(TriangleBounds.class).clone();
                    LevelEditorScene.cursor.addComponent(tBounds);
                }

                if (parent.getComponent(Portal.class) != null) LevelEditorScene.cursor.addComponent(parent.getComponent(Portal.class).clone());

                this.isSelected = true;
                LevelEditorScene.cursor.getComponent(CursorScript.class).isEditing = false;
            }
        }
    }

    @Override
    public MenuItem clone() {
        return new MenuItem(this.x, this.y, this.width, this.height, this.button, this.buttonHover);
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.drawImage(this.button.subImg.image, x, y, width, height, null);
        g2.drawImage(parent.getComponent(SubSprite.class).subImg.image, (int)(x + buffer), (int)(y + buffer), Constants.TILE_WIDTH, Constants.TILE_WIDTH, null);
        if (isSelected) {
            g2.drawImage(this.buttonHover.subImg.image, x, y, width, height, null);
        }
    }
}
