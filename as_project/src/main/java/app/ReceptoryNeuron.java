package app;

import sim.field.network.Edge;
import sim.util.Double2D;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReceptoryNeuron extends BaseNeuronAgent {
    private final ReceptoryField receptoryField;

    public ReceptoryNeuron(String name, ReceptoryField receptoryField, NeuralNetwork neuralNetwork, Double2D position) {
        super(name);
        this.receptoryField = receptoryField;

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
