package com.jade.jade;

import com.jade.components.Sprite;

import java.util.HashMap;
import java.util.Map;

public class AssetPool {
    static Map<String, Sprite> sprites = new HashMap<>();

    public static boolean hasSprite(String pictureFile) {
        return AssetPool.sprites.containsKey(pictureFile);
    }

    public static Sprite getSprite(String pictureFile) {
        if (AssetPool.hasSprite(pictureFile)) {
            return AssetPool.sprites.get(pictureFile);
        }

        return null;
    }

    public static void addSprite(String pictureFile, Sprite sprite) {
        if (!AssetPool.hasSprite(pictureFile)) {
            AssetPool.sprites.put(pictureFile, sprite);
        }
    }

    public static void clearSprites() {
        AssetPool.sprites.clear();
    }
}
