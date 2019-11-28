package com.jade.dataStructures;

import com.jade.components.Component;

import java.awt.*;

public class NodeGene extends Component implements Comparable<NodeGene> {

    public enum Type {
        INPUT,
        HIDDEN,
        OUTPUT,
        ;
    }

    private Type type;
    private int id;
    private float value;
    private boolean debug = true;

    public int layer;
    public float x, y;

    public NodeGene(Type type, int id, float value, int layer) {
        this.type = type;
        this.id = id;
        this.layer = layer;
        this.value = value;
    }

    public NodeGene(NodeGene toBeCopied) {
        type = toBeCopied.getType();
        id = toBeCopied.getId();
        layer = toBeCopied.layer;
    }

    public void addValue(float valueToAdd) {
        this.value += valueToAdd;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float val) {
        this.value = val;
    }

    public Type getType() {
        return this.type;
    }

    public int getId() {
        return this.id;
    }

    public NodeGene copy() {
        return new NodeGene(this.type, this.id, this.value, this.layer);
    }

    @Override
    public Component clone() {
        return copy();
    }

    @Override
    public void draw(Graphics2D g2) {
        if (debug) {
            g2.setColor(Color.BLACK);
            g2.drawOval((int) x, (int) y, 40, 40);
            g2.setColor(Color.WHITE);
            g2.fillOval((int) x, (int) y, 40, 40);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            g2.drawString("" + (id + 1), x + 17, y + 19);
            g2.drawString("" + value, x + 13, y + 32);
        } else {
            g2.setColor(Color.BLACK);
            g2.drawOval((int)x, (int)y, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval((int)x + 2, (int)y + 2, 8, 8);
        }
    }

    @Override
    public int compareTo(NodeGene gene) {
        if (gene.layer == this.layer) {
            if (id == gene.getId()) return 0;

            return gene.getId() > id ? -1 : 1;
        }

        return gene.layer > this.layer ? -1 : 1;
    }
}
