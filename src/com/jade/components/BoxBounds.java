package com.jade.components;

import com.jade.dataStructures.Genome;
import com.jade.dataStructures.JString;
import com.jade.dataStructures.Vector2;
import com.jade.jade.GameObject;
import com.jade.file.Parser;
import com.jade.main.Constants;
import com.jade.main.LevelScene;
import com.jade.main.Window;
import com.jade.scripts.Player;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Rectangle2D;

public class BoxBounds extends Component {
    public double width, height;
    public double angle = 0.0;

    public boolean isSelected = false;

    public boolean onGround = true;
    public boolean isDeathBox;
    public boolean canCollide;
    public Vector2 velocity;
    public Vector2 acceleration;

    public boolean isPlaying;
    public float halfWidth;
    public float halfHeight;

    public float xBuffer, yBuffer;

    public BoxBounds(double width, double height, double angle, boolean isPlaying, boolean deathBox, boolean canCollide) {
        this.width = width;
        this.height = height;
        this.isPlaying = isPlaying;
        this.angle = angle;
        this.halfWidth = (float)(width / 2.0);
        this.halfHeight = (float)(height / 2.0);
        if (width <= Constants.TILE_WIDTH)
            this.xBuffer = (float)((Constants.TILE_WIDTH - width) / 2.0);
        else
            this.xBuffer = (float)((Constants.TILE_WIDTH * 2 - width) / 2.0);
        if (height <= Constants.TILE_WIDTH)
            this.yBuffer = (float)(Constants.TILE_WIDTH - height);
        else
            this.yBuffer = (float)(Constants.TILE_WIDTH * 2 - height);
        this.isDeathBox = deathBox;
        this.canCollide = canCollide;
    }

    public BoxBounds(double width, double height, boolean isPlaying, boolean deathBox) {
        this.width = width;
        this.height = height;
        this.isPlaying = isPlaying;
        this.angle = angle;
        if (width <= Constants.TILE_WIDTH)
            this.xBuffer = (float)((Constants.TILE_WIDTH - width) / 2.0);
        else
            this.xBuffer = (float)((Constants.TILE_WIDTH * 2 - width) / 2.0);
        if (height <= Constants.TILE_WIDTH)
            this.yBuffer = (float)(Constants.TILE_WIDTH - height);
        else
            this.yBuffer = (float)(Constants.TILE_WIDTH * 2 - height);
        this.isDeathBox = deathBox;
        this.canCollide = true;
    }

    public BoxBounds clone() {
        return new BoxBounds(width, height, angle, isPlaying, isDeathBox, canCollide);
    }

    @Override
    public void start() {
        this.velocity = new Vector2(0, 0);
        this.acceleration = new Vector2(0, 0);
        this.halfWidth = (float)this.width / 2.0f;
        this.halfHeight = (float)this.height / 2.0f;
    }

    @Override
    public void update(double dt) {
        if (!isPlaying) {
            if (!Window.isEditing && canCollide) {
                resolveCollision(LevelScene.getScene().player);
            }
            return;
        }

        parent.transform.position = Vector2.add(parent.transform.position, Vector2.scale(velocity, dt));
        velocity = Vector2.add(velocity, Vector2.scale(acceleration, dt));
        velocity = Vector2.add(velocity, Vector2.scale(new Vector2(0.0f, Constants.GRAVITY), dt));

        if (velocity.lengthSquared() > Constants.TERMINAL_VELOCITY * Constants.TERMINAL_VELOCITY) {
            velocity = Vector2.normalize(velocity);
            velocity = Vector2.scale(velocity, Constants.TERMINAL_VELOCITY);
        }
    }

    public boolean pointInSquare(float x, float y) {
        return x >= this.parent.transform.position.x + xBuffer && x <= this.parent.transform.position.x + this.width + xBuffer &&
                y >= this.parent.transform.position.y + yBuffer && y <= this.parent.transform.position.y + this.height + yBuffer;
    }

    public boolean isContainedInRectangle(float x, float y, float w, float h) {
        return this.parent.transform.position.x + xBuffer >= x && this.parent.transform.position.x + this.width + xBuffer <= x + w &&
                this.parent.transform.position.y + yBuffer >= y && this.parent.transform.position.y + this.height + yBuffer <= y + h;
    }

