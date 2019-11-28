package com.jade.scripts;

import com.jade.dataStructures.Vector2;
import com.jade.components.BoxBounds;
import com.jade.components.Component;
import com.jade.jade.GameObject;
import com.jade.main.Constants;
import com.jade.main.LevelScene;
import com.jade.main.Window;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Player extends Component {
    private BufferedImage layerOne, layerTwo, outline, spaceship;
    public Color layerOneColor, layerTwoColor;

    private boolean isPlaying;
    public boolean isJumping = false;
    private int debounceFrames = 2;
    private int debounceLeft = 0;
    public boolean isAi = false;

    // 0 -- Regular
    // 1 -- Spaceship
    public int state = 0;

    public Player(BufferedImage layerOneImage, BufferedImage layerTwoImage, BufferedImage outline, Color layerOneColor, Color layerTwoColor, BufferedImage spaceship, boolean isPlaying) {
        layerOne = layerOneImage;
        layerTwo = layerTwoImage;
        this.outline = outline;
        this.layerOneColor = layerOneColor;
        this.layerTwoColor = layerTwoColor;
        this.isPlaying = isPlaying;
        this.spaceship = spaceship;
    }

    @Override
    public void start() {
        recolor();
    }

    public void recolor() {
        int threshold = 200;
        for(int y=0; y < layerOne.getWidth(); y++) {
            for (int x=0; x < layerOne.getHeight(); x++) {
                Color color = new Color(layerOne.getRGB(x, y));
                if (color.getRed() > threshold || color.getGreen() > threshold || color.getBlue() > threshold) {
                    layerOne.setRGB(x, y, layerOneColor.getRGB());
                }
            }
        }

        for(int y=0; y < layerTwo.getWidth(); y++) {
            for (int x=0; x < layerTwo.getHeight(); x++) {
                Color color = new Color(layerTwo.getRGB(x, y));
                if (color.getRed() > threshold || color.getGreen() > threshold || color.getBlue() > threshold) {
                    layerTwo.setRGB(x, y, layerTwoColor.getRGB());
                }
            }
        }
    }

    @Override
    public void update(double dt) {
        if (!isPlaying) return;
        debounceLeft++;
        if (debounceLeft >= debounceFrames) {
            debounceLeft = 0;
            isJumping = false;
        }

        parent.transform.position.x += Constants.SPEED * dt;

        if (state == 0) {
            updateRegularState(dt);
        } else if (state == 1) {
            updateSpaceshipState(dt);
        }

    }

    public void jump(BoxBounds bounds) {
        if (state == 1) {
            addFlyingForce();
            isJumping = true;
        } else if (state == 0 && bounds.onGround) {
            addJumpForce();
            bounds.onGround = false;
            isJumping = true;
        }
    }

    private void updateSpaceshipState(double dt) {
        if (Window.keyListener.isKeyPressed(KeyEvent.VK_SPACE) && !isAi)
            jump(parent.getComponent(BoxBounds.class));

        if (parent.getComponent(BoxBounds.class).onGround) {
            parent.getComponent(BoxBounds.class).velocity.y = 0.0f;
        }

        if (Math.abs(parent.getComponent(BoxBounds.class).velocity.y) > Constants.FLY_TERMINAL_VELOCITY) {
            parent.getComponent(BoxBounds.class).velocity.y = Math.signum(parent.getComponent(BoxBounds.class).velocity.y) * Constants.FLY_TERMINAL_VELOCITY;
        }

        Vector2 vel = parent.getComponent(BoxBounds.class).velocity;
        parent.getComponent(BoxBounds.class).angle = (vel.y / Constants.TERMINAL_VELOCITY) * 45;
    }

    private void updateRegularState(double dt) {
        if (Window.keyListener.isKeyPressed(KeyEvent.VK_SPACE) && !isAi)
            jump(parent.getComponent(BoxBounds.class));

        if (!parent.getComponent(BoxBounds.class).onGround) {
            parent.getComponent(BoxBounds.class).angle += 300.0f * dt;
        } else {
            parent.getComponent(BoxBounds.class).angle = (int)Math.floor((parent.getComponent(BoxBounds.class).angle + 5) / 180.0) * 180;
            if (parent.getComponent(BoxBounds.class).angle > 180 && parent.getComponent(BoxBounds.class).angle < 360) {
                parent.getComponent(BoxBounds.class).angle = 0;
            }
            isJumping = false;
        }
    }

    @Override
    public Player clone() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2) {
        BoxBounds bounds = parent.getComponent(BoxBounds.class);

        if (state == 0) {
            Graphics2D oldGraphics = (Graphics2D) g2.create();
            oldGraphics.translate(parent.transform.position.x, parent.transform.position.y);
            oldGraphics.rotate(Math.toRadians(bounds.angle), bounds.width / 2.0, bounds.height / 2.0);
            oldGraphics.drawImage(layerTwo, 0, 0, Constants.PLR_WIDTH, Constants.PLR_HEIGHT, null);
            oldGraphics.drawImage(layerOne, 0, 0, Constants.PLR_WIDTH, Constants.PLR_HEIGHT, null);
            oldGraphics.drawImage(outline, 0, 0, Constants.PLR_WIDTH, Constants.PLR_HEIGHT, null);
            oldGraphics.dispose();
        } else if (state == 1) {
            Graphics2D oldGraphics = (Graphics2D) g2.create();
            oldGraphics.translate(parent.transform.position.x, parent.transform.position.y);
            oldGraphics.rotate(Math.toRadians(bounds.angle), bounds.width / 2.0, bounds.height / 2.0);
            oldGraphics.drawImage(layerTwo, 12, 12, Constants.PLR_WIDTH / 3, Constants.PLR_HEIGHT / 3, null);
            oldGraphics.drawImage(layerOne, 12, 12, Constants.PLR_WIDTH / 3, Constants.PLR_HEIGHT / 3, null);
            oldGraphics.drawImage(outline, 12, 12, Constants.PLR_WIDTH / 3, Constants.PLR_HEIGHT / 3, null);
            oldGraphics.drawImage(spaceship, 0, 0, Constants.PLR_WIDTH, Constants.PLR_HEIGHT, null);
            oldGraphics.dispose();
        }

    }

    public void die() {
        if (!isAi)
            Window.getWindow().changeScene(1);
        else
            LevelScene.getScene().resetAi();
    }

    private void addJumpForce() {
        parent.getComponent(BoxBounds.class).velocity =  new Vector2(0, Constants.JUMP_FORCE);
    }

    private void addFlyingForce() {
        Vector2 vel = parent.getComponent(BoxBounds.class).velocity;
        parent.getComponent(BoxBounds.class).velocity = Vector2.add(vel, new Vector2(0, Constants.FLY_FORCE));
    }
}
