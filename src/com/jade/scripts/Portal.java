package com.jade.scripts;

import com.jade.components.BoxBounds;
import com.jade.components.Component;
import com.jade.dataStructures.JString;
import com.jade.file.Parser;
import com.jade.jade.GameObject;
import com.jade.main.LevelScene;
import com.jade.main.Window;

public class Portal extends Component {

    public float width, height;
    public int transformState;

    private boolean hasCollided = false;

    public Portal(float width, float height, int transformState) {
        this.width = width;
        this.height = height;
        this.transformState = transformState;
    }

    public Portal clone() {return new Portal(this.width, this.height, this.transformState);}

    @Override
    public void update(double dt) {
        if (Window.isEditing) return;
        if (hasCollided) return;
        GameObject plr = LevelScene.getScene().player;
        float dx = (plr.transform.position.x + plr.getComponent(BoxBounds.class).halfWidth) - (this.parent.transform.position.x + (width / 2.0f));
        float dy = (plr.transform.position.y + plr.getComponent(BoxBounds.class).halfHeight) - (this.parent.transform.position.y + (height / 2.0f));

        float combinedHalfWidths = plr.getComponent(BoxBounds.class).halfWidth + (width / 2.0f);
        float combinedHalfHeights = plr.getComponent(BoxBounds.class).halfHeight + (height / 2.0f);

        if (Math.abs(dx) < combinedHalfWidths) {
            if (Math.abs(dy) < combinedHalfHeights) {
                float overlapX = combinedHalfWidths - Math.abs(dx);
                float overlapY = combinedHalfHeights - Math.abs(dy);

                if (overlapX >= overlapY) {
                    if (dy > 0) {
                        // Collision on the bottom of box
                        hasCollided = true;
                        plr.getComponent(Player.class).state = transformState;
                    } else {
                        // Collision on the top of box
                        // Transform
                        hasCollided = true;
                        plr.getComponent(Player.class).state = transformState;
                    }
                } else if (Math.abs(dx) < 40) {
                    // Transform
                    hasCollided = true;
                    plr.getComponent(Player.class).state = transformState;
                }
            }
        }
    }

    @Override
    public String serialize(int tabSize) {
        StringBuilder builder = JString.getBuilder();

        builder.append(beginObjectProperty("Portal", tabSize));
        builder.append(addFloatProperty("width", width, tabSize + 1, true, true));
        builder.append(addFloatProperty("height", height, tabSize + 1, true, true));
        builder.append(addIntProperty("transformState", transformState, tabSize + 1, true, false));
        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static Portal deserialize() {
        Parser.consumeBeginObjectProperty();

        float width = Parser.consumeFloatProperty("width");
        Parser.consume(',');
        float height = Parser.consumeFloatProperty("height");
        Parser.consume(',');
        int transformState = Parser.consumeIntProperty("transformState");
        Parser.consume('}');

        return new Portal(width, height, transformState);
    }
}
