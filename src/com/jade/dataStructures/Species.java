package com.jade.dataStructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Species implements Comparable<Species> {
    public int staleness;

    public List<Genome> population;
    public Genome representative;
    public float topFitness = 0.0f;

    public Species(Genome representative) {
        population = new ArrayList<>();
        staleness = 0;
        population.add(representative);
        this.representative = representative;
    }

    public void sortByFitnessDesc() {
        Collections.sort(population, Collections.reverseOrder());
    }

    public void sortByFitnessAsc() {
        Collections.sort(population);
    }

    public float getAverageFitness() {
        float sum = 0.0f;
        for (Genome g : population) {
            sum += g.getFitness();
        }

        return sum / population.size();
    }

    @Override
    public int compareTo(Species species) {
        if (this.getAverageFitness() == species.getAverageFitness()) return 0;

        return this.getAverageFitness() > species.getAverageFitness() ? 1 : -1;
    }
}
