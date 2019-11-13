package com.jade.main;

public class Constants {
    // =======================================================
    // Physics Constants
    // =======================================================
    public static final int GRAVITY = 2850;
    public static final int TERMINAL_VELOCITY = 1900;

    // =======================================================
    // Window properties
    // =======================================================
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final String SCREEN_TITLE = "Geometry Dash";

    // =======================================================
    // Background image constants
    // =======================================================
    public static final int BG_WIDTH = 512;
    public static final int BG_HEIGHT = 512;
    public static final int GROUND_BG_WIDTH = 256;
    public static final int GROUND_BG_HEIGHT = 256;

    // =======================================================
    // Player constants
    // =======================================================
    public static final int PLR_WIDTH = 41;
    public static final int PLR_HEIGHT = 41;
    public static final int SPEED = 395;
    public static final int JUMP_FORCE = -650;
    public static final int FLY_FORCE = -45;
    public static final int FLY_TERMINAL_VELOCITY = 500;

    // =======================================================
    // Tile constants
    // =======================================================
    public static final int TILE_WIDTH = 42;
    public static final int ONE_TENTH_TILE_WIDTH = 4;
    public static final int GRID_WIDTH = 42;
    public static final int GRID_HEIGHT = 42;


    // =======================================================
    // Camera constants
    // =======================================================
    public static final int GROUND_HEIGHT = 3 * Constants.TILE_WIDTH;
    public static final int CAMERA_OFFSET_Y = -385;
    public static final int CAMERA_OFFSET_X = -400;
    public static final int CAMERA_BOX_TOP_Y = 250;
    public static final int CAMERA_BOX_BOTTOM_Y = 450;

    // =======================================================
    // Level editor UI constants
    // =======================================================
    public static final int BUTTON_WIDTH = 60;
    public static final int BIG_BUTTON_WIDTH = 90;
    public static final int MENU_CONTAINER_Y = 535;
    public static final int BUTTON_OFFSET_X = 400;
    public static final int BUTTON_OFFSET_Y = 560;
    public static final int BUTTON_HORIZONTAL_SPACING = 10;

    public static final int TAB_WIDTH = 75;
    public static final int TAB_HEIGHT = 38;
    public static final int TAB_OFFSET_X = 380;
    public static final int TAB_OFFSET_Y = 497;
    public static final int TAB_HORIZONTAL_SPACING = 10;

    // =======================================================
    // Miscellaneous
    // =======================================================
    public static final char[] WHITESPACE = {'\n', ' ', '\t', '\r'};
}
