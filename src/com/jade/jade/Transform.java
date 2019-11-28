package com.jade.jade;

import com.jade.dataStructures.JString;
import com.jade.dataStructures.Vector2;
import com.jade.file.Parser;
import com.jade.file.Serialize;

public class Transform extends Serialize {
    public Vector2 position;

    public Transform(Vector2 position) {
        this.position = position;
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("Transform", tabSize));
        builder.append(position.serialize(tabSize + 1) + addEnding(true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    @Override
    public String toString() {
        return "Vec2 (" + position.x + ", " + position.y + ")";
    }

    public Transform clone() {
        return new Transform(new Vector2(this.position.x, this.position.y));
    }

    public static Transform deserialize() {
        Parser.skipWhiteSpace();
        String transformTitle = Parser.parseString();
        Parser.checkString("Transform", transformTitle);
        Parser.consume(':');
        Parser.consume('{');

        Vector2 position = Vector2.deserialize();

        Parser.consume('}');

        return new Transform(position);
    }
}
