package app;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;


public class NeuralNetwork extends SimState {
    public Continuous2D brain = new Continuous2D(1.0, 100, 100);
    public Network network = new Network(true);

    private final int neuronsNumber = 4;
    private final double weight = 0.5;
    private final double externalStimulationTime = 500.0;
    
    public NeuralNetwork(long seed) {
        super(seed);
    }

    public void start() {
        super.start();

        BaseNeuronAgent first = null;
        BaseNeuronAgent previous = null;
        for (int i = 0; i < neuronsNumber; i++) {
            BaseNeuronAgent neuron = new BaseNeuronAgent("Neuron " + i);
            brain.setObjectLocation(neuron, neuronLocation(brain, i));
            network.addNode(neuron);
            if (previous != null) {
                Edge edge = new Edge(previous, neuron, null);
                edge.setWeight(weight);
                network.addEdge(edge);
                previous.addNeighbour(neuron);
            }
            else {
                first = neuron;
            }
            previous = neuron;
        }

        MockSensor mockSensor = new MockSensor(first, 1.0);
        schedule.scheduleOnce(mockSensor);
        schedule.scheduleOnceIn(externalStimulationTime, mockSensor);
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

    public double[] getExcitationDistribution(){
        Bag neurons = network.getAllNodes();
        double[] distribution = new double[neurons.numObjs];
        for (int i=0; i<neurons.numObjs; i++){
            distribution[i] = ((BaseNeuronAgent)(neurons.objs[i])).getExcitation();
        }
        return distribution;
    }
}
