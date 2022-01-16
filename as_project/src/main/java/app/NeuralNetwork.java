package app;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;

import java.util.ArrayList;
import java.util.List;


public class NeuralNetwork extends SimState {
    public Continuous2D brain = new Continuous2D(1.0, 100, 100);
    public Network network = new Network(true);

    public ReceptorsStimulation currentStimulation = null;
    
    public NeuralNetwork(long seed) {
        super(seed);
    }

    public void start() {
        super.start();

        List<ReceptoryField> receptoryFields = new ArrayList<>();

        receptoryFields.add(new ReceptoryField(5.0, true, true, 5.0, 30, 100));
        receptoryFields.add(new ReceptoryField(5.0, true, true, 5.0, 110, 180));
        receptoryFields.add(new ReceptoryField(5.0, true, false, 205.0, 30, 100));
        receptoryFields.add(new ReceptoryField(5.0, true, false, 205.0, 110, 180));
        receptoryFields.add(new ReceptoryField(5.0, false, false, 205.0, 30, 180));

        scheduleRandomStimulations(receptoryFields, 1000);
    }

    private void scheduleRandomStimulations(List<ReceptoryField> receptoryFields, double totalTime) {
        for (double startTime = 0.0; startTime < totalTime; startTime += random.nextDouble() * 15) {
            double endTime = startTime + random.nextDouble() * 15;

            ReceptorsStimulation receptorsStimulation = new ReceptorsStimulation(this, startTime, endTime);
            for (ReceptoryField receptoryField : receptoryFields) {
                receptorsStimulation.addReceptedValue(receptoryField, random.nextDouble() * receptoryField.getValueRange());
            }

            startTime = endTime;
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
