package com.jade.ui;

import com.jade.components.Component;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;

import java.awt.*;
import java.awt.geom.Line2D;

public class Grid extends Component {
    private int gridSize;
    private int numXLines;
    private int numYLines;

    private float firstX = 0.0f;
    private float firstY = 0.0f;

    private float bottom = 0.0f;

    public Grid(int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public void start() {
        this.numXLines = Constants.SCREEN_HEIGHT / this.gridSize;
        this.numYLines = Constants.SCREEN_WIDTH / this.gridSize;
    }

    @Override
    public void update(double dt) {
        this.firstX = ((float)Math.floor(LevelEditorScene.camera.transform.position.x / gridSize) * gridSize) - LevelEditorScene.camera.transform.position.x;
        this.firstY = ((float)Math.floor(LevelEditorScene.camera.transform.position.y / gridSize) * gridSize) - LevelEditorScene.camera.transform.position.y;
        this.bottom = Math.min(Constants.GROUND_HEIGHT - LevelEditorScene.camera.transform.position.y, Constants.SCREEN_HEIGHT);
    }

    @Override
    public Grid clone() {
        return new Grid(gridSize);
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(0.2f, 0.2f, 0.2f, 0.5f));

        float x = this.firstX;
        float y = this.firstY;

        for (int column=0; column <= numYLines; column++) {
            g2.draw(new Line2D.Float(x, 0, x, bottom));
            x += gridSize;
        }

        for (int row=0; row <= numXLines; row++) {
            if (LevelEditorScene.camera.transform.position.y + y < Constants.GROUND_HEIGHT) {
                g2.draw(new Line2D.Float(0, y, Constants.SCREEN_WIDTH, y));
                y += gridSize;
            }
        }
    }
}
