package com.jade.scripts;

import com.jade.components.Component;
import com.jade.main.Constants;
import com.jade.main.Scene;
import com.jade.main.Window;

import java.awt.event.MouseEvent;

public class CameraControls extends Component {
    private float prevMx, prevMy;

    @Override
    public void start() {
        prevMx = 0.0f;
        prevMy = 0.0f;
    }

    @Override
    public CameraControls clone() {
        return new CameraControls();
    }

    @Override
    public void update(double dt) {
        if (Window.mouseListener.mousePressed && Window.mouseListener.mouseButton == MouseEvent.BUTTON2) {
            float dx = (Window.mouseListener.x + Window.mouseListener.dx - prevMx);
            float dy = (Window.mouseListener.y + Window.mouseListener.dy - prevMy);
            Scene.camera.transform.position.x -= dx;
            Scene.camera.transform.position.y -= dy;
            if (Scene.camera.transform.position.y > Constants.CAMERA_OFFSET_Y + 30)
                Scene.camera.transform.position.y = Constants.CAMERA_OFFSET_Y + 30;
        }

        prevMx = Window.mouseListener.x + Window.mouseListener.dx;
        prevMy = Window.mouseListener.y + Window.mouseListener.dy;
    }
}
