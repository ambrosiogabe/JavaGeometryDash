package com.jade.components;

import com.jade.jade.GameObject;
import com.jade.file.Serialize;

import java.awt.*;

public abstract class Component<T> extends Serialize {
    public GameObject parent;

    public void draw(Graphics2D g2) {
        return;
    }

    public void update(double dt) {
        return;
    }

    public void start() {
        return;
    }

    public abstract Component clone();

    @Override
    public String serialize(int tabSize) {
        return "";
    }
}
