package app;

import sim.engine.SimState;
import sim.engine.Steppable;

public class BaseNeuronAgent implements Steppable {
    @Override
    public void step(SimState simState) {
        System.out.printf("Neuron fired at %f%n", simState.schedule.getTime());
    }

    public void startInputExcitation(Object input, double weight, NeuralNetwork neuralNetwork) {
        neuralNetwork.schedule.scheduleOnceIn(1.0, this);
    }
}
