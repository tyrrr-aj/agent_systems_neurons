package app;

import sim.field.network.Edge;
import sim.util.Double2D;

import java.util.Collection;

public class ObjectNeuron extends BaseNeuronAgent{
    public ObjectNeuron(String name, NeuralNetwork neuralNetwork, Collection<ReceptoryNeuron> neighbours) {
        super(name);

        for (ReceptoryNeuron receptoryNeuron: neighbours) {
            Edge onToRnEdge = new Edge(this, receptoryNeuron, null);
            Edge rnToOnEdge = new Edge(receptoryNeuron, this, null);

            onToRnEdge.setWeight(1.0);
            rnToOnEdge.setWeight(receptoryNeuron.addNeighbouringONAndGetWeightToON(this, neuralNetwork));

            neuralNetwork.network.addEdge(onToRnEdge);
            neuralNetwork.network.addEdge(rnToOnEdge);

            addNeighbour(receptoryNeuron);
        }
    }
}
