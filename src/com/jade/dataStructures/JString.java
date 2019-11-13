package com.jade.dataStructures;

public class JString {
    private static StringBuilder builder = null;

    public static StringBuilder getBuilder() {
        if (JString.builder == null) {
            JString.builder = new StringBuilder();
        }

        JString.builder = new StringBuilder();
        return JString.builder;
    }
}
