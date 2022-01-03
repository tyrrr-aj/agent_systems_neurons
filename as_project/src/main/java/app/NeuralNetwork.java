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

        ReceptoryField leftReceptoryField = new ReceptoryField(5.0, true, true,5.0, 1, 99);
        scheduleRandomStimulation(leftReceptoryField, 1000);

        ReceptoryField rightReceptoryField = new ReceptoryField(5.0, true, false,95.0, 1, 99);
        scheduleRandomStimulation(rightReceptoryField, 1000);

//        BaseNeuronAgent first = null;
//        BaseNeuronAgent previous = null;
//        for (int i = 0; i < neuronsNumber; i++) {
//            BaseNeuronAgent neuron = new BaseNeuronAgent("Neuron " + i);
//            brain.setObjectLocation(neuron, neuronLocation(brain, i));
//            network.addNode(neuron);
//            if (previous != null) {
//                Edge edge = new Edge(previous, neuron, null);
//                edge.setWeight(weight);
//                network.addEdge(edge);
//                previous.addNeighbour(neuron);
//            }
//            else {
//                first = neuron;
//            }
//            previous = neuron;
//        }
//
//        MockSensor mockSensor = new MockSensor(first, 1.0);
//        schedule.scheduleOnce(mockSensor);
//        schedule.scheduleOnceIn(externalStimulationTime, mockSensor);
    }

    private Double2D neuronLocation(Continuous2D brain, int neuronIndex) {
        double unit = brain.getWidth() / (neuronsNumber + 1);
        double x = (neuronIndex + 1) * unit;
        double y = brain.getHeight() / 2;
        return new Double2D(x, y);
    }

    private void scheduleRandomStimulation(ReceptoryField receptoryField, double totalTime) {
        for (double time = 0.0; time < totalTime; time += random.nextDouble() * 15) {
            receptoryField.scheduleStartStimulation(random.nextDouble(), this, time);
            time += random.nextDouble() * 15;
            receptoryField.scheduleStopStimulation(this, 8.0);
        }
    }

    public static void main(String[] args) {
        doLoop(NeuralNetwork.class, args);
        System.exit(0);
    }

    public double[] getExcitationDistribution(){
        Bag neurons = network.getAllNodes();
        double[] distribution = new double[neurons.numObjs];
        for (int i=0; i<neurons.numObjs; i++){
            if (neurons.objs[i] instanceof BaseNeuronAgent) {
                distribution[i] = ((BaseNeuronAgent) (neurons.objs[i])).getExcitation();
            }
            else if (neurons.objs[i] instanceof Sensor) {
                distribution[i] = ((Sensor) (neurons.objs[i])).isStimulated() ? 1.0 : 0.0;
            }
            else if (neurons.objs[i] instanceof ReceptoryField) {
                distribution[i] = ((ReceptoryField) (neurons.objs[i])).isStimulated() ? 1.0 : 0.0;
            }
        }
        return distribution;
    }
}
