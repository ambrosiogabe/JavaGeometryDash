package com.jade.scripts;

import com.jade.components.SubSprite;
import com.jade.main.LevelEditorScene;

public class RotateButton extends Button {
    public boolean rotateRight;

    public RotateButton(int width, int height, SubSprite img, SubSprite imgSelected, String text, boolean rotateRight) {
        super(width, height, img, imgSelected, text);
        this.rotateRight = rotateRight;
    }

    @Override
    public void buttonPressed() {
        if (rotateRight) {
            LevelEditorScene.getScene().rotateSelected(90.0);
        } else {
            LevelEditorScene.getScene().rotateSelected(-90.0);
        }
    }

    @Override
    public RotateButton clone() {
        return new RotateButton(width, height, img, imgSelected, text, rotateRight);
    }
}
