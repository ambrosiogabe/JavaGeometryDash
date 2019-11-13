package com.jade.scripts;

import com.jade.components.Component;
import com.jade.jade.GameObject;
import com.jade.main.Constants;
import com.jade.main.LevelEditorScene;
import com.jade.main.Scene;
import com.jade.main.Window;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ParallaxBackground extends Component {
    public int width, height;
    public BufferedImage image;
    public GameObject[] backgrounds;
    public int timeStep = 0;
    private float speed = 80.0f;

    private boolean isPlaying;

    private boolean followGround = false;
    private Ground ground;

    public ParallaxBackground(int width, int height, String file, GameObject[] backgrounds, boolean isPlaying, Ground ground, boolean followGround) {
        this.width = width;
        this.height = height;
        this.backgrounds = backgrounds;
        this.isPlaying = isPlaying;
        if (followGround) this.speed = Constants.SPEED - 35;

        try {
            this.image = ImageIO.read(new File(file));
        } catch(IOException e) {
            e.printStackTrace();
        }

        this.followGround = followGround;
        this.ground = ground;
    }

    @Override
    public void update(double dt) {
        if (isPlaying) {
            this.timeStep++;

            this.parent.transform.position.x -= dt * speed;
            this.parent.transform.position.x = (float) Math.floor(this.parent.transform.position.x);
            if (this.parent.transform.position.x < -width) {
                float maxX = 0;
                int otherTimeStep = 0;
                for (GameObject go : backgrounds) {
                    if (go.transform.position.x > maxX) {
                        maxX = go.transform.position.x;
                        otherTimeStep = go.getComponent(ParallaxBackground.class).timeStep;
                    }
                }

                if (otherTimeStep == this.timeStep) {
                    this.parent.transform.position.x = maxX + width;
                } else {
                    this.parent.transform.position.x = (float) Math.floor((maxX + width) - (dt * speed));
                }
            }
        }

        if (followGround) {
            this.parent.transform.position.y = ground.parent.transform.position.y;
        }
    }

    @Override
    public ParallaxBackground clone() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (followGround) {
            g2.drawImage(image, (int)this.parent.transform.position.x, (int)(this.parent.transform.position.y - LevelEditorScene.camera.transform.position.y),
                    width, height, null);
        } else {
            int height = Math.min((int)(ground.parent.transform.position.y - Scene.camera.transform.position.y), Constants.SCREEN_HEIGHT);
            g2.drawImage(image, (int) this.parent.transform.position.x, (int) this.parent.transform.position.y, width, Constants.SCREEN_HEIGHT, null);
            g2.setColor(Window.getWindow().groundColor);
            g2.fillRect((int)this.parent.transform.position.x, height, width, Constants.SCREEN_HEIGHT - height);
        }
    }
}
