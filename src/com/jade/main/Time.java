package com.jade.main;

public class Time {
    public static double timeStarted = System.nanoTime();

    public static double getTime() {
        return (System.nanoTime() - timeStarted) * 1E-9;
    }
}
