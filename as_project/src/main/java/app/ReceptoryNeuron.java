package app;

import sim.util.Bag;

public class ReceptoryNeuron extends BaseNeuronAgent {
    private final ReceptoryField receptoryField;

    public ReceptoryNeuron(String name, ReceptoryField receptoryField, NeuralNetwork neuralNetwork) {
        super(name);
        this.receptoryField = receptoryField;

        updateNeighbourhood(neuralNetwork);
    }

    private void updateNeighbourhood(NeuralNetwork neuralNetwork) {

    }
}
