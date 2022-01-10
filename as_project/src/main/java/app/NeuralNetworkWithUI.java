package app;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

import javax.swing.*;
import java.awt.*;

public class NeuralNetworkWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D neuralNetworkContinuousPortrayal = new ContinuousPortrayal2D();
    NetworkPortrayal2D neuralNetworkNetworkPortrayal = new NetworkPortrayal2D();

    public NeuralNetworkWithUI(){
        super(new NeuralNetwork(System.currentTimeMillis()));
    }
    public NeuralNetworkWithUI(SimState simState){
        super(simState);
    }
    public static String getName(){
        return "Neural Network Simulation";
    }

    public static void main(String[] args){
        NeuralNetworkWithUI neuralNetworkWithUI = new NeuralNetworkWithUI();
        Console c = new Console(neuralNetworkWithUI);
        c.setVisible(true);
    }

    public void start(){
        super.start();
        setupPortrayals();
    }

    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals(){
        NeuralNetwork neuralNetwork = (NeuralNetwork) state;

        neuralNetworkContinuousPortrayal.setField(neuralNetwork.brain);
        neuralNetworkContinuousPortrayal.setPortrayalForClass(ReceptoryNeuron.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        BaseNeuronAgent baseNeuronAgent = (BaseNeuronAgent) object;

                                        double excitation = baseNeuronAgent.getExcitation();
                                        int color= (int) (excitation+1)*100;
                                        paint = new Color(color, 0,255-color);
                                        super.draw(object, graphics, info);
                                    }
                                }, 5.0, null, Color.black, true),
                        0, 5.0, Color.green, true)));

        neuralNetworkContinuousPortrayal.setPortrayalForClass(Sensor.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        Sensor sensor = (Sensor) object;

                                        int color= sensor.isStimulated() ? 255 : 0;
                                        paint = new Color(color, 0,255-color);
                                        super.draw(object, graphics, info);
                                    }
                                }, 5.0, null, Color.black, true),
                        0, 5.0, Color.green, true)));

        neuralNetworkContinuousPortrayal.setPortrayalForClass(ReceptoryField.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        ReceptoryField receptoryField = (ReceptoryField) object;

                                        int color= receptoryField.isStimulated() ? 255 : 0;
                                        paint = new Color(color, 0,255-color);
                                        super.draw(object, graphics, info);
                                    }
                                }, 5.0, null, Color.black, true),
                        0, 5.0, Color.green, true)));

        neuralNetworkNetworkPortrayal.setField(new SpatialNetwork2D(neuralNetwork.brain, neuralNetwork.network));
        SimpleEdgePortrayal2D p = new SimpleEdgePortrayal2D(Color.black, Color.black, Color.black, new Font("SansSerif", 0, 3)) {
            @Override
            public String getLabel(Edge edge, EdgeDrawInfo2D info) {
                return String.format("%.2f", edge.getWeight());
            }
        };
        p.setAdjustsThickness(true);
        p.setBaseWidth(1.0);
        neuralNetworkNetworkPortrayal.setPortrayalForAll(p);

        display.reset();
        display.setBackdrop(Color.WHITE);

        display.repaint();
    }

    public void init(Controller c){
        super.init(c);
        display = new Display2D(600,600,this);
        display.setClipping(false);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Neural Network Display");
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(neuralNetworkNetworkPortrayal, "Network");
        display.attach(neuralNetworkContinuousPortrayal, "Neural Network");
    }

    public void quit(){
        super.quit();
        if (displayFrame!=null)
            displayFrame.dispose();
        displayFrame=null;
        display=null;
    }

    public Object getSimulationInspectedObject() {
        return state;
    }

    public Inspector getInspector(){
        Inspector inspector = super.getInspector();
        inspector.setVolatile(true);
        return inspector;
    }

}
