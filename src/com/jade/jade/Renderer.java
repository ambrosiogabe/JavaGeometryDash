package com.jade.jade;

import com.jade.dataStructures.Vector2;
import com.jade.main.Constants;
import com.jade.main.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {
    public Map<Integer, List<GameObject>> gameObjects;
    public Camera camera;

    public Renderer(Camera camera) {
        this.camera = camera;
        gameObjects = new HashMap<>();
    }

    public void submit(GameObject go) {
        gameObjects.computeIfAbsent(go.zIndex, k -> new ArrayList<GameObject>());
        gameObjects.get(go.zIndex).add(go);
    }

    public void remove(GameObject go) {
        for (List<GameObject> goList : gameObjects.values()) {
            if (goList.contains(go)) {
                goList.remove(go);
                break;
            }
        }
    }

    public void draw(Graphics2D g2) {
        int lowestZIndex = Integer.MAX_VALUE;
        int highestZIndex = Integer.MIN_VALUE;
        for (Integer i : gameObjects.keySet()) {
            if (i < lowestZIndex) lowestZIndex = i;
            if (i > highestZIndex) highestZIndex = i;
        }

        int zIndex = lowestZIndex;
        while (zIndex <= highestZIndex) {
            if (gameObjects.get(zIndex) == null) {
                zIndex++;
                continue;
            }
            for (GameObject gameObject : gameObjects.get(zIndex)) {
                if (!gameObject.isUi) {
                    if (gameObject.transform.position.x > Scene.camera.transform.position.x - Constants.TILE_WIDTH * 2 &&
                            gameObject.transform.position.x < Scene.camera.transform.position.x + Constants.SCREEN_WIDTH) {
                        Transform oldTransform = gameObject.transform;
                        gameObject.transform = new Transform(oldTransform.position);
                        gameObject.transform.position = new Vector2(-camera.transform.position.x + gameObject.transform.position.x, -camera.transform.position.y + gameObject.transform.position.y);
                        gameObject.draw(g2);
                        gameObject.transform = oldTransform;
                    }
                } else {
                    gameObject.draw(g2);
                }
            }
            zIndex++;
        }
    }
}
