package com.jade.dataStructures;

import com.jade.components.Component;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Genome extends Component implements Comparable<Genome>, Runnable {
    private List<ConnectionGene> connections;
    private List<NodeGene> genes;
    private final float PROBABILITY_PERTURBING = 0.9f;
    private final float PROBABILITY_MUTATING = 0.8f;
    private final float MAX_PERTURBATION = 0.1f;
    private final float PROBABILITY_ADD_NEW_NODE = 0.03f;
    private final float PROBABILITY_ADD_CONNECTION = 0.05f;
    private static Random random = new Random();
    private int x, y;
    public int globalRank;
    private float fitness, adjustedFitness;
    private boolean debug = true;

    public boolean isReady = true;
    public List<NodeGene> outputs = new ArrayList<>();

    public Genome(int x, int y) {
        this.connections = new ArrayList<>();
        this.genes = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.fitness = 0.0f;
        this.adjustedFitness = 0.0f;
        this.globalRank = 0;
    }

    public Genome(int x, int y, float fitness, float adjustedFitness, int globalRank) {
        this.connections = new ArrayList<>();
        this.genes = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.fitness = fitness;
        this.adjustedFitness = adjustedFitness;
        this.globalRank = globalRank;
    }

    public static Genome basicGenome(List<NodeGene> inputs, List<NodeGene> output, InnovationGenerator innovationGenerator) {
        Genome genome = new Genome(0, 0);
        for (NodeGene gene : output) {
            genome.addNodeGene(gene);
        }

        for (NodeGene gene : inputs) {
            genome.addNodeGene(gene);
            for (NodeGene o : output) {
                genome.addConnectionGene(new ConnectionGene(gene, o, 0.0f, true, innovationGenerator));
            }
        }
        genome.mutate(innovationGenerator);

        return genome;
    }

    public Genome copy() {
        Genome newGene = new Genome(this.x, this.y, this.fitness, this.adjustedFitness, this.globalRank);
        for (NodeGene nodeGene : genes) {
            NodeGene newNodeGene = nodeGene.copy();
            newGene.addNodeGene(newNodeGene);
        }

        for (ConnectionGene con : connections) {
            ConnectionGene newCon = con.copy();
            newCon.setNodes(newGene.getNodeOfId(newCon.getInNodeIndex()), newGene.getNodeOfId(newCon.getOutNodeIndex()));
            newGene.addConnectionGene(newCon);
        }

        return newGene;
    }

    public void mutate(InnovationGenerator innovationGenerator) {
        for (ConnectionGene con : connections) {
            if (random.nextFloat() < PROBABILITY_MUTATING) { // 80% chance of being mutated
                if (random.nextFloat() < PROBABILITY_PERTURBING) { // 90% chance of being perturbed
                    con.setWeight(con.getWeight() + ((random.nextFloat() * 2 - 1f) * MAX_PERTURBATION));
                    if (con.getWeight() >= 1.0f) con.setWeight(1.0f);
                    else if (con.getWeight() <= 0.0f) con.setWeight(0.0f);
                } else { // 10% chance of random new weight
                    con.setWeight(random.nextFloat());
                }
            }
        }

        if (random.nextFloat() < PROBABILITY_ADD_NEW_NODE) {
            addNode(innovationGenerator);
        }

        if (random.nextFloat() < PROBABILITY_ADD_CONNECTION) {
            addConnection(innovationGenerator);
        }
    }

    public void addNode(InnovationGenerator innovation) {
        ConnectionGene conToSplit = connections.get(random.nextInt(connections.size()));

        NodeGene newGene = new NodeGene(NodeGene.Type.HIDDEN, genes.size(), 0.0f, conToSplit.getInNode().layer + 1);
        genes.add(newGene);

        if (conToSplit.getOutNode().layer - conToSplit.getInNode().layer < 2) {
            Stack<NodeGene> genesToVisit = new Stack<>();
            List<NodeGene> visited = new ArrayList<>();
            visited.add(conToSplit.getOutNode());
            genesToVisit.push(conToSplit.getOutNode());
            while (genesToVisit.size() > 0) {
                NodeGene current = genesToVisit.pop();
                current.layer++;

                for (ConnectionGene con : connections) {
                    if (con.getInNode() == current && !visited.contains(current)) {
                        visited.add(current);
                        genesToVisit.push(con.getInNode());
                    }
                }
            }
        }
        conToSplit.disable();

        connections.add(new ConnectionGene(conToSplit.getInNode(), newGene, 1.0f, true, innovation));
        connections.add(new ConnectionGene(newGene, conToSplit.getOutNode(), conToSplit.getWeight(), true, innovation));
    }

    public void addConnection(InnovationGenerator innovation) {
        boolean added = false;
        int numTries = 0;
        do {
            numTries++;
            NodeGene newInGene = genes.get(random.nextInt(genes.size()));
            NodeGene newOutGene;
            int otherNumTries = 0;
            do {
                otherNumTries++;
                newOutGene = genes.get(random.nextInt(genes.size()));
            } while (newOutGene == newInGene && otherNumTries < 5);
            if (otherNumTries >= 5) continue;

            boolean exists = false;
            boolean canConnect = true;
            for (ConnectionGene con : connections) {
                if ((newInGene == con.getInNode() || newInGene == con.getOutNode()) && (newOutGene == con.getInNode() || newOutGene == con.getOutNode())) {
                    exists = true;
                    break;
                }
                if (newInGene.getType() == newOutGene.getType() && (newInGene.getType() == NodeGene.Type.INPUT || newInGene.getType() == NodeGene.Type.OUTPUT)) {
                    canConnect = false;
                    break;
                }
                if ((newInGene.getType() == NodeGene.Type.OUTPUT && newOutGene.getType() == NodeGene.Type.INPUT) || (newInGene.getType() == NodeGene.Type.INPUT && newOutGene.getType() == NodeGene.Type.OUTPUT)) {
                    canConnect = false;
                    break;
                }
            }
            if (exists || !canConnect) continue;

            if (newInGene.layer > newOutGene.layer) {
                NodeGene tmp = newInGene;
                newInGene = newOutGene;
                newOutGene = tmp;
            }

            ConnectionGene newCon = new ConnectionGene(newInGene, newOutGene, random.nextFloat(), true, innovation);
            connections.add(newCon);
            added = true;
        } while (!added && numTries < 3);
    }

    public void evaluate() {
        isReady = false;
        List<NodeGene> outputs = new ArrayList<>();
        Collections.sort(genes);

        for (int i=0; i < genes.size(); i++) {
            NodeGene gene = genes.get(i);
            for (ConnectionGene con : connections) {
                if (con.isExpressed() && con.getInNode() == gene) {
                    con.feedForward();
                }
            }

            if (gene.getType() == NodeGene.Type.OUTPUT) {
                outputs.add(gene);
            }
        }

        isReady = true;
        this.outputs = outputs;
    }

    public static int hasInnovationIsExpressed(Genome genome, int innovation) {
        for (ConnectionGene con : genome.getConnectionGenes()) {
            if (con.getInnovation() == innovation) {
                if (con.isExpressed()) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }

    public static ConnectionGene getInnovation(Genome genome, int innovation) {
        for (ConnectionGene con : genome.getConnectionGenes()) {
            if (con.getInnovation() == innovation) {
                return con;
            }
        }
        return null;
    }

    public boolean hasNodeOfId(int id) {
        for (NodeGene node : genes) {
            if (node.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public NodeGene getNodeOfId(int id) {
        for (NodeGene node : genes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public ConnectionGene addAndWireConnectionGene(ConnectionGene connection) {
        if (!hasNodeOfId(connection.getInNode().getId())) addNodeGene(connection.getInNode().copy());
        if (!hasNodeOfId(connection.getOutNode().getId())) addNodeGene(connection.getOutNode().copy());

        ConnectionGene newCon = connection.copy();
        newCon.setNodes(getNodeOfId(connection.getInNode().getId()), getNodeOfId(connection.getOutNode().getId()));
        addConnectionGene(newCon);
        return newCon;
    }

    public static Genome makeBaby(Genome genome1, Genome genome2) {
        Genome baby = new Genome(0, 0);
        List<ConnectionGene> allGenomes = new ArrayList<>(genome1.getConnectionGenes());
        List<NodeGene> allNodes = new ArrayList<>(genome1.getNodeGenes());
        for (ConnectionGene gene : genome2.getConnectionGenes()) {
            if (!allGenomes.contains(gene)) {
                allGenomes.add(gene);
            }
        }
        for (NodeGene gene : genome2.getNodeGenes()) {
            if (!allNodes.contains(gene)) {
                allNodes.add(gene);
            }
        }

        for (ConnectionGene con : allGenomes) {
            int gene1HasExpressed = hasInnovationIsExpressed(genome1, con.getInnovation());
            int gene2HasExpressed = hasInnovationIsExpressed(genome2, con.getInnovation());
            boolean bothHave = gene1HasExpressed > 0 && gene2HasExpressed > 0;
            boolean bothDisabled = gene1HasExpressed == 1 && gene2HasExpressed == 1;
            boolean bothEnabled = gene1HasExpressed == 2 && gene2HasExpressed == 2;

            if (bothHave && bothEnabled) { // 100% chance to add it
                baby.addAndWireConnectionGene(con);
            } else if (bothHave && bothDisabled) { // 100% chance to add it, 100% chance it is disabled
                ConnectionGene newCon = baby.addAndWireConnectionGene(con);
                newCon.disable();
            } else if (bothHave) { // One is enabled, one is disabled
                ConnectionGene newCon = baby.addAndWireConnectionGene(con);
                if (Genome.random.nextFloat() > 0.75) {
                    newCon.disable();
                } else {
                    newCon.enable();
                }
            } else {
                if (genome1.getFitness() == genome2.getFitness()) { // If they have the same fitness, randomly assign excess/disjoint gene to child
                    if (Genome.random.nextBoolean()) {
                        baby.addAndWireConnectionGene(con);
                    }
                } else if (genome1.getFitness() > genome2.getFitness()) { // If gene1 has this connection, and is stronger, then add conection to child
                    if (gene1HasExpressed > 0) { // Gene1 was part of the stronger parent
                        baby.addAndWireConnectionGene(con);
                    }
                } else if (genome1.getFitness() < genome2.getFitness()) { // If gene2 has this connection, and is stronger, then add connection to child
                    if (gene2HasExpressed > 0) { // Gene2 was part of the stronger parent
                        baby.addAndWireConnectionGene(con);
                    }
                }
            }
        }

        return baby;
    }

    public static float compatibilityDistance(Genome genome1, Genome genome2, float c1, float c2, float c3) {
        int excessGenes = countExcessGenes(genome1, genome2);
        int disjointGenes = countDisjointGenes(genome1, genome2);
        float weightDifferences = averageWeightDifferences(genome1, genome2);

        int highestNumberOfGenes = Math.max(genome1.getNodeGenes().size(), genome2.getNodeGenes().size());
        if (highestNumberOfGenes < 20) {
            highestNumberOfGenes = 1;
        }

        return ((c1 * excessGenes) / highestNumberOfGenes) + ((c2 * disjointGenes) / highestNumberOfGenes) +
                (c3 * weightDifferences);
    }

    public static float averageWeightDifferences(Genome genome1, Genome genome2) {
        float weightDifferences = 0.0f;
        int matchingGenes = 0;

        List<ConnectionGene> allGenomes = new ArrayList<>(genome1.getConnectionGenes());
        List<NodeGene> allNodes = new ArrayList<>(genome1.getNodeGenes());
        for (ConnectionGene gene : genome2.getConnectionGenes()) {
            if (!allGenomes.contains(gene)) {
                allGenomes.add(gene);
            }
        }
        for (NodeGene gene : genome2.getNodeGenes()) {
            if (!allNodes.contains(gene)) {
                allNodes.add(gene);
            }
        }

        for (ConnectionGene con : allGenomes) {
            ConnectionGene con1 = getInnovation(genome1, con.getInnovation());
            ConnectionGene con2 = getInnovation(genome2, con.getInnovation());

            if (con1 != null && con2 != null && con1.getInnovation() == con2.getInnovation()) {
                float difference = Math.abs(con1.getWeight() - con2.getWeight());
                weightDifferences += difference;
                matchingGenes++;
            }
        }

        if (matchingGenes == 0) {
            return 0;
        } else {
            return weightDifferences / (float)matchingGenes;
        }
    }

    public static int countDisjointGenes(Genome genome1, Genome genome2) {
        List<ConnectionGene> genome1List = genome1.getConnectionGenes();
        List<ConnectionGene> genome2List = genome2.getConnectionGenes();
        Collections.sort(genome1List);
        Collections.sort(genome2List);
        Set<Integer> genome1Set = new HashSet<>();
        Set<Integer> genome2Set = new HashSet<>();

        for (ConnectionGene con : genome1List) {
            genome1Set.add(con.getInnovation());
        }
        for (ConnectionGene con : genome2List) {
            genome2Set.add(con.getInnovation());
        }

        int genome1LastInno = genome1List.get(genome1List.size() - 1).getInnovation();
        int genome2LastInno = genome2List.get(genome2List.size() - 1).getInnovation();
        int excess = Math.abs(genome1LastInno - genome2LastInno);
        int highestInno = Math.max(genome1LastInno, genome2LastInno) + 1;

        genome1Set.retainAll(genome2Set);
        return Math.max(highestInno - genome1Set.size() - excess, 0);
    }

    public static int countExcessGenes(Genome genome1, Genome genome2) {
        List<ConnectionGene> genome1List = genome1.getConnectionGenes();
        List<ConnectionGene> genome2List = genome2.getConnectionGenes();
        Collections.sort(genome1List);
        Collections.sort(genome2List);
        int genome1LastInno = genome1List.get(genome1List.size() - 1).getInnovation();
        int genome2LastInno = genome2List.get(genome2List.size() - 1).getInnovation();
        return Math.abs(genome1LastInno - genome2LastInno);
    }

    @Override
    public Component clone() {
        return null;
    }

    @Override
    public void draw(Graphics2D g2) {
        Collections.sort(genes);

        if (debug) {
            int currentLayer = 0;
            int currentY = 100;
            for (int i = 0; i < genes.size(); i++) {
                if (currentLayer != genes.get(i).layer) {
                    currentLayer = genes.get(i).layer;
                    if (currentLayer % 2 != 0)
                        currentY = this.y + 100 + (currentLayer * 20);
                    else
                        currentY = this.y + 100;
                }

                genes.get(i).x = this.x + (currentLayer * 150) + 50;
                genes.get(i).y = currentY;
                currentY += 100;
            }

            for (ConnectionGene con : connections) {
                if (!con.isExpressed()) continue;
                con.draw(g2);
            }

            for (NodeGene gene : genes) {
                gene.draw(g2);
            }
        } else {
            int currentLayer = 0;
            int currentY = 300;
            for (int i = 0; i < genes.size(); i++) {
                if (currentLayer != genes.get(i).layer) {
                    currentLayer = genes.get(i).layer;
                    if (currentLayer % 2 != 0)
                        currentY = this.y + 300 + (currentLayer * 20);
                    else
                        currentY = this.y + 300;
                }
                genes.get(i).x = this.x + (currentLayer * 20) + 200;
                genes.get(i).y = currentY;
                currentY += 20;
            }
        }
    }

    public List<NodeGene> getInputs() {
        List<NodeGene> inputs = new ArrayList<>();
        Collections.sort(genes);

        for (NodeGene gene : genes) {
            if (gene.getType() == NodeGene.Type.INPUT) {
                inputs.add(gene);
            }
        }

        return inputs;
    }

    public void addNodeGene(NodeGene gene) { genes.add(gene); }
    public void addConnectionGene(ConnectionGene connection) { connections.add(connection); }
    public List<NodeGene> getNodeGenes() { return genes; }
    public List<ConnectionGene> getConnectionGenes() { return connections; }
    public float getFitness() { return this.fitness; }
    public void setFitness(float val) { this.fitness = val; }
    public void printOrderedByInnovation() {
        Collections.sort(connections);
        for (ConnectionGene con : connections) {
            System.out.printf("=========");
        }
        System.out.println();
        for (ConnectionGene con : connections) {
            System.out.printf("|%4d   |", con.getInnovation());
        }
        System.out.println();
        for (ConnectionGene con : connections) {
            System.out.printf("|%2d → %-2d|", con.getInNode().getId() + 1, con.getOutNode().getId() + 1);
        }
        System.out.println();
        for (ConnectionGene con : connections) {
            if (!con.isExpressed()) {
                System.out.printf("| %-6s|", "DISAB");
            } else {
                System.out.printf("|%7s|", " ");
            }
        }
        System.out.println();
        for (ConnectionGene con : connections) {
            System.out.printf("=========");
        }
        System.out.println();
    }

    @Override
    public int compareTo(Genome genome) {
        if (this.fitness == genome.fitness) return 0;

        return this.fitness > genome.fitness ? 1 : -1;
    }

    private float sigmoid(float x) {
        return (float)(1 / (1 + Math.exp(-4.9 * x)));
    }

    @Override
    public void run() {
        evaluate();
    }
}
