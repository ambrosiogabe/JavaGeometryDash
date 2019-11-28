package com.jade.components;

import com.jade.dataStructures.JString;
import com.jade.dataStructures.Vector2;
import com.jade.file.Parser;
import com.jade.jade.GameObject;
import com.jade.main.Constants;
import com.jade.main.LevelScene;
import com.jade.main.Window;
import com.jade.scripts.Player;

import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;

public class TriangleBounds extends Component {
    public double base, height, halfWidth, yBuffer, xBuffer;
    private double enclosingRadius, halfHeight;
    public boolean isPlaying;
    public float angle = 0.0f;

    private float x1, x2, x3, y1, y2, y3;

    // For polygon intersection tests
    private final int INSIDE = 0;
    private final int LEFT = 1;
    private final int RIGHT = 2;
    private final int BOTTOM = 4;
    private final int TOP = 8;

    public boolean isSelected = false;

    public TriangleBounds(double base, double height, float angle, boolean isPlaying) {
        this.base = base;
        this.height = height;
        this.isPlaying = isPlaying;
        this.angle = angle;
        this.halfWidth = base / 2.0;
        this.yBuffer = Constants.GRID_HEIGHT - height;
        this.xBuffer = (Constants.GRID_WIDTH - base) / 2.0;
        this.halfHeight = height / 2.0;
        this.enclosingRadius = Math.max(halfHeight, halfWidth);
    }

    public TriangleBounds(double base, double height, boolean isPlaying) {
        this.base = base;
        this.height = height;
        this.isPlaying = isPlaying;
        this.halfWidth = base / 2.0;
        this.yBuffer = Constants.GRID_HEIGHT - height;
        this.xBuffer = (Constants.GRID_WIDTH - base) / 2.0;
        this.halfHeight = height / 2.0;
        this.enclosingRadius = Math.max(halfHeight, halfWidth);
    }

    public TriangleBounds clone() {
        return new TriangleBounds(base, height, isPlaying);
    }

    @Override
    public void start() {
        recalculateTransform();
    }

    @Override
    public void update(double dt) {
        recalculateTransform();
        if (Window.isEditing) return;

        if (broadPhase(LevelScene.getScene().player)) {
            // Possible Collision!
            if (narrowPhase(LevelScene.getScene().player)) {
                // There is definitely a collision!
                LevelScene.getScene().player.getComponent(Player.class).die();
            }
        }
    }

    private boolean broadPhase(GameObject plr) {
        double centerX = x1;
        double centerY = y1 + halfHeight;
        if (angle % 360 == 0) {
            // Don't change anything.
        } else if (angle % 270 == 0) {
            centerX = x1 + halfWidth;
            centerY = y1;
        } else if (angle % 180 == 0 && angle % 360 != 0) {
            centerY = y1 - halfHeight;
        } else if (angle % 90 == 0 && angle % 360 != 0) {
            centerX = x1 - halfWidth;
            centerY = y1;
        }

        double plrCenterX = plr.transform.position.x + plr.getComponent(BoxBounds.class).halfWidth;
        double plrCenterY = plr.transform.position.y + plr.getComponent(BoxBounds.class).halfHeight;

        return ((plrCenterX - centerX) * (plrCenterX - centerX)) + ((plrCenterY - centerY) * (plrCenterY - centerY)) <=
                (this.enclosingRadius + plr.getComponent(BoxBounds.class).halfWidth) * (this.enclosingRadius + plr.getComponent(BoxBounds.class).halfWidth);
    }

    private boolean narrowPhase(GameObject plr) {
        Vector2 p1 = new Vector2(x1, y1);
        Vector2 p2 = new Vector2(x2, y2);
        Vector2 p3 = new Vector2(x3, y3);

        BoxBounds bounds = plr.getComponent(BoxBounds.class);
        Vector2 origin = new Vector2(bounds.parent.transform.position.x + bounds.halfWidth, bounds.parent.transform.position.y + bounds.halfHeight);

        return (playerIntersectingLine(p1, p2, origin, -bounds.angle, 0, bounds, plr.transform.position)) ||
                (playerIntersectingLine(p1, p3, origin, -bounds.angle, 0, bounds, plr.transform.position)) ||
                (playerIntersectingLine(p2, p3, origin, -bounds.angle, 0, bounds, plr.transform.position));
    }

