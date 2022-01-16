package app;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Bag;

import java.io.File;
import java.util.*;


public class NeuralNetwork extends SimState {
    public Continuous2D brain = new Continuous2D(1.0, 100, 100);
    public Network network = new Network(true);

    public ReceptorsStimulation currentStimulation = null;
    private int correctClassification = 0;
    private int wrongClassifications = 0;
    
    public NeuralNetwork(long seed) {
        super(seed);
    }

    private final String csvPath = "C:\\Users\\adams\\Studia\\Agent systems\\agent_systems_neurons\\as_project\\src\\main\\resources\\iris.data";

    public void start() {
        super.start();

        List<ReceptoryField> receptoryFields = new ArrayList<>();

        double constCoordLow = 5.0;
        double constCoordHigh = 95.0;

        receptoryFields.add(new ReceptoryField(Constants.EPSILON, true, true, constCoordLow, 15, 47, 4.3, 7.9));
        receptoryFields.add(new ReceptoryField(Constants.EPSILON, true, true, constCoordLow, 53, 85, 2.0, 4.4));
        receptoryFields.add(new ReceptoryField(Constants.EPSILON, true, false, constCoordHigh, 15, 47, 1.0, 6.9));
        receptoryFields.add(new ReceptoryField(Constants.EPSILON, true, false, constCoordHigh, 53, 85, 0.1, 2.5));
        receptoryFields.add(new ReceptoryField(Constants.EPSILON, false, false, constCoordHigh, 15, 85, 0.0, 2.0));

//        scheduleRandomStimulations(receptoryFields, 1000);
        scheduleStimulationsAccordingToCsv(receptoryFields);
    }

    public void finish() {
        System.out.printf("%n Accuracy: %f%n", getAccuracy());
    }

    private void scheduleStimulationsAccordingToCsv(List<ReceptoryField> receptoryFields) {
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(csvPath));) {
            while (scanner.hasNextLine()) {
                records.add(getRecordFromLine(scanner.nextLine()));
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }

        Collections.shuffle(records);

        scheduleStimulations(records, receptoryFields);
    }

    private List<String> getRecordFromLine(String line) {
        final String COMMA_DELIMITER = ",";

        List<String> values = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

    private void scheduleStimulations(List<List<String>> records, List<ReceptoryField> receptoryFields) {
        double startTime = 0.0;

        int n_learning_examples = 100;

        Map<String, Double> labels = new HashMap<>() {{
            put("Iris-setosa", 0.0);
            put("Iris-versicolor", 1.0);
            put("Iris-virginica", 2.0);
        }};

        for (int sampleIdx = 0; sampleIdx < records.size(); sampleIdx++) {
            List<String> record = records.get(sampleIdx);
            ReceptorsStimulation receptorsStimulation = new ReceptorsStimulation(this, startTime, startTime + Constants.RECORD_STIMULATION_TIME);

            for (int i = 0; i < 4; i++) {
                double value = Double.parseDouble(record.get(i));
                receptorsStimulation.addReceptedValue(receptoryFields.get(i), value);
            }

            double value = labels.get(record.get(4));
            if (sampleIdx < n_learning_examples) {
                receptorsStimulation.addReceptedValue(receptoryFields.get(4), value);
            }
            else {
                receptorsStimulation.markAsValidation(receptoryFields.get(4), value);
            }

            startTime += Constants.RECORD_STIMULATION_TIME + Constants.PAUSE_BETWEEN_RECORDS;
        }
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

    public void recordClassification(boolean isCorrect) {
        if (isCorrect) {
            correctClassification += 1;
        }
        else {
            wrongClassifications += 1;
        }
    }

    public double getAccuracy() {
        return correctClassification + wrongClassifications != 0 ? (double) correctClassification / (correctClassification + wrongClassifications) : 0.0;
    }
}
