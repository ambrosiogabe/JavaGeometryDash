package com.jade.components;

import com.jade.dataStructures.JString;
import com.jade.jade.AssetPool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sprite extends Component {
    public BufferedImage image;
    public String pictureFile;
    public boolean isSpritesheet = false;
    public int tileWidth = 0;
    public int tileHeight = 0;
    public int width = 0, height = 0;
    public int spacing = 0;
    public int columns = 0;
    public int gid = 1;
    public int size;

    public List<SubSprite> sprites = new ArrayList<>();

    public Sprite(String pictureFile, boolean isSpritesheet, int tileWidth, int tileHeight, int columns, int spacing, int size) {
        this.pictureFile = pictureFile;
        this.isSpritesheet = isSpritesheet;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        this.spacing = spacing;
        this.columns = columns;
        this.size = size;

        if (AssetPool.hasSprite(new File(pictureFile).getAbsolutePath())) {
            System.out.println(AssetPool.getSprite(new File(pictureFile).getAbsolutePath()));
            System.out.println("Error trying to recreate a sprite that already exists! Sprite: " + pictureFile);
            System.exit(-1);
        }

        AssetPool.addSprite(new File(pictureFile).getAbsolutePath(), this);

        try {
            this.image = ImageIO.read(new File(pictureFile));
            this.width = image.getWidth();
            this.height = image.getHeight();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Sprite(String pictureFile, int width, int height) {
        this.pictureFile = pictureFile;
        this.width = width;
        this.height = height;
        this.isSpritesheet = false;

        if (AssetPool.hasSprite(new File(pictureFile).getAbsolutePath())) return;

        AssetPool.addSprite(new File(pictureFile).getAbsolutePath(), this);
        try {
            this.image = ImageIO.read(new File(pictureFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void loadSpritesheet() {
        int row = 0;
        int count = 0;
        while (count < this.size) {
            for (int column = 0; column < columns; column++) {
                int imgX = (column * tileWidth) + (column * spacing);
                int imgY = (row * tileHeight) + (row * spacing);

                sprites.add(new SubSprite(this.pictureFile, imgX, imgY, tileWidth, tileHeight, row, column));
                count++;
                if (count > this.size - 1) {
                    break;
                }
            }
            row++;
        }
    }

    public int getX(int id) {
        id -= gid;
        id = id % columns;
        return id * tileWidth + (id * spacing);
    }

    public int getY(int id) {
        id -= gid;
        id = id / columns;
        return id * tileHeight + (id * spacing);
    }

    @Override
    public Sprite clone() {
        return new Sprite(this.pictureFile, this.isSpritesheet, this.tileWidth, this.tileHeight, this.columns, this.spacing, this.size);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!isSpritesheet) {
            g2.drawImage(this.image, (int)parent.transform.position.x, (int)parent.transform.position.y, width, height, null);
        }
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("Sprite", tabSize));
        builder.append(addStringProperty("pictureFile", pictureFile, tabSize + 1, true, true));
        builder.append(addBooleanProperty("isSpritesheet", isSpritesheet, tabSize + 1, true, true));
        builder.append(addIntProperty("tileWidth", tileWidth, tabSize + 1, true, true));
        builder.append(addIntProperty("tileHeight", tileHeight, tabSize + 1, true, true));
        builder.append(addIntProperty("columns", columns, tabSize + 1, true, true));
        builder.append(addIntProperty("spacing", spacing, tabSize + 1, true, true));
        builder.append(addIntProperty("width", width, tabSize + 1, true, true));
        builder.append(addIntProperty("height", height, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }
}