    public boolean isColliding(BoxBounds bounds, Vector2 pos) {
        Vector2 p1 = new Vector2(x1, y1);
        Vector2 p2 = new Vector2(x2, y2);
        Vector2 p3 = new Vector2(x3, y3);

        Vector2 origin = new Vector2(pos.x + bounds.halfWidth, pos.y + bounds.halfHeight);

        return (playerIntersectingLine(p1, p2, origin, -bounds.angle, 0, bounds, pos)) ||
                (playerIntersectingLine(p1, p3, origin, -bounds.angle, 0, bounds, pos)) ||
                (playerIntersectingLine(p2, p3, origin, -bounds.angle, 0, bounds, pos));
    }


    private boolean playerIntersectingLine(Vector2 oldP1, Vector2 oldP2, Vector2 origin, double angle, int depth, BoxBounds bounds, Vector2 playerPos) {
        if (depth > 5) return true;
        Vector2 p1 = rotateAbout(Math.toRadians(angle), oldP1, origin);
        Vector2 p2 = rotateAbout(Math.toRadians(angle), oldP2, origin);

        int code1 = computeRegionCode(p1, bounds, playerPos);
        int code2 = computeRegionCode(p2, bounds, playerPos);

        if (code1 == 0 && code2 == 0) {
            // Line is completely inside
            return true;
        } else if ((code1 & code2) != 0) {
            // Line is completely outside
            return false;
        } else {
            int ymax = (int)(playerPos.y + bounds.height);
            int ymin = (int)(playerPos.y);
            int xmax = (int)(playerPos.x + bounds.width);
            int xmin = (int)(playerPos.x);

            int codeForPointOutside;
            Vector2 newVec = new Vector2();
            if (code1 != 0)
                codeForPointOutside = code1;
            else
                codeForPointOutside = code2;
            if ((codeForPointOutside & TOP) == TOP) {
                newVec.x = p1.x + (p2.x - p1.x) * (ymin - p1.y) / (p2.y - p1.y);
                newVec.y = ymin;
            } else if ((codeForPointOutside & BOTTOM) == BOTTOM) {
                newVec.x = p1.x + (p2.x - p1.x) * (ymax - p1.y) / (p2.y - p1.y);
                newVec.y = ymax;
            } else if ((codeForPointOutside & RIGHT) == RIGHT) {
                if (p2.x - p1.x != 0) {
                    newVec.y = p1.y + (p2.y - p1.y) * (xmax - p1.x) / (p2.x - p1.x);
                    newVec.x = xmax;
                } else {
                    newVec.y = p1.y + (p2.y - p1.y);
                    newVec.x = p1.x;
                }
            } else if ((codeForPointOutside & LEFT) == LEFT) {
                if (p2.x - p1.x != 0) {
                    newVec.y = p1.y + (p2.y - p1.y) * (xmin - p1.x) / (p2.x - p1.x);
                    newVec.x = xmin;
                } else {
                    newVec.y = p1.y + (p2.y - p1.y);
                    newVec.x = p1.x;
                }
            }

            if (codeForPointOutside == code1) {
                Vector2 newP1 = rotateAbout(Math.toRadians(0), newVec, origin);
                return playerIntersectingLine(newP1, oldP2, origin, angle, depth + 1, bounds, playerPos);
            } else {
                Vector2 newP2 = rotateAbout(Math.toRadians(0), newVec, origin);
                return playerIntersectingLine(oldP1, newP2, origin, angle, depth + 1, bounds, playerPos);
            }
        }
    }

    private int computeRegionCode(Vector2 point, BoxBounds bounds, Vector2 playerPos) {
        int code = INSIDE;
        Vector2 topLeftPlr = playerPos;

        if (point.x < topLeftPlr.x)
            code |= LEFT;
        else if (point.x > topLeftPlr.x + bounds.width)
            code |= RIGHT;
        if (point.y < topLeftPlr.y)
            code |= TOP;
        else if (point.y > topLeftPlr.y + bounds.height)
            code |= BOTTOM;

        return code;
    }

