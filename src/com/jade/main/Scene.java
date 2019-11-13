package com.jade.main;

import com.jade.jade.Camera;

import java.awt.Graphics2D;

public abstract class Scene {
    String name;
    public static Camera camera;

    public void Scene(String name) {
        this.name = name;
    }

    public abstract void init();
    public abstract void update(double dt);
    public abstract void draw(Graphics2D g2);
}
