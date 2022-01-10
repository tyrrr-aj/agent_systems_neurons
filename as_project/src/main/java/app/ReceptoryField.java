package app;

import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.TentativeStep;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.*;

public class ReceptoryField implements Steppable {
    private final double epsilon;
    private Double minValue = 0.0;
    private Double maxValue = 1.0;
    private final boolean isVertical;
    private final boolean facedUp;
    private final double constantCoord;
    private final double variableCoordMin;
    private final double variableCoordMax;
    private boolean isStimulated = false;

    private final PriorityQueue<Pair<Double, Double>> stimulations = new PriorityQueue<>((e1, e2) -> e1.getLeft() < e2.getLeft() ? -1 : 1); // first time, second stimulation

    public ReceptoryField(double epsilon, boolean isVertical, boolean facedUp, double constantCoord, double variableCoordMin, double variableCoordMax) {
        this.epsilon = epsilon;
        this.isVertical = isVertical;
        this.facedUp = facedUp;
        this.constantCoord = constantCoord;
        this.variableCoordMin = variableCoordMin;
        this.variableCoordMax = variableCoordMax;
    }

    @Override
    public void step(SimState simState) {
        NeuralNetwork neuralNetwork = (NeuralNetwork) simState;

        Pair<Double, Double> event = stimulations.remove();
        Double stimulation = event.getRight();

        if (isStimulated) {
            stopNeighboursStimulation(neuralNetwork);
        }

        if (stimulation != null) {

            move(neuralNetwork, stimulation);

            Bag nearestNeighs = neuralNetwork.brain.getNeighborsWithinDistance(getPosition(neuralNetwork), epsilon);
            if (Arrays.stream(nearestNeighs.toArray())
                    .filter(neigh -> neigh instanceof Sensor)
                    .map(Sensor.class::cast)
                    .filter(neigh -> neigh.getReceptoryField() == this)
                    .findAny()
                    .isEmpty()) {
                Double2D vec = isVertical ? new Double2D(1.0, 0.0) : new Double2D(0.0, 1.0);
                if (!facedUp) {
                    vec = vec.multiply(-1.0);
                }
                new Sensor(neuralNetwork, this, getPosition(neuralNetwork).add(vec), vec);
            }

            Bag neighs = neuralNetwork.brain.getNeighborsWithinDistance(getPosition(neuralNetwork), 5 * epsilon);
            Arrays.stream(neighs.toArray())
                    .filter(neigh -> neigh instanceof Sensor)
                    .map(Sensor.class::cast)
                    .filter(neigh -> neigh.getReceptoryField() == this)
                    .forEach(neigh -> neigh.startStimulation(neuralNetwork));

            isStimulated = true;
        }
        else {
            isStimulated = false;
        }
    }

    public void scheduleStartStimulation(double value, SimState simState, double time) {
        stimulations.offer(Pair.of(time, value));
        simState.schedule.scheduleOnce(time, this);
    }

    public void scheduleStopStimulation(SimState simState, double time) {
        stimulations.offer(Pair.of(time, null));
        simState.schedule.scheduleOnce(time, this);
    }


    public Double2D getPosition(NeuralNetwork neuralNetwork) {
        return neuralNetwork.brain.getObjectLocation(this);
    }

    public double getValueRange() {
        return minValue != null ? maxValue - minValue : 0.0;
    }

    public double getSpatialRange() {
        return variableCoordMax - variableCoordMin + 1.0; // +1.0 is for vec to sensors (it's manhattan distance)
    }

    public boolean isStimulated() {
        return isStimulated;
    }

    private void stopNeighboursStimulation(NeuralNetwork neuralNetwork) {
        Bag neighs = neuralNetwork.brain.getNeighborsWithinDistance(getPosition(neuralNetwork), 5 * epsilon);
        Arrays.stream(neighs.toArray())
                .filter(neigh -> neigh instanceof Sensor)
                .map(Sensor.class::cast)
                .filter(neigh -> neigh.getReceptoryField() == this)
                .forEach(neigh -> neigh.stopStimulation(neuralNetwork));
    }

    private void move(NeuralNetwork neuralNetwork, double stimulation) {
        double ratio = stimulation / getValueRange();
        double varCoord = variableCoordMin + (variableCoordMax - variableCoordMin) * ratio;
        Double2D newPosition = isVertical ? new Double2D(constantCoord, varCoord) : new Double2D(varCoord, constantCoord);
        neuralNetwork.brain.setObjectLocation(this, newPosition);
    }
}
