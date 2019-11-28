package com.jade.dataStructures;

import com.jade.main.Constants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SpecieManager {
    InnovationGenerator innovationGenerator = new InnovationGenerator();
    public final float C1 = 1.0f;
    public final float C2 = 1.0f;
    public final float C3 = 0.4f;
    public final float DELTA_T = 3.0f;
    public final float CROSSOVER_CHANCE = 0.75f;
    public final int MAX_STALENESS = 15;
    public final int POPULATION_SIZE = 150;

    public Pool pool;
    private Random random = new Random();

    public List<Species> species = new ArrayList<>();

    public SpecieManager(int inputSize, int outputSize) {
        List<NodeGene> inputs = new ArrayList<>();
        List<NodeGene> outputs = new ArrayList<>();
        int globalCounter = 0;
        for (int i=0; i < inputSize; i++) {
            inputs.add(new NodeGene(NodeGene.Type.INPUT, globalCounter, 0.0f, 0));
            globalCounter++;
        }

        for (int i=0; i < outputSize; i++) {
            outputs.add(new NodeGene(NodeGene.Type.OUTPUT, globalCounter, 0.0f, 1));
            globalCounter++;
        }

        initialize(inputs, outputs);
    }

    public Genome breedChild(Species species) {
        Genome child;
        if (random.nextFloat() < CROSSOVER_CHANCE) {
            Genome g1 = species.population.get(random.nextInt(species.population.size()));
            Genome g2 = species.population.get(random.nextInt(species.population.size()));
            child = Genome.makeBaby(g1, g2);
        } else {
            Genome g = species.population.get(random.nextInt(species.population.size()));
            child = g.copy();
        }

        child.mutate(innovationGenerator);

        return child;
    }

    public void newGeneration() {
        cullSpecies(false);
        //System.out.println("1: Size: " + species.size());
        rankGlobally();
        removeStaleSpecies();
        //System.out.println("2: Size: " + species.size());
        rankGlobally();
        removeWeakSpecies();
        //System.out.println("3: Size: " + species.size());
        //System.out.println();

        float sum = totalAverageFitness();
        List<Genome> children = new ArrayList<>();
        for (Species s : species) {
            double breed = Math.floor(s.getAverageFitness() / sum * s.population.size()) - 1;
            for (int i=0; i < breed; i++) {
                children.add(breedChild(s));
            }
        }
        cullSpecies(true);
        if (species.size() == 0) {
            System.out.println("No increase in fitness for all species.");
            System.exit(-1);
        }

        while (children.size() + species.size() < POPULATION_SIZE) {
            Species s = species.get(random.nextInt(species.size()));
            children.add(breedChild(s));
        }

        for (Genome child : children) {
            addToSpecies(child);
        }

        pool.generation++;
    }

    public void initialize(List<NodeGene> inputs, List<NodeGene> outputs) {
        pool = new Pool();

        for(int i=0; i < POPULATION_SIZE; i++) {
            Genome basic = Genome.basicGenome(inputs, outputs, pool.innovationGenerator);
            addToSpecies(basic);
        }
    }

    public Genome nextGenome() {
        if (species.get(pool.currentSpecies).population.get(pool.currentGenome).getFitness() > pool.maxFitness) {
            pool.maxFitness = species.get(pool.currentSpecies).population.get(pool.currentGenome).getFitness();
        }

        if (pool.currentGenome + 1 < species.get(pool.currentSpecies).population.size()) {
            pool.currentGenome++;
            species.get(pool.currentSpecies).population.get(pool.currentGenome).run();
            return species.get(pool.currentSpecies).population.get(pool.currentGenome);
        }

        pool.currentSpecies++;
        if (pool.currentSpecies < species.size()) {
            pool.currentGenome = 0;
            species.get(pool.currentSpecies).population.get(pool.currentGenome).run();
            return species.get(pool.currentSpecies).population.get(pool.currentGenome);
        }

        newGeneration();
        pool.currentSpecies = 0;
        pool.currentGenome = 0;
        return nextGenome();
    }

    public void removeStaleSpecies() {
        List<Species> survived = new ArrayList<>();
        Collections.sort(species);

        int i = 1;
        for (Species currentSpecie : species) {
            currentSpecie.sortByFitnessDesc();

            if (currentSpecie.population.get(0).getFitness() > currentSpecie.topFitness) {
                currentSpecie.topFitness = currentSpecie.population.get(0).getFitness();
                currentSpecie.staleness = 0;
            } else {
                currentSpecie.staleness++;
            }

            if (currentSpecie.staleness < MAX_STALENESS) {
                survived.add(currentSpecie);
            } else if (i < 3) {
                survived.add(currentSpecie);
            }
            i++;
        }

        species = survived;
    }

    public void removeWeakSpecies() {
        List<Species> survived = new ArrayList<>();

        float sum = totalAverageFitness();
        for (Species s : species) {
            double breed = Math.floor(s.getAverageFitness() / sum * s.population.size());
            if (breed >= 1.0) {
                survived.add(s);
            }
        }

        species = survived;
    }

    public void addToSpecies(Genome child) {
        for (Species s : species) {
            if (sameSpecies(s.population.get(0), child)) {
                s.population.add(child);
                return;
            }
        }

        Species childSpecies = new Species(child);
        species.add(childSpecies);
    }

    public void cullSpecies(boolean cutToOne) {
        for (Species s : species) {
            s.sortByFitnessAsc();

            double remaining = Math.ceil(s.population.size() / 2.0);
            if (cutToOne) remaining = 1.0;
            while (s.population.size() > remaining) {
                s.population.remove(0);
            }
        }
    }

    private float totalAverageFitness() {
        float sum = 0.0f;
        for (Species s : species) {
            sum += s.getAverageFitness();
        }
        return sum;
    }

    private boolean sameSpecies(Genome g1, Genome g2) {
        float similarity = Genome.compatibilityDistance(g1, g2, C1, C2, C3);
        return similarity < DELTA_T;
    }

    private void rankGlobally() {
        List<Genome> global = new ArrayList<>();
        for (Species s : species) {
            global.addAll(s.population);
        }
        Collections.sort(global);

        for (int i=0; i < global.size(); i++) {
            global.get(i).globalRank = i;
        }
    }

    public void draw(Graphics2D g2, int fitness, float evaluation) {
        g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.7f));
        g2.fillRect(760, 0, Constants.SCREEN_WIDTH, 220);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        g2.drawString("Generation: " + pool.generation, 800, 165);
        g2.drawString("Species: " + pool.currentSpecies, 800, 185);
        g2.drawString("Genome: " + pool.currentGenome, 800, 205);
        g2.drawString("Fitness: " + fitness, 900, 165);
        g2.drawString("Max Fitness: " + (int)(pool.maxFitness * 100), 900, 185);
        g2.drawString("Eval: " + evaluation, 900, 205);
    }
}
