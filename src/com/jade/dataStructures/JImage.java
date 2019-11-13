package com.jade.dataStructures;

import com.jade.components.Component;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class JImage extends Component {
    public BufferedImage image;

    public JImage(BufferedImage subImg) {
        this.image = subImg;
    }

    @Override
    public JImage clone() {
        return new JImage(this.image);
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("JImage", tabSize));
        builder.append(addIntProperty("width", image.getWidth(), tabSize + 1, true, true));
        builder.append(addIntProperty("height", image.getHeight(), tabSize + 1, true, true));

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        builder.append(addByteArrayProperty("imageData", pixels, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }
}
