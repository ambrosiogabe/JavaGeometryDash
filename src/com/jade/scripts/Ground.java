package com.jade.scripts;

import com.jade.dataStructures.Vector2;
import com.jade.components.BoxBounds;
import com.jade.components.Component;
import com.jade.jade.GameObject;
import com.jade.main.Constants;
import com.jade.main.LevelScene;
import com.jade.main.Window;

import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;

public class Ground extends Component {
    GameObject player;
    Vector2 originalPos;
    float offsetX;

    public float maxY = 0.0f;

    public Ground(GameObject player) {
        this.player = player;
    }

    @Override
    public void start() {
        this.offsetX = Vector2.subtract(player.transform.position, this.parent.transform.position).x;
        this.originalPos = this.parent.transform.position;
        maxY = this.parent.transform.position.y;
    }

    @Override
    public Ground clone() {
        return new Ground(this.player);
    }

    @Override
    public void update(double dt) {
        this.parent.transform.position = Vector2.subtract(player.transform.position, new Vector2(-Constants.CAMERA_OFFSET_X, 0));
        this.parent.transform.position.y = originalPos.y;

        if (player.transform.position.y > maxY - player.getComponent(BoxBounds.class).height) {
            player.transform.position.y = maxY - (float)player.getComponent(BoxBounds.class).height;
            player.getComponent(BoxBounds.class).onGround = true;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.WHITE);
        g2.drawRect((int)this.parent.transform.position.x, (int)this.parent.transform.position.y, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    }
}