    private void resolveCollision(GameObject plr) {
        float dx = (plr.transform.position.x + plr.getComponent(BoxBounds.class).halfWidth) - (this.parent.transform.position.x + xBuffer + this.halfWidth);
        float dy = (plr.transform.position.y + plr.getComponent(BoxBounds.class).halfHeight) - (this.parent.transform.position.y + yBuffer + this.halfHeight);

        float combinedHalfWidths = plr.getComponent(BoxBounds.class).halfWidth + this.halfWidth;
        float combinedHalfHeights = plr.getComponent(BoxBounds.class).halfHeight + this.halfHeight;

        if (Math.abs(dx) < combinedHalfWidths) {
            if (Math.abs(dy) < combinedHalfHeights) {
                float overlapX = combinedHalfWidths - Math.abs(dx);
                float overlapY = combinedHalfHeights - Math.abs(dy);

                if (overlapX >= overlapY) {
                    if (dy > 0) {
                        // Collision on the bottom of box
                        if (!isDeathBox)
                            plr.transform.position.y = this.parent.transform.position.y + Constants.GRID_HEIGHT;
                        else {
                            plr.getComponent(Player.class).die();
                        }
                    } else {
                        // Collision on the top of box
                        if (!isDeathBox) {
                            if (plr.getComponent(Player.class).isJumping) {
                                plr.transform.position.y = this.parent.transform.position.y + this.yBuffer - Constants.PLR_HEIGHT;
                            }
                            plr.getComponent(BoxBounds.class).velocity.y = 0;
                            plr.getComponent(BoxBounds.class).onGround = true;
                        } else {
                            plr.getComponent(Player.class).die();
                        }
                    }
                } else if (Math.abs(dx) < 40) {
//                    if (plr.getComponent(Player.class).isAi) {
//                        plr.getComponent(Player.class).die();
//                        return;
//                    }

                    if (dx > 0) {
                        if (!isDeathBox && dy <= 0.3) {
                            if (plr.getComponent(Player.class).isJumping) return;
                            plr.transform.position.y = this.parent.transform.position.y + this.yBuffer - Constants.PLR_HEIGHT;
                            plr.getComponent(BoxBounds.class).velocity.y = 0;
                            plr.getComponent(BoxBounds.class).onGround = true;
                        } else if (!isDeathBox && dy >= 37.0) {
                            plr.transform.position.y = this.parent.transform.position.y + this.yBuffer + (float)height + 0.4f;
                            plr.getComponent(BoxBounds.class).velocity.y = 0;
                            plr.getComponent(BoxBounds.class).onGround = true;
                        } else {
                            // Collision on the left
                            plr.getComponent(Player.class).die();
                        }
                    } else if (dx < 0) {
                        if (!isDeathBox && dy <= 0.3) {
                            if (plr.getComponent(Player.class).isJumping) return;
                            plr.transform.position.y = this.parent.transform.position.y + this.yBuffer - Constants.PLR_HEIGHT;
                            plr.getComponent(BoxBounds.class).velocity.y = 0;
                            plr.getComponent(BoxBounds.class).onGround = true;
                        } else {
                            // Collision on the right
                            plr.getComponent(Player.class).die();
                        }
                    }
                }
            }
        }
    }

    public boolean isColliding(BoxBounds bounds, Vector2 pos) {
        // bounds.centerx - this.centerx
        float dx = (pos.x + bounds.halfWidth) - (this.parent.transform.position.x + xBuffer + this.halfWidth);
        // bounds.centery - this.centery
        float dy = (pos.y + bounds.halfHeight) - (this.parent.transform.position.y + yBuffer + this.halfHeight);

        float combinedHalfWidths = bounds.halfWidth + this.halfWidth;
        float combinedHalfHeights = bounds.halfHeight + this.halfHeight;

        if (Math.abs(dx) < combinedHalfWidths) {
            return Math.abs(dy) < combinedHalfHeights;
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.GREEN);
        if (isSelected) {
            g2.setStroke(new BasicStroke(3));
            Graphics2D oldGraphics = (Graphics2D)g2.create();
            oldGraphics.translate(parent.transform.position.x + xBuffer, parent.transform.position.y + yBuffer);
            oldGraphics.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
            oldGraphics.draw(new Rectangle2D.Double(0, 0, width, height));
        }
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("BoxBounds", tabSize));
        builder.append(addDoubleProperty("width", width, tabSize + 1, true, true));
        builder.append(addDoubleProperty("height", height, tabSize + 1, true, true));
        builder.append(addDoubleProperty("angle", angle, tabSize + 1, true, true));
        builder.append(addBooleanProperty("isPlaying", isPlaying, tabSize + 1, true, true));
        builder.append(addBooleanProperty("canCollide", canCollide, tabSize + 1, true, true));
        builder.append(addBooleanProperty("isDeathBox", isDeathBox, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static BoxBounds deserialize() {
        Parser.consumeBeginObjectProperty();

        double width = Parser.consumeDoubleProperty("width");
        Parser.consume(',');
        double height = Parser.consumeDoubleProperty("height");
        Parser.consume(',');
        double angle = Parser.consumeDoubleProperty("angle");
        Parser.consume(',');
        boolean isPlaying = Parser.consumeBooleanProperty("isPlaying");
        Parser.consume(',');
        boolean canCollide = Parser.consumeBooleanProperty("canCollide");
        Parser.consume(',');
        boolean deathBox = Parser.consumeBooleanProperty("isDeathBox");
        Parser.consume('}');

        return new BoxBounds(width, height, angle, isPlaying, deathBox, canCollide);
    }
}
