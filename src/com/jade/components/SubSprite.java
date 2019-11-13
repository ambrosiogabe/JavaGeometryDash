package com.jade.components;

import com.jade.dataStructures.JImage;
import com.jade.dataStructures.JString;
import com.jade.jade.AssetPool;
import com.jade.file.Parser;

import java.awt.*;
import java.io.File;

public class SubSprite extends Component {
    public Sprite spriteParent;

    public int imgX, imgY, width, height;
    public int row, column;
    public JImage subImg;

    public SubSprite(String parentImageFile, int imgX, int imgY, int width, int height, int row, int column) {
        Sprite parentSprite = AssetPool.getSprite(new File(parentImageFile).getAbsolutePath());
        this.spriteParent = parentSprite;
        this.imgX = imgX;
        this.imgY = imgY;
        this.width = width;
        this.height = height;
        this.row = row;
        this.column = column;

        if (parentSprite != null) {
            this.subImg = new JImage(parentSprite.image.getSubimage(imgX, imgY, width, height));
        }
        else {
            System.out.println("Error! Sprite parent is null: '" + parentImageFile + "'");
            System.exit(-1);
        }
    }

    public void draw(Graphics2D g2) {
        Graphics2D oldGraphics = (Graphics2D)g2.create();
        oldGraphics.translate(parent.transform.position.x, parent.transform.position.y);

        if (parent.getComponent(BoxBounds.class) != null) {
            BoxBounds bounds = parent.getComponent(BoxBounds.class);
            //oldGraphics.translate(bounds.xBuffer, bounds.yBuffer);
            //oldGraphics.rotate(Math.toRadians(bounds.angle), bounds.width / 2.0, bounds.height / 2.0);
            oldGraphics.rotate(Math.toRadians(bounds.angle), width / 2.0, height / 2.0);
        } else if (parent.getComponent(TriangleBounds.class) != null) {
            TriangleBounds bounds = parent.getComponent(TriangleBounds.class);
            //oldGraphics.translate(bounds.xBuffer, bounds.yBuffer);
            //oldGraphics.rotate(Math.toRadians(bounds.angle), bounds.base / 2.0, bounds.height / 2.0);
            oldGraphics.rotate(Math.toRadians(bounds.angle), width / 2.0, height / 2.0);
        }

        oldGraphics.drawImage(subImg.image, 0, 0, width, height, null);
        oldGraphics.dispose();
    }

    @Override
    public SubSprite clone() {
        return new SubSprite(spriteParent.pictureFile, imgX, imgY, width, height, row, column);
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("SubSprite", tabSize));
        builder.append(addStringProperty("parentImageFile", spriteParent.pictureFile, tabSize + 1, true, true));
        builder.append(addIntProperty("imgX", imgX, tabSize + 1, true, true));
        builder.append(addIntProperty("imgY", imgY, tabSize + 1, true, true));
        builder.append(addIntProperty("width", width, tabSize + 1, true, true));
        builder.append(addIntProperty("height", height, tabSize + 1, true, true));
        builder.append(addIntProperty("row", row, tabSize + 1, true, true));
        builder.append(addIntProperty("column", column, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static SubSprite deserialize() {
        Parser.consumeBeginObjectProperty();

        String parentImageFile = Parser.consumeStringProperty("parentImageFile");
        Parser.consume(',');

        int imgX = Parser.consumeIntProperty("imgX");
        Parser.consume(',');
        int imgY = Parser.consumeIntProperty("imgY");
        Parser.consume(',');
        int width = Parser.consumeIntProperty("width");
        Parser.consume(',');
        int height = Parser.consumeIntProperty("height");
        Parser.consume(',');
        int row = Parser.consumeIntProperty("row");
        Parser.consume(',');
        int column = Parser.consumeIntProperty("column");
        Parser.consume('}');

        return new SubSprite(parentImageFile, imgX, imgY, width, height, row, column);
    }
}
