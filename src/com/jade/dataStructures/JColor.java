package com.jade.dataStructures;

import com.jade.file.Parser;
import com.jade.file.Serialize;

import java.awt.Color;

public class JColor extends Serialize {
    public Color color;
    int r;
    int g;
    int b;

    public JColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.color = new Color((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f);
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("JColor", tabSize));
        builder.append(addIntProperty("r", r, tabSize + 1, true, true));
        builder.append(addIntProperty("g", g, tabSize + 1, true, true));
        builder.append(addIntProperty("b", b, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static JColor deserialize() {
        Parser.consumeBeginObjectProperty();

        int r = Parser.consumeIntProperty("r");
        Parser.consume(',');
        int g = Parser.consumeIntProperty("g");
        Parser.consume(',');
        int b = Parser.consumeIntProperty("b");
        Parser.consume('}');

        return new JColor(r, g, b);
    }
}
