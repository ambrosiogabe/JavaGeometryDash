package com.jade.dataStructures;

import com.jade.file.Parser;
import com.jade.file.Serialize;

public class Vector2 extends Serialize {
    public float x;
    public float y;

    public Vector2() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float length() {
        return (float)Math.sqrt((x * x) + (y * y));
    }

    public float lengthSquared() {
        return (x * x) + (y * y);
    }

    public static Vector2 normalize(Vector2 value) {
        float v = 1.0f / value.length();
        return new Vector2(value.x * v, value.y * v);
    }

    public static Vector2 add(Vector2 value1, Vector2 value2) {
        return new Vector2(value1.x + value2.x, value1.y + value2.y);
    }

    public static Vector2 subtract(Vector2 value1, Vector2 value2) {
        return new Vector2(value1.x - value2.x, value1.y - value2.y);
    }

    public static Vector2 scale(Vector2 value, float scale) {
        return new Vector2(value.x * scale, value.y * scale);
    }

    public static Vector2 scale(Vector2 value, double scale) {
        return new Vector2(value.x * (float)scale, value.y * (float)scale);
    }

    public static double dot(Vector2 v1, Vector2 v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public static double component(Vector2 vector, Vector2 directionVector) {
        double alpha = Math.atan2(directionVector.y, directionVector.x);
        double theta = Math.atan2(vector.y, vector.x);
        double mag = vector.length();
        double a = mag * Math.cos(theta - alpha);
        return a;
    }

    public static Vector2 componentVector(Vector2 vector, Vector2 directionVector) {
        Vector2 v = Vector2.normalize(directionVector);
        return Vector2.scale(v, Vector2.component(vector, directionVector));
    }

    @Override
    public String toString() {
        return "Vec2<" + this.x + ", " + this.y + ">";
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("Vector2", tabSize));
        builder.append(addFloatProperty("x", x, tabSize + 1, true, true));
        builder.append(addFloatProperty("y", y, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static Vector2 deserialize() {
        String title = Parser.parseString();
        Parser.checkString("Vector2", title);
        Parser.consume(':');
        Parser.consume('{');

        String xTitle = Parser.parseString();
        Parser.checkString("x", xTitle);
        Parser.consume(':');
        float x = Parser.parseFloat();
        Parser.consume(',');

        String yTitle = Parser.parseString();
        Parser.checkString("y", yTitle);
        Parser.consume(':');
        float y = Parser.parseFloat();
        Parser.consume('}');

        return new Vector2(x, y);
    }
}
