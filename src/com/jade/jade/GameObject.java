package com.jade.jade;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

import com.jade.components.Component;
import com.jade.dataStructures.JString;
import com.jade.file.Parser;
import com.jade.file.Serialize;

import java.util.List;
import java.util.ArrayList;

public class GameObject extends Serialize implements Comparable<GameObject> {
    List<Component> components;
    List<Component> componentsToRemove = new ArrayList<>();
    List<Component> componentsToAdd = new ArrayList<>();

    private boolean debugMode = false;
    public String name;
    public Transform transform;
    public List<GameObject> gameObjects;
    public GameObject parent;
    public boolean isUi = false;
    public int zIndex = 0;

    public GameObject(String name, Transform transform) {
        components = new ArrayList<Component>();
        gameObjects = new ArrayList<GameObject>();
        this.name = name;
        this.transform = transform;
        this.parent = null;
    }

    public GameObject(String name) {
        // Only meant to be called for a "Scene" game object
        // a game object that encapsulates everything
        this.name = name;
        this.transform = null;
    }

    public void setZIndex(int newIndex) {
        this.zIndex = newIndex;
    }

    public void setUi(boolean set) {
        isUi = set;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }

        return null;
    }

    private <T extends Component> void removeComponent(Component comp) {
        for (Component c : components) {
            if (c == comp) {
                components.remove(c);
                return;
            }
        }
    }

    public <T extends Component> void safeRemoveComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                componentsToRemove.add(c);
                return;
            }
        }
    }

    public void addComponent(Component c) {
        c.parent = this;
        components.add(c);
        c.start();
    }

    public void safeAddComponent(Component c) {
        componentsToAdd.add(c);
    }

    public void draw(Graphics2D g2) {
        if (debugMode) {
            g2.setColor(Color.BLUE);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 20));
            g2.drawString(name, transform.position.x, transform.position.y);
        }

        for (Component c : components) {
            c.draw(g2);
        }
    }

    public void update(double dt) {
        for (Component c : components) {
            c.update(dt);
        }

        if (componentsToAdd.size() > 0) {
            for (Component c : componentsToAdd) {
                addComponent(c);
            }
            componentsToAdd.clear();
        }

        if (componentsToRemove.size() > 0) {
            for (Component c : componentsToRemove) {
                removeComponent(c);
            }
            componentsToRemove.clear();
        }
    }

    public List<Component> getAllComponents() {
        return this.components;
    }

    public void addGameObject(GameObject newObj) {
        this.gameObjects.add(newObj);
        newObj.parent = this;
    }

    public GameObject clone() {
        GameObject go = new GameObject(this.name, this.transform.clone());
        for (Component c : components) {
            go.addComponent(c.clone());
        }
        for (GameObject g : gameObjects) {
            go.gameObjects.add(g.clone());
        }
        return go;
    }

    @Override
    public int compareTo(GameObject go) {
        if (go.transform.position.y != this.transform.position.y)
            return this.transform.position.y > go.transform.position.y ? 1 : -1;

        if (go.transform.position.x == this.transform.position.x) return 0;
        return this.transform.position.x > go.transform.position.x ? 1 : -1;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String serialize(int tabSize) {
        if (name == "Nonserialize") return "";

        StringBuilder builder = JString.getBuilder();

        // GameObject
        builder.append(beginObjectProperty("GameObject", tabSize));

        // Transform
        builder.append(transform.serialize(tabSize + 1) + addEnding(true, true));

        // Z Index
        builder.append(addIntProperty("zIndex", zIndex, tabSize + 1, true, true));

        // Name
        if (components.size() > 0 || gameObjects.size() > 0)
            builder.append(addStringProperty("Name", name, tabSize + 1, true, true));
        else
            builder.append(addStringProperty("Name", name, tabSize + 1, true, false));

        // Components
        int i = 0;
        if (components.size() > 0)
            builder.append(beginObjectProperty("Components", tabSize + 1));
        for (Component c : components) {
            String str = c.serialize(tabSize + 2);
            if (str.compareTo("") != 0) builder.append(str);
            if (str.compareTo("") != 0 && i != components.size() - 1)
                builder.append(addEnding(true, true));
            else if (str.compareTo("") != 0)
                builder.append(addEnding(true, false));
            i++;
        }

        // Add comma after components if needed
        if (components.size() > 0)
            builder.append(closeObjectProperty(tabSize + 1));
        if (gameObjects.size() > 0)
            builder.append(addEnding(true, true));
        else
            builder.append(addEnding(true, false));

        // GameObjects
        i = 0;
        if (gameObjects.size() > 0)
            beginObjectProperty("GameObjects", tabSize + 1);
        for (GameObject go : gameObjects) {
            builder.append(go.serialize(tabSize + 2));

            if (i != gameObjects.size() - 1)
                builder.append(addEnding(true, true));
            else
                builder.append(addEnding(true, false));
            i++;
        }
        if (gameObjects.size() > 0)
            closeObjectProperty(tabSize + 1);

        builder.append(closeObjectProperty(tabSize));

        return builder.toString();
    }

    public static GameObject deserialize() {
        String gameObjectTitle = Parser.parseString();
        Parser.checkString("GameObject", gameObjectTitle);

        Parser.consume(':');
        Parser.consume('{');

        Transform transform = Transform.deserialize();
        Parser.consume(',');

        int zIndex = Parser.consumeIntProperty("zIndex");
        Parser.consume(',');

        String nameTitle = Parser.parseString();
        Parser.checkString("Name", nameTitle);
        Parser.consume(':');
        String name = Parser.parseString();

        GameObject go = new GameObject(name, transform);

        String title = "";
        if (Parser.peek() == ',') {
            Parser.consume(',');
            title = Parser.parseString();
            if (title.compareTo("Components") == 0) {
                Parser.consume(':');
                Parser.consume('{');
                Component c = Parser.parseComponent();
                go.addComponent(c);

                while (Parser.peek() == ',') {
                    Parser.consume(',');
                    c = Parser.parseComponent();
                    go.addComponent(c);
                }
                Parser.consume('}');
            }
        }

        if (title.compareTo("GameObjects") == 0) {
            Parser.consume(':');
            Parser.consume('{');
            GameObject g = Parser.getNextGameObject();
            go.addGameObject(g);

            while (Parser.peek() == ',') {
                Parser.consume(',');
                g = Parser.getNextGameObject();
                go.addGameObject(g);
            }
            Parser.consume('}');
        }

        Parser.consume('}');
        go.setZIndex(zIndex);
        return go;
    }
}
