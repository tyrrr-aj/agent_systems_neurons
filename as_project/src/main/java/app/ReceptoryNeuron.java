package app;

import sim.field.network.Edge;
import sim.util.Double2D;

import java.util.*;
import java.util.stream.Collectors;

public class ReceptoryNeuron extends BaseNeuronAgent {
    private final ReceptoryField receptoryField;
    private final double representedValue;

    private final Map<ObjectNeuron, Integer> objectNeuronsConnections = new HashMap<>();

    public ReceptoryNeuron(String name, ReceptoryField receptoryField, NeuralNetwork neuralNetwork, Double2D position, double representedValue) {
        super(name);
        this.receptoryField = receptoryField;
        this.representedValue = representedValue;

        neuralNetwork.brain.setObjectLocation(this, position);
        updateNeighbourhood(neuralNetwork);
    }

    private void updateNeighbourhood(NeuralNetwork neuralNetwork) {
        List<ReceptoryNeuron> neuronsInReceptoryField = Arrays.stream(neuralNetwork.brain.allObjects.toArray())
                .filter(neuron -> neuron instanceof ReceptoryNeuron)
                .map(neuron -> (ReceptoryNeuron) neuron)
                .filter(neuron -> neuron.receptoryField == receptoryField)
                .filter(neuron -> neuron != this)
                .collect(Collectors.toList());

        Double2D position = neuralNetwork.brain.getObjectLocation(this);

        ReceptoryNeuron closestNeigh = neuronsInReceptoryField
                .stream()
                .min(Comparator.comparing(neuron -> position.manhattanDistance(neuralNetwork.brain.getObjectLocation(neuron))))
                .orElse(null);

        if (closestNeigh != null) {
            Double2D closestNeighPosition = neuralNetwork.brain.getObjectLocation(closestNeigh);

            ReceptoryNeuron otherSideNeigh = neuronsInReceptoryField
                    .stream()
                    .filter(neuron -> {
                        Double2D neuronPosition = neuralNetwork.brain.getObjectLocation(neuron);
                        return position.manhattanDistance(neuronPosition) < closestNeighPosition.manhattanDistance(neuronPosition);
                    })
                    .min(Comparator.comparing(neuron -> position.manhattanDistance(neuralNetwork.brain.getObjectLocation(neuron))))
                    .orElse(null);

            if (otherSideNeigh != null) {
                Edge oldEdge = neuralNetwork.network.getEdge(closestNeigh, otherSideNeigh);
                Edge oldEdgeReverse = neuralNetwork.network.getEdge(otherSideNeigh, closestNeigh);
                neuralNetwork.network.removeEdge(oldEdge);
                neuralNetwork.network.removeEdge(oldEdgeReverse);

                addTwoWayEdge(neuralNetwork, this, otherSideNeigh);
            }

            addTwoWayEdge(neuralNetwork, this, closestNeigh);
        }
    }

    public ReceptoryField getReceptoryField() {
        return receptoryField;
    }

    public double getRepresentedValue() {
        return representedValue;
    }

    public double addNeighbouringONAndGetWeightToON(ObjectNeuron objectNeuron, NeuralNetwork neuralNetwork) {
        if (objectNeuronsConnections.containsKey(objectNeuron)) {
            int nConnections = objectNeuronsConnections.get(objectNeuron) + 1;
            objectNeuronsConnections.replace(objectNeuron, nConnections);
        }
        else {
            objectNeuronsConnections.put(objectNeuron, 1);
            addNeighbour(objectNeuron);
        }

        for (ObjectNeuron neigh: objectNeuronsConnections.keySet()) {
            Edge edge = neuralNetwork.network.getEdge(this, neigh);
            if (edge != null) {
                edge.setWeight((double) objectNeuronsConnections.get(neigh) / objectNeuronsConnections.size());
            }
        }

        return (double) objectNeuronsConnections.get(objectNeuron) / objectNeuronsConnections.size();
    }

    @Override
    protected void recordActivation(NeuralNetwork neuralNetwork) {
        neuralNetwork.currentStimulation.recordActivation(this, neuralNetwork);
    }

    private double getWeight(NeuralNetwork neuralNetwork, ReceptoryNeuron first, ReceptoryNeuron second) {
        Double2D firstPosition = neuralNetwork.brain.getObjectLocation(first);
        Double2D secondPosition = neuralNetwork.brain.getObjectLocation(second);

        return 1 - firstPosition.manhattanDistance(secondPosition) / receptoryField.getSpatialRange();
    }

    private void addTwoWayEdge(NeuralNetwork neuralNetwork, ReceptoryNeuron first, ReceptoryNeuron second) {
        Edge newEdgeClosest = new Edge(first, second, null);
        Edge newEdgeClosestReverse = new Edge(second, first, null);

        double weight = getWeight(neuralNetwork, first, second);
        newEdgeClosest.setWeight(weight);
        newEdgeClosestReverse.setWeight(weight);

        neuralNetwork.network.addEdge(newEdgeClosest);
        neuralNetwork.network.addEdge(newEdgeClosestReverse);
    }
}
