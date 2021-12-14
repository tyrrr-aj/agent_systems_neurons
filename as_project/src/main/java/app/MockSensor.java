package app;

import sim.engine.SimState;
import sim.engine.Steppable;

public class MockSensor implements Steppable {
    private final BaseNeuronAgent output;
    private final double weight;

    public MockSensor(BaseNeuronAgent output, double weight) {
        this.output = output;
        this.weight = weight;
    }

    @Override
    public void step(SimState simState) {
        NeuralNetwork neuralNetwork = (NeuralNetwork) simState;
        output.startInputExcitation(this, weight, neuralNetwork);
    }
}
