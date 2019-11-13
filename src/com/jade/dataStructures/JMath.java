package com.jade.dataStructures;

public class JMath {
    public static boolean nearF(double x, double y,double precision) {
        return x >= y - precision && x <= y + precision;
    }

    public static boolean nearF(float x, float y, float precision) {
        return x >= y - precision && x <= y + precision;
    }
}
