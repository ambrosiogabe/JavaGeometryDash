package com.jade.ui;

import com.jade.components.Component;
import com.jade.components.SubSprite;
import com.jade.main.LevelEditorScene;
import com.jade.main.Window;
import com.jade.scripts.SnapToGrid;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.event.MouseEvent;

public class TabItem extends Component {

    public int width, height;
    public boolean isSelected = false;

    public TabItem(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setSelected(boolean val) {
        isSelected = val;
    }

    @Override
    public void update(double dt) {
        if (Window.mouseListener.mousePressed && Window.mouseListener.mouseButton == MouseEvent.BUTTON1) {
            if (Window.mouseListener.x >= parent.transform.position.x && Window.mouseListener.x <= parent.transform.position.x + width &&
            Window.mouseListener.y >= parent.transform.position.y && Window.mouseListener.y <= parent.transform.position.y + height) {
                isSelected = true;
                LevelEditorScene.cursor.getComponent(SnapToGrid.class).switchToEdit();
            }
        }
    }

    @Override
    public TabItem clone() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2) {
        float alpha = 0.5f;
        if (isSelected) alpha = 1.0f;
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);
        g2.drawImage(parent.getComponent(SubSprite.class).subImg.image, (int)parent.transform.position.x, (int)parent.transform.position.y, width, height, null);
        ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
        g2.setComposite(ac);
    }
}
