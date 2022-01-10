package app;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.TentativeStep;
import sim.util.Double2D;

import java.util.HashMap;
import java.util.Map;


public class ReceptorsStimulation implements Steppable {
    private final NeuralNetwork neuralNetwork;
    private final double timeStart;
    private final double timeEnd;

    private boolean isFirstRun = true;

    private final Map<ReceptoryField, ReceptoryNeuron> activatedRNs = new HashMap<>();
    private boolean hasONBeenActivated = false;

    public ReceptorsStimulation(NeuralNetwork neuralNetwork, double timeStart, double timeEnd) {
        this.neuralNetwork = neuralNetwork;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;

        neuralNetwork.schedule.scheduleOnce(timeStart, this);
        neuralNetwork.schedule.scheduleOnce(timeStart + Constants.OBJECT_NODE_CHECK_TIME, this);
    }

    public void addReceptedValue(ReceptoryField receptoryField, double value) {
        receptoryField.scheduleStartStimulation(value, neuralNetwork, timeStart);
        receptoryField.scheduleStopStimulation(neuralNetwork, timeEnd);
    }

    public boolean recordActivation(BaseNeuronAgent neuron) {
        if (neuron instanceof ObjectNeuron) {
            hasONBeenActivated = true;
            return true;
        }
        else if (neuron instanceof ReceptoryNeuron receptoryNeuron) {
            if (!activatedRNs.containsKey(receptoryNeuron.getReceptoryField())) {
                activatedRNs.put(receptoryNeuron.getReceptoryField(), receptoryNeuron);
                return true;
            }
        }
        return false;
    }

    @Override
    public void step(SimState simState) {
        NeuralNetwork neuralNetwork = (NeuralNetwork) simState;

        if (isFirstRun) {
            neuralNetwork.currentStimulation = this;
            isFirstRun = false;
        }
        else {
            if (!hasONBeenActivated) {
                createON(neuralNetwork);
            }
        }
    }

    private void createON(NeuralNetwork neuralNetwork) {
        if (activatedRNs.size() < 2) {
            if (neuralNetwork.schedule.getTime() < timeEnd) {
                neuralNetwork.schedule.scheduleOnceIn(Constants.OBJECT_NODE_CHECK_TIME, this);
            }
            return;
        }

        Double2D position = activatedRNs
            .values()
            .stream()
            .map(neuron -> neuralNetwork.brain.getObjectLocation(neuron))
            .reduce(Double2D::add)
            .get()
            .multiply(1.0 / activatedRNs.size());

        ObjectNeuron objectNeuron = new ObjectNeuron("objectNeuron", neuralNetwork, activatedRNs.values());
        neuralNetwork.brain.setObjectLocation(objectNeuron, position);
    }
}
