package app;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Double2D;


public class NeuralNetwork extends SimState {
    public Continuous2D brain = new Continuous2D(1.0, 100, 100);
    public Network network = new Network(true);

    private final int neuronsNumber = 4;
    private final double weight = 0.5;
    
    public NeuralNetwork(long seed) {
        super(seed);
    }

    public void start() {
        super.start();

        BaseNeuronAgent first = null;
        BaseNeuronAgent previous = null;
        for (int i = 0; i < neuronsNumber; i++) {
            BaseNeuronAgent neuron = new BaseNeuronAgent();
            brain.setObjectLocation(neuron, neuronLocation(brain, i));
            network.addNode(neuron);
            if (previous != null) {
                Edge edge = new Edge(previous, neuron, null);
                edge.setWeight(weight);
                network.addEdge(edge);

            }
            else {
                first = neuron;
            }
            previous = neuron;
        }

        MockSensor mockSensor = new MockSensor(first, 1.0);
        schedule.scheduleOnce(mockSensor);
    }

    private Double2D neuronLocation(Continuous2D brain, int neuronIndex) {
        double unit = brain.getWidth() / (neuronsNumber + 1);
        double x = (neuronIndex + 1) * unit;
        double y = brain.getHeight() / 2;
        return new Double2D(x, y);
    }

    public static void main(String[] args) {
        doLoop(NeuralNetwork.class, args);
        System.exit(0);
    }
}
