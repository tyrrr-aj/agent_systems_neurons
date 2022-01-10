package app;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.TentativeStep;

import java.text.DecimalFormat;
import java.util.*;

public abstract class BaseNeuronAgent implements Steppable {
    private List<BaseNeuronAgent> neighbours;
    private HashMap<Object, Pair<Double, Double>> currentInputExcitations; //left weight, right startTime
    private NeuronState state;
    private double excitation;
    private Optional<Double> lastUpdateTime;
    private Optional<TentativeStep> nearestActivation;

    private final String name;

    public BaseNeuronAgent(String name) {
        neighbours = new ArrayList<>();
        currentInputExcitations = new HashMap<>();
        state = NeuronState.REGULAR;
        excitation = 0.0;
        lastUpdateTime = Optional.empty();
        nearestActivation = Optional.empty();
        this.name = name;
    }

    public void addNeighbour(BaseNeuronAgent neighbour) {
        neighbours.add(neighbour);
    }

    @Override
    public void step(SimState simState) {
        NeuralNetwork neuralNetwork = (NeuralNetwork) simState;

        if (state == NeuronState.REGULAR) {
            for (BaseNeuronAgent neigh : neighbours) {
                double weight = ((NeuralNetwork)simState).network.getEdge(this, neigh).getWeight();
                neigh.startInputExcitation(this, weight, simState);
            }
            state = NeuronState.ACTIVATED;
            excitation = Constants.THRESHOLD;
            simState.schedule.scheduleOnceIn(Constants.ACTIVATION_TIME, this);
            recordActivation(neuralNetwork);
        } else if (state == NeuronState.ACTIVATED) {
            for (BaseNeuronAgent neigh : neighbours) {
                neigh.stopInputExcitation(this, simState);
            }
            state = NeuronState.REFRACTED;
            excitation = -Constants.THRESHOLD;
            simState.schedule.scheduleOnceIn(Constants.REFRACTION_TIME, this);
        } else {
            excitation = 0.0;
            for (Object agent : currentInputExcitations.keySet()) {
                Pair<Double, Double> pair = currentInputExcitations.get(agent);
                currentInputExcitations.replace(agent, new ImmutablePair<>(pair.getLeft(), now(simState)));
            }
            state = NeuronState.REGULAR;
            rescheduleActivation(simState);
        }
    }

    public void startInputExcitation(Object neighbour, double weight, SimState simState) {
        if (state == NeuronState.REGULAR) {
            excitation = newExcitation(simState);
        }
        currentInputExcitations.put(neighbour, new ImmutablePair<>(weight, now(simState)));
        if (state == NeuronState.REGULAR) {
            rescheduleActivation(simState);
        }
    }

    public void stopInputExcitation(Object neighbour, SimState simState) {
        if (state == NeuronState.REGULAR) {
            excitation = newExcitation(simState);
        }
        currentInputExcitations.remove(neighbour);
        if (state == NeuronState.REGULAR) {
            rescheduleActivation(simState);
        }
    }

    protected void recordActivation(NeuralNetwork neuralNetwork) {
        neuralNetwork.currentStimulation.recordActivation(this);
    }

    private double newExcitation(SimState simState) {
        double newExcitation = currentInputExcitations.isEmpty()
                ? Math.max(0.0, excitation - countWhenNoCurrentInputExcitations(simState))
                : Math.min(1.0, excitation + countBasedOnCurrentInputExcitations(simState)); // TODO: remove hack with Math.min
        lastUpdateTime = Optional.of(now(simState));
        return newExcitation;
    }

    private double countWhenNoCurrentInputExcitations(SimState simState) {
        return lastUpdateTime.isEmpty() ? 0.0 : Math.signum(excitation) * relaxation(excitation, now(simState) - lastUpdateTime.get());
    }

    private double relaxation(double excitation, double time) {
        return time * Constants.RELAXATION_RATIO;
    }

    private double countBasedOnCurrentInputExcitations(SimState simState) {
        double now = now(simState);
        double sum = 0;
        for (Object key : currentInputExcitations.keySet()) {
            Pair<Double, Double> pair = currentInputExcitations.get(key);
            sum += pair.getLeft() * (now - pair.getRight());
        }

        return sum;
    }

    private void rescheduleActivation(SimState simState) {
        nearestActivation.ifPresent(TentativeStep::stop);
        nearestActivation = Optional.of(new TentativeStep(this));
        simState.schedule.scheduleOnceIn(computeActivationTime(), nearestActivation.get());
    }

    private double computeActivationTime() {
        double remainingExcitation = Constants.THRESHOLD - excitation;
        return remainingExcitation != 0.0 ? remainingExcitation / weightSum() : 0.0;
    }

    private double weightSum() {
        double sum = 0;
        for (Object key : currentInputExcitations.keySet()) {
            Pair<Double, Double> pair = currentInputExcitations.get(key);
            sum += pair.getLeft();
        }

        return sum;
    }

    private double now(SimState simState) {
        return simState.schedule.getTime();
    }

    public double getExcitation() {
        return excitation;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.#####");
        return "BaseNeuronAgent{\n" +
                "state=" + state +
                "\nexcitation=" + df.format(excitation) +
                "\nname='" + name + '\'' +
                '}';
    }
}
