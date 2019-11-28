package com.jade.dataStructures;


public class Pool {
    public int generation;
    public int currentSpecies;
    public int currentGenome;
    public int currentFrame;
    public float maxFitness;
    public InnovationGenerator innovationGenerator;

    public Pool() {
        this.generation = 0;
        this.currentSpecies = 0;
        this.currentGenome = 0;
        this.currentFrame = 0;
        this.maxFitness = 0.0f;
        this.innovationGenerator = new InnovationGenerator();
    }
}
