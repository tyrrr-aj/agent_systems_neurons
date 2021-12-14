package app;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.TentativeStep;

import java.util.*;

public class BaseNeuronAgent implements Steppable {
    private List<BaseNeuronAgent> neighbours;
    private HashMap<Object, Pair<double, double>> currentInputExcitations; //left weight, right startTime
    private NeuronState state;
    private double excitation;
    private Optional<Double> lastUpdateTime;
    private Optional<TentativeStep> nearestActivation;

    public BaseNeuronAgent() {
        neighbours = new ArrayList<>();
        currentInputExcitations = new HashMap<>();
        state = NeuronState.REGULAR;
        excitation = 0.0;
        lastUpdateTime = Optional.empty();
        nearestActivation = Optional.empty();
    }

    @Override
    public void step(SimState simState) {
        if (state == NeuronState.REGULAR) {
            for (BaseNeuronAgent neigh : neighbours) {
                double weight = ((NeuralNetwork)simState).network.getEdge(this, neigh).getWeight();
                neigh.startInputExcitation(this, weight, simState);
            }
            state = NeuronState.ACTIVATED;
            simState.schedule.scheduleOnceIn(Constants.ACTIVATION_TIME, this);
        } else if (state == NeuronState.ACTIVATED) {
            for (BaseNeuronAgent neigh : neighbours) {
                neigh.stopInputExcitation(this, simState);
            }
            state = NeuronState.REFRACTED;
            simState.schedule.scheduleOnceIn(Constants.REFRACTION_TIME, this);
        } else {
            excitation = 0.0;
            for (Object agent : currentInputExcitations.keySet()) {
                Pair<double, double> pair = currentInputExcitations.get(agent);
                currentInputExcitations.replace(agent, new ImmutablePair<double, double>(pair.getLeft(), now(simState)));
            }
            state = NeuronState.REGULAR;
            rescheduleActivation(simState);
        }
    }

    private void startInputExcitation(Object neighbour, double weight, SimState simState) {
        currentInputExcitations.put(neighbour, new ImmutablePair<double, double>(weight, now(simState)));
        if (state == NeuronState.REGULAR) {
            excitation = newExcitation(simState);
            rescheduleActivation(simState);
        }
    }

    private void stopInputExcitation(BaseNeuronAgent neighbour, SimState simState) {
        currentInputExcitations.remove(neighbour);
        if (state == NeuronState.REGULAR) {
            excitation = newExcitation(simState);
            rescheduleActivation(simState);
        }
    }

    private double newExcitation(SimState simState) {
        double newExcitation = currentInputExcitations.isEmpty()
                ? excitation - countWhenNoCurrentInputExcitations(simState)
                : excitation + countBasedOnCurrentInputExcitations(simState);
        lastUpdateTime = Optional.of(now(simState));
        return newExcitation;
    }

    private double countWhenNoCurrentInputExcitations(SimState simState) {
        return Math.signum(excitation) * relaxation(excitation, now(simState) - lastUpdateTime.get());
    }

    private double relaxation(double excitation, double time) {
        return time * Constants.RELAXATION_RATIO;
    }

    private double countBasedOnCurrentInputExcitations(SimState simState) {
        double now = now(simState);
        double sum = 0;
        for (Object key : currentInputExcitations.keySet()) {
            Pair<double, double> pair = currentInputExcitations.get(key);
            sum += pair.getLeft() * (now - pair.getRight());
        }

        return sum;
    }

    private void rescheduleActivation(SimState simState) {
        nearestActivation.ifPresent(activation -> activation.stop());
        simState.schedule.scheduleOnceIn(computeActivationTime(simState), this);
    }

    private double computeActivationTime(SimState simState) {
        return (Constants.THRESHOLD - excitation - countBasedOnCurrentInputExcitations(simState))/weightSum();
    }

    private double weightSum() {
        double sum = 0;
        for (Object key : currentInputExcitations.keySet()) {
            Pair<double, double> pair = currentInputExcitations.get(key);
            sum += pair.getLeft();
        }

        return sum;
    }

    private double now(SimState simState) {
        return simState.schedule.getTime();
    }
}
