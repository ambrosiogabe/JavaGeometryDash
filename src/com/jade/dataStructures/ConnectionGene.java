package com.jade.dataStructures;

import com.jade.components.Component;

import java.awt.*;

public class ConnectionGene extends Component implements Comparable<ConnectionGene> {

    private NodeGene inNode;
    private NodeGene outNode;
    private int inNodeIndex;
    private int outNodeIndex;
    private float weight;
    private boolean expressed;
    private int innovation;
    private boolean debug = true;

    public ConnectionGene(NodeGene inNode, NodeGene outNode, float weight, boolean expressed, InnovationGenerator innovationGenerator) {
        this.inNode = inNode;
        this.outNode = outNode;
        this.weight = weight;
        this.expressed = expressed;
        this.inNodeIndex = inNode.getId();
        this.outNodeIndex = outNode.getId();

        this.innovation = innovationGenerator.getInnovation(this);
    }

    public ConnectionGene(int inNode, int outNode, float weight, boolean expressed, int innovation) {
        this.inNodeIndex = inNode;
        this.outNodeIndex = outNode;
        this.weight = weight;
        this.expressed = expressed;
        this.innovation = innovation;
    }

    public ConnectionGene(ConnectionGene toBeCopied) {
        inNodeIndex = toBeCopied.inNodeIndex;
        outNodeIndex = toBeCopied.outNodeIndex;
        weight = toBeCopied.weight;
        innovation = toBeCopied.innovation;
        expressed = toBeCopied.expressed;
    }

    public void setNodes(NodeGene inNode, NodeGene outNode) {
        this.inNode = inNode;
        this.outNode = outNode;
    }

    public void feedForward() {
        this.outNode.addValue(this.inNode.getValue() * this.weight);
        this.outNode.setValue(sigmoid(this.outNode.getValue()));
    }
    private float sigmoid(float x) {
        return (float)(1 / (1 + Math.exp(-1.9 * x)));
    }

    public int getInNodeIndex() { return this.inNodeIndex; }
    public int getOutNodeIndex() { return this.outNodeIndex; }

    public NodeGene getInNode() {
        return this.inNode;
    }

    public NodeGene getOutNode() {
        return this.outNode;
    }

    public float getWeight() {
        return this.weight;
    }

    public void disable() {
        this.expressed = false;
    }

    public void enable() {
        this.expressed = true;
    }

    public boolean isExpressed() {
        return this.expressed;
    }

    public void setInnovation(int innovation) {
        this.innovation = innovation;
    }

    public int getInnovation() {
        return this.innovation;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public ConnectionGene copy() {
        return new ConnectionGene(inNodeIndex, outNodeIndex, weight, expressed, innovation);
    }

    @Override
    public Component clone() {
        return copy();
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!expressed)
            return;
        else
            g2.setColor(Color.GREEN);

        if (debug) {
            g2.drawLine((int) inNode.x + 40, (int) inNode.y + 20, (int) outNode.x, (int) outNode.y + 20);
            g2.fillRect((int) outNode.x - 10, (int) outNode.y + 20, 10, 10);

            float centerX = inNode.x + Math.abs((outNode.x - inNode.x) / 2.0f);
            float centerY;
            if (outNode.y - inNode.y > 0)
                centerY = inNode.y + Math.abs((outNode.y - inNode.y) / 2.0f);
            else
                centerY = inNode.y - Math.abs((outNode.y - inNode.y) / 2.0f);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            g2.setColor(Color.WHITE);
            g2.drawString("" + weight, centerX, centerY);
            g2.drawString("" + innovation, centerX, centerY + 10);
        } else {
            g2.drawLine((int)inNode.x + 5, (int)inNode.y + 5, (int)outNode.x, (int)outNode.y + 5);
        }
    }

    @Override
    public int compareTo(ConnectionGene connectionGene) {
        if (connectionGene.getInnovation() == this.innovation) return 0;

        return this.innovation > connectionGene.getInnovation() ? 1 : -1;
    }
}
