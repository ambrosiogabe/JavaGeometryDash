package com.jade.dataStructures;

import java.util.ArrayList;
import java.util.List;

public class InnovationGenerator {
    private int currentInnovation = 0;

    public List<ConnectionGene> innovationTracker;

    public InnovationGenerator() {
        innovationTracker = new ArrayList<ConnectionGene>();
    }

    public int getInnovation(ConnectionGene newCon) {
        for (ConnectionGene con : innovationTracker) {
            if (con.getInNode().getType() == newCon.getInNode().getType() && con.getInNode().getId() == newCon.getInNode().getId() &&
                con.getOutNode().getType() == newCon.getOutNode().getType() && con.getOutNode().getId() == newCon.getOutNode().getId()) {
                return con.getInnovation();
            }
        }
        innovationTracker.add(newCon);
        return currentInnovation++;
    }
}
