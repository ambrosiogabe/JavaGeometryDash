package com.jade.file;

import com.jade.components.TriangleBounds;
import com.jade.dataStructures.JString;
import com.jade.components.BoxBounds;
import com.jade.components.Component;
import com.jade.jade.GameObject;
import com.jade.components.SubSprite;
import com.jade.scripts.Portal;

public class Parser {

    public static int offset = 0;
    public static int line = 1;
    public static byte[] bytes;

    public static void init(int offset, byte[] bytes) {
        Parser.offset = offset;
        Parser.bytes = bytes;
    }

    public static GameObject getNextGameObject() {
        if (bytes.length == 0 || offset >= bytes.length) return null;

        if (peek() == ',') Parser.consume(',');
        skipWhiteSpace();
        if (offset >= bytes.length - 1) return null;

        GameObject go = GameObject.deserialize();
        return go;
    }

    public static void skipWhiteSpace() {
        char c = '\0';
        while (offset != bytes.length - 1 && (peek() == ' ' || peek() == '\t' || peek() == '\n' || peek() == '\r' || peek() == '\f')) {
            if (peek() == '\n') Parser.line++;
            advance();
        }
    }

    public static String parseString() {
        skipWhiteSpace();
        char c;
        StringBuilder builder = JString.getBuilder();

        consume('"');

        while (offset != bytes.length - 1 && peek() != '"') {
            c = advance();
            builder.append(c);
        }
        consume('"');

        return builder.toString();
    }

    public static boolean parseBoolean() {
        skipWhiteSpace();
        char c;
        StringBuilder builder = JString.getBuilder();

        if (offset != bytes.length - 1 && peek() == 't') {
            c = advance();
            builder.append(c);
            consume('r');
            builder.append('r');
            consume('u');
            builder.append('u');
            consume('e');
            builder.append('e');
        } else if (offset != bytes.length - 1 && peek() == 'f') {
            c = advance();
            builder.append(c);
            consume('a');
            builder.append('a');
            consume('l');
            builder.append('l');
            consume('s');
            builder.append('s');
            consume('e');
            builder.append('e');
        } else {
            System.out.println("Error: Expected 'true' or 'false'. Instead got '" + peek() + "' at line: " + line);
            System.exit(-1);
        }

        return builder.toString().compareTo("true") == 0;
    }

    public static float parseFloat() {
        skipWhiteSpace();
        char c;
        StringBuilder builder = JString.getBuilder();

        while (isDigit(peek()) || peek() == '.' || peek() == 'f' || peek() == '-') {
            c = advance();
            builder.append(c);
        }

        return Float.parseFloat(builder.toString());
    }

    public static double parseDouble() {
        skipWhiteSpace();
        char c;
        StringBuilder builder = JString.getBuilder();

        while (isDigit(peek()) || peek() == '.' || peek() == '-') {
            c = advance();
            builder.append(c);
        }

        return Float.parseFloat(builder.toString());
    }

    public static int parseInt() {
        skipWhiteSpace();
        char c;
        StringBuilder builder = JString.getBuilder();

        while (isDigit(peek()) || peek() == '-') {
            c = advance();
            builder.append(c);
        }

        return Integer.parseInt(builder.toString());
    }

    public static char peek() {
        if (offset >= bytes.length) return '\0';
        return (char)bytes[offset];
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static void consume(char c) {
        skipWhiteSpace();
        char actual = (char)bytes[offset];
        if (actual != c) {
            System.out.println("Error: Expected '" + c + "' instead got '" + actual + "' at line: " + Parser.line);
            System.exit(-1);
        }
        offset++;
    }

    public static char advance() {
        char c = (char)bytes[offset];
        offset++;
        return c;
    }

    public static void checkString(String s1, String s2) {
        if (s1.compareTo(s2) != 0) {
            System.out.println("Error: Expected \"" + s1 + "\" instead got \"" + s2 + "\" at line: " + Parser.line);
            System.exit(-1);
        }
    }

    public static Component parseComponent() {
        String componentTitle = Parser.parseString();
        switch (componentTitle) {
            case "SubSprite":
                return SubSprite.deserialize();
            case "BoxBounds":
                return BoxBounds.deserialize();
            case "TriangleBounds":
                return TriangleBounds.deserialize();
            case "Portal":
                return Portal.deserialize();
            default:
                System.out.println("Could not find a component of type \"" + componentTitle + "\" at line: " + Parser.line);
                System.exit(-1);
        }

        return null;
    }


    // Helper functions
    public static void consumeBeginObjectProperty() {
        consume(':');
        consume('{');
    }

    public static void consumeBeginObjectProperty(String name) {
        String title = parseString();
        checkString(name, title);
        consumeBeginObjectProperty();
    }

    public static String consumeStringProperty(String propertyName) {
        String title = parseString();
        checkString(propertyName, title);
        consume(':');
        return parseString();
    }

    public static int consumeIntProperty(String propertyName) {
        String title = parseString();
        checkString(propertyName, title);
        consume(':');
        return parseInt();
    }

    public static float consumeFloatProperty(String propertyName) {
        String title = parseString();
        checkString(propertyName, title);
        consume(':');
        return parseFloat();
    }

    public static double consumeDoubleProperty(String propertyName) {
        String title = parseString();
        checkString(propertyName, title);
        consume(':');
        return parseDouble();
    }

    public static boolean consumeBooleanProperty(String propertyName) {
        String title = parseString();
        checkString(propertyName, title);
        consume(':');
        return parseBoolean();
    }
}
