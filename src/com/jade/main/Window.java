package com.jade.main;

import com.jade.jade.AssetPool;

import javax.swing.JFrame;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

public class Window extends JFrame implements Runnable {

    public static Graphics graphics;
    public static KL keyListener = new KL();
    public static ML mouseListener = new ML();
    public static boolean isEditing = false;
    boolean isRunning = true;

    Color[] colors = {
            new Color(15.0f / 255.0f, 98.0f / 255.0f, 212.0f / 255.0f, 1.0f),    // BLUE
            new Color(140.0f / 255.0f, 29.0f / 255.0f, 209.0f / 255.0f, 1.0f),  // PURPLE
            new Color(209.0f / 255.0f, 29.0f / 255.0f, 179.0f / 255.0f, 1.0f),  // PINK
            new Color(181.0f / 255.0f, 31.0f / 255.0f, 31.0f / 255.0f, 1.0f),   // RED
            new Color(237.0f / 255.0f, 132.0f / 255.0f, 40.0f / 255.0f, 1.0f),  // ORANGE
            new Color(247.0f / 255.0f, 229.0f / 255.0f, 64.0f / 255.0f, 1.0f),  // YELLOW
            new Color(44.0f / 255.0f, 163.0f / 255.0f, 23.0f / 255.0f, 1.0f),   // GREEN
            new Color(15.0f / 255.0f, 209.0f / 255.0f, 193.0f / 255.0f, 1.0f),  // CYAN
    };

    float ogSteps = 300.0f;
    int currentColor = 0;
    int nextColor = 1;
    float steps = ogSteps;
    float blueStepAmount = 0.0f;
    float redStepAmount = 0.0f;
    float greenStepAmount = 0.0f;
    public Color bgColor = colors[currentColor];
    public Color groundColor = new Color(28.0f / 255.0f, 70.0f / 255.0f, 148.0f / 255.0f, 1.0f);

    private static Window window = null;
    private boolean isPaused = false;
    private float debounce = 0.1f;
    private float debounceLeft = 0.1f;
    private int currentScene = 0;

    public Graphics doubleBuffer = null;
    Image doubleBufferImage = null;
    public static Scene scene;

    public Window() {
        this.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        this.setTitle(Constants.SCREEN_TITLE);
        this.setResizable(false);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addKeyListener(Window.keyListener);
        this.addMouseListener(Window.mouseListener);
        this.addMouseMotionListener(Window.mouseListener);
        this.setLocationRelativeTo(null);
    }

    public static Window getWindow() {
        if (Window.window == null) {
            Window.window = new Window();
        }

        return Window.window;
    }

    public void changeScene(int scene) {
        AssetPool.clearSprites();
        LevelEditorScene.deleteScene();
        LevelScene.deleteScene();
        currentScene = scene;

        switch(scene) {
            case 0:
                Window.isEditing = true;
                Window.scene = LevelEditorScene.getScene();
                Window.scene.init();
                break;
            case 1:
                Window.isEditing = false;
                Window.scene = LevelScene.getScene();
                Window.scene.init();
                break;
            default:
                System.out.println("UNKNOWN SCENE!!");
                Window.scene = null;
                break;
        }
    }

    public void init() {
        changeScene(0);
        Window.graphics = getGraphics();
    }

    public void pause() {
        isPaused = true;
    }

    public void update(double dt) {
        debounceLeft -= dt;
        if (isPaused) {
            if (keyListener.isKeyPressed(KeyEvent.VK_ENTER) && debounceLeft < 0) {
                draw(getGraphics());
                debounceLeft = debounce;
                Window.scene.update(0.01);
            } else if (keyListener.isKeyPressed(KeyEvent.VK_ESCAPE)) {
                isPaused = false;
            }
        } else {
            Window.scene.update(dt);
            if (isPaused) return;
            draw(getGraphics());
            if (keyListener.isKeyPressed(KeyEvent.VK_F5)) {
                isPaused = true;
            }
        }
    }

    public void draw(Graphics g) {
        Dimension d = getSize();
        if (doubleBufferImage == null) {
            doubleBufferImage = createImage(d.width, d.height);
            doubleBuffer = doubleBufferImage.getGraphics();
        }
        renderOffscreen(doubleBufferImage.getGraphics());
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentScene == 0)
            g.drawImage(doubleBufferImage, 0, 0, null);
        else
            g.drawImage(doubleBufferImage, -300, -200, Constants.SCREEN_WIDTH + 600, Constants.SCREEN_HEIGHT + 400, null);
    }

    public void renderOffscreen(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        float r = Math.max(Math.min(bgColor.getRed() + redStepAmount, 255.0f), 0.0f);
        float gr = Math.max(Math.min(bgColor.getGreen() + greenStepAmount, 255.0f), 0.0f);
        float b = Math.max(Math.min(bgColor.getBlue() + blueStepAmount, 255.0f), 0.0f);

        bgColor = new Color(r / 255.0f, gr / 255.0f, b / 255.0f);
        //steps -= 1.0f;
        if (steps <= 0) {
            steps = ogSteps;
            currentColor = (currentColor + 1) % colors.length;
            nextColor = (nextColor + 1) % colors.length;
            bgColor = colors[currentColor];

            int oldRed = colors[currentColor].getRed();
            int newRed = colors[nextColor].getRed();
            int oldBlue = colors[currentColor].getBlue();
            int newBlue = colors[nextColor].getBlue();
            int oldGreen = colors[currentColor].getGreen();
            int newGreen = colors[nextColor].getGreen();
            blueStepAmount = (newBlue - oldBlue) / steps;
            redStepAmount = (newRed - oldRed) / steps;
            greenStepAmount = (newGreen - oldGreen) / steps;
        }

        Window.scene.draw(g2);
    }

    public void run() {
        double lastFrameTime = 0.0;
        try {
            while(isRunning) {
                double time = Time.getTime();
                double deltaTime = time - lastFrameTime;
                lastFrameTime = time;

                update(deltaTime);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
