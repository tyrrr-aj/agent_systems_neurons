package app;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;
import sim.util.Double2D;

public class Sensor implements Steppable {
    private final ReceptoryField receptoryField;
    private ReceptoryNeuron associatedReceptoryNeuron;

    private boolean isStimulated = false;

    public Sensor(NeuralNetwork neuralNetwork, ReceptoryField receptoryField, Double2D position, Double2D receptoryNeuronsVec) {
        this.receptoryField = receptoryField;
        neuralNetwork.brain.setObjectLocation(this, position);
        associatedReceptoryNeuron = new ReceptoryNeuron("receptory", receptoryField, neuralNetwork);
        neuralNetwork.brain.setObjectLocation(associatedReceptoryNeuron, position.add(receptoryNeuronsVec.multiply(5.0)));
        neuralNetwork.network.addNode(this);
        neuralNetwork.network.addNode(associatedReceptoryNeuron);
        Edge edge = new Edge(this, associatedReceptoryNeuron, null);
        neuralNetwork.network.addEdge(edge);
    }

    @Override
    public void step(SimState simState) {
        NeuralNetwork neuralNetwork = (NeuralNetwork) simState;

        if (isStimulated) {
            double distance = receptoryField.getPosition(neuralNetwork).manhattanDistance(getPosition(neuralNetwork));
            double stimulation;
            if (receptoryField.getValueRange() > 0.0) {
                stimulation = 1.0 - distance / receptoryField.getSpatialRange();
            } else {
                stimulation = 1.0;
            }
            associatedReceptoryNeuron.startInputExcitation(this, stimulation, simState);
        }
        else {
            associatedReceptoryNeuron.stopInputExcitation(this, simState);
        }
    }

    public void startStimulation(SimState simState) {
        isStimulated = true;
        simState.schedule.scheduleOnceIn(0.0, this);
    }

    public void stopStimulation(SimState simState) {
        isStimulated = false;
        simState.schedule.scheduleOnceIn(0.0, this);
    }

    public Double2D getPosition(NeuralNetwork neuralNetwork) {
        return neuralNetwork.brain.getObjectLocation(this);
    }

    public ReceptoryField getReceptoryField() {
        return receptoryField;
    }

    public boolean isStimulated() {
        return isStimulated;
    }
}