    public boolean pointInTriangle(float x, float y) {
        float v0x = x3 - x1;
        float v0y = y3 - y1;
        float v1x = x2 - x1;
        float v1y = y2 - y1;
        float v2x = x - x1;
        float v2y = y - y1;

        float dot00 = dot(v0x, v0y, v0x, v0y);
        float dot01 = dot(v0x, v0y, v1x, v1y);
        float dot02 = dot(v0x, v0y, v2x, v2y);
        float dot11 = dot(v1x, v1y, v1x, v1y);
        float dot12 = dot(v1x, v1y, v2x, v2y);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v < 1);
    }

    public boolean isContainedInRectangle(float x, float y, float w, float h) {
        return this.parent.transform.position.x + xBuffer >= x && this.parent.transform.position.x + xBuffer + base <= x + w &&
                this.parent.transform.position.y + yBuffer >= y && this.parent.transform.position.y + yBuffer + height <= y + h;
    }

    private float dot(float x0, float y0, float x1, float y1) {
        return (x0 * x1) + (y0 * y1);
    }

    private void recalculatePoints() {
        double rAngle = Math.toRadians(angle);
        Vector2 p1 = new Vector2(x1, y1);
        Vector2 p2 = new Vector2(x2, y2);
        Vector2 p3 = new Vector2(x3, y3);
        Vector2 origin = new Vector2(parent.transform.position.x + (Constants.GRID_WIDTH / 2.0f), parent.transform.position.y + (Constants.GRID_HEIGHT / 2.0f));

        p1 = rotateAbout(rAngle, p1, origin);
        p2 = rotateAbout(rAngle, p2, origin);
        p3 = rotateAbout(rAngle, p3, origin);

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;
        x3 = p3.x;
        y3 = p3.y;
    }

    private Vector2 rotateAbout(double angle, Vector2 p, Vector2 o) {
        double cos = Math.round(Math.cos(angle) * 100.0) / 100.0;
        double sin = Math.round(Math.sin(angle) * 100.0) / 100.0;
        Vector2 newVector = new Vector2(p.x, p.y);
        newVector.x -= o.x;
        newVector.y -= o.y;

        float newX = (float)((newVector.x * cos) - (newVector.y * sin));
        float newY = (float)((newVector.x * sin) + (newVector.y * cos));

        return new Vector2(newX + o.x, newY + o.y);
    }

    private void recalculateTransform() {
        x1 = (float)(xBuffer + parent.transform.position.x + halfWidth);
        y1 = (float)(yBuffer + parent.transform.position.y);
        x2 = (float)(xBuffer + parent.transform.position.x);
        y2 = (float)(yBuffer + parent.transform.position.y + height);
        x3 = (float)(xBuffer + parent.transform.position.x + base);
        y3 = y2;

        recalculatePoints();
    }

    @Override
    public void draw(Graphics2D g2) {
        if (isSelected) {
            Graphics2D oldGraphics = (Graphics2D)g2.create();
            oldGraphics.translate(parent.transform.position.x, parent.transform.position.y);
            oldGraphics.rotate(Math.toRadians(angle), Constants.TILE_WIDTH / 2.0, Constants.TILE_WIDTH / 2.0);
            oldGraphics.setStroke(new BasicStroke(2));
            oldGraphics.setColor(Color.GREEN);
            oldGraphics.drawLine((int)(halfWidth + xBuffer), (int)yBuffer, (int)xBuffer, (int)(height + yBuffer));
            oldGraphics.drawLine((int)(halfWidth + xBuffer), (int)yBuffer, (int)(base + xBuffer), (int)(height + yBuffer));
            oldGraphics.drawLine((int)xBuffer, (int)(height + yBuffer), (int)(base + xBuffer), (int)(height + yBuffer));
        }
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("TriangleBounds", tabSize));
        builder.append(addDoubleProperty("base", base, tabSize + 1, true, true));
        builder.append(addDoubleProperty("height", height, tabSize + 1, true, true));
        builder.append(addFloatProperty("angle", angle, tabSize + 1, true, true));
        builder.append(addBooleanProperty("isPlaying", isPlaying, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static TriangleBounds deserialize() {
        Parser.consumeBeginObjectProperty();

        double base = Parser.consumeDoubleProperty("base");
        Parser.consume(',');
        double height = Parser.consumeDoubleProperty("height");
        Parser.consume(',');
        float angle = Parser.consumeFloatProperty("angle");
        Parser.consume(',');
        boolean isPlaying = Parser.consumeBooleanProperty("isPlaying");
        Parser.consume('}');

        return new TriangleBounds(base, height, angle, isPlaying);
    }
}
