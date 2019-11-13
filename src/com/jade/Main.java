package com.jade;

import com.jade.main.Window;

public class Main {
    public static void main(String[] args) {
        Window window = Window.getWindow();
        window.init();

        Thread t1 = new Thread(window);
        t1.start();
    }
}
